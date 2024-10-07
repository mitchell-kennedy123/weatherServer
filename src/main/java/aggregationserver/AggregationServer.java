package aggregationserver;

import common.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.*;

public class AggregationServer implements NetworkNode, Runnable {

  private static final Logger logger = Logger.getLogger(AggregationServer.class.getName());

  private final String serverAddress;
  private final int port;
  private final LamportClock lamportClock;
  private boolean isRunning;
  private ServerSocket serverSocket;
  private final FileManager fileManager;

  /**
   * Constructor for AggregationServer
   * Initializes the server with a specified address and port. Sets up LamportClock and FileManager.
   *
   * @param serverAddress The IP address where the server will be running.
   * @param port The port number to listen for incoming connections.
   */
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
    fileManager.start();
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
      fileManager.shutdown();
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

  /**
   * Handles an incoming PUT request.
   * Extracts the Lamport timestamp and weather data from the request, updates the LamportClock,
   * and stores the data using the FileManager.
   *
   * @param request HttpReader - the incoming HTTP request with the PUT data.
   * @param in BufferedReader - the input stream reader to read data.
   * @param out PrintWriter - the output stream writer to send responses.
   * @return int - the HTTP status code.
   */
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
    return 200;
  }

  /**
   * Handles an incoming GET request.
   * Retrieves weather data based on the station ID, and sends it back to the client.
   *
   * @param request HttpReader - the incoming HTTP request with the GET data.
   * @param in BufferedReader - the input stream reader to read data.
   * @param out PrintWriter - the output stream writer to send responses.
   * @return int - the HTTP status code.
   */
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

  /**
   * Sends an HTTP response to the client.
   *
   * @param out PrintWriter - the output stream writer to send responses to the client.
   * @param statusCode int - the HTTP status code
   * @param statusText String - the status text (200 -> "OK").
   * @param message String - the body of the response message.
   */
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

  /**
   * Listens for incoming client connections.
   * Starts a new thread for each client connection and handles it using ClientHandler.
   */
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

  /**
   * Reads the port from the command-line arguments and starts the server
   * and gracefully stop the server.
   *
   * @param args String[] - command-line arguments: <port (optional)>
   */
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

    // Add shutdown hook to gracefully shut down the server when the program is terminated (kill signal)
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