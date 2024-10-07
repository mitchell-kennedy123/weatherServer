package aggregationserver;

import common.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.*;



public class AggregationServer implements NetworkNode, Runnable {

  private static final Logger logger = Logger.getLogger(AggregationServer.class.getName());

  private final String serverAddress;
  private final int port;
  private final LamportClock lamportClock;
  private boolean isRunning;
  private ServerSocket serverSocket;
  private final FileManager fileManager; // Add the FileManager

  public AggregationServer(String serverAddress, int port) {
    this.serverAddress = serverAddress;
    this.port = port;
    this.lamportClock = new LamportClock();
    this.isRunning = false;
    this.fileManager = new FileManager("data/AggregationServerData"); // Initialize the file manager
    LoggerSetup.setupLogger(logger, "logs/aggregation-server.log");
  }

  @Override
  public boolean startup() {
    isRunning = true;
    fileManager.start(); // Start the FileManager
    logger.info("Aggregation Server started on " + serverAddress + ":" + port);
    return true;
  }

  @Override
  public boolean shutdown() {
    isRunning = false;
    try {
      // Close the server socket to unblock the accept() call
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
      fileManager.shutdown(); // Shutdown the FileManager
      logger.info("Aggregation Server shutting down.");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error while shutting down the server socket", e);
      return false;
    }
    return true;
  }

  @Override
  public boolean isRunning() {
    return this.isRunning;
  }

  @Override
  public String getServerAddress() {
    return this.serverAddress;
  }

  @Override
  public int getPort() {
    return this.port;
  }

  @Override
  public LamportClock getLamportClock() {
    return this.lamportClock;
  }

  // Handle incoming PUT requests
  public int handlePutRequest(HttpReader request, BufferedReader in, PrintWriter out) {
    // Extract data from request
    int requestLamportTimestamp = Integer.parseInt(request.getHeader("Lamport-Timestamp"));
    WeatherDataSerializer dataFromJson = WeatherDataSerializer.extractDataFromJson(request.getBody());

    // Update lamport clock
    lamportClock.processEvent(requestLamportTimestamp);

    String contentServerId = request.getHeader("Content-Server-Id");
    WriteRequest writeRequest = new WriteRequest(requestLamportTimestamp, contentServerId, dataFromJson);
    fileManager.addWriteRequest(writeRequest); // Add the request to the FileManager

    String statusText = StatusCodes.getStatusText(200);
    String statusMessage = StatusCodes.getStatusMessage(200);
    sendResponse(out, 200, statusText, statusMessage);
    // Check is first entry for status codes
    boolean isFirstEntry = true; // TODO fix this
    return isFirstEntry ? StatusCodes.CREATED : StatusCodes.OK;
  }

  // Handle incoming GET requests
  public int handleGetRequest(HttpReader request, BufferedReader in, PrintWriter out) {
    String stationId = request.getHeader("Station-Id");

    try {
      String weatherData;
      if (stationId == null) {
        // If stationId is null, get the most recent file
        weatherData = fileManager.getMostRecentFile();
        if (weatherData == null) {
          logger.severe("No recent file found.");
          sendResponse(out, 404, "Not Found", "{\"error\":\"No recent data found\"}");
          return 404; // HTTP 404 Not Found
        }
      } else {
        // Otherwise, get data for the specific station ID
        weatherData = fileManager.readWeatherData(stationId);
        if (weatherData == null) {
          logger.severe("Data for station ID " + stationId + " not found.");
          sendResponse(out, 404, "Not Found", "{\"error\":\"Resource not found\"}");
          return 404; // HTTP 404 Not Found
        }
      }

      sendResponse(out, 200, "OK", weatherData);
      return 200; // HTTP 200 OK

    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading data", e);
      sendResponse(out, 500, "Internal Server Error", "{\"error\":\"Internal server error\"}");
      return 500; // HTTP 500 Internal Server Error
    }
  }

  // Send HTTP response to the client
  public void sendResponse(PrintWriter out, int statusCode, String statusText, String message) {
    out.println("HTTP/1.1 " + statusCode + " " + statusText);
    out.println("Content-Type: text/plain");
    out.println("Content-Length: " + message.length());
    out.println();
    out.println(message);
  }

  @Override
  public void run() {
    listen();
  }

  // Start listening for incoming connections
  private void listen() {
    try {
      serverSocket = new ServerSocket(port);
      logger.info("Server is listening on port " + port);
      while (isRunning) {
        Socket clientSocket = serverSocket.accept();
        logger.info("New client connected");

        // Handle each client connection in a new thread
        ClientHandler clientHandler = new ClientHandler(clientSocket, this);
        new Thread(clientHandler).start();
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error while listening for connections", e);
    }
  }

  // Usage <port>
  public static void main(String[] args) {
    int port = NetworkNode.DEFAULT_PORT;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        logger.severe("Invalid port number provided. Using default port " + port);
      }
    }

    AggregationServer server = new AggregationServer(NetworkNode.DEFAULT_SERVER_ADDRESS, port);
    server.startup();

    Thread serverThread = new Thread(server);
    serverThread.start();

    // Add shutdown hook to gracefully shut down the server when the program is terminated
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Shutdown hook triggered.");
      server.shutdown();
      try {
        serverThread.join(); // Ensure the server thread exits cleanly
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Error shutting down server thread", e);
      }
    }));
  }
}