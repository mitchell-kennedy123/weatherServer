package contentserver;

import common.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.*;

public class ContentServer implements NetworkNode, Runnable {

  private static final Logger logger = Logger.getLogger(ContentServer.class.getName());

  private String serverAddress;
  private int port;
  private String filePath;
  private final String contentServerID;
  private LamportClock lamportClock;
  private boolean isRunning;

  /**
   * Constructor for ContentServer.
   * Initializes the content server with a specified server address, port, and file path.
   *
   * @param serverAddress The IP address where the server will send PUT requests.
   * @param port The port number of the target AggregationServer.
   * @param filePath The path of the file containing weather data.
   *                 The name of .txt file specifies the content server ID.
   */
  public ContentServer(String serverAddress, int port, String filePath) {
    this.serverAddress = serverAddress;
    this.port = port;
    this.filePath = filePath;
    this.contentServerID = extractIDFromFilePath(filePath);  // Extract the ID from file path
    this.lamportClock = new LamportClock();
    this.isRunning = false;
    LoggerSetup.setupLogger(logger, "logs/content-server.log");
  }

  /**
   * Extracts the ID from the file path.
   *
   * @param filePath The file path.
   * @return String - the content server ID.
   */
  private String extractIDFromFilePath(String filePath) {
    String fileName = Paths.get(filePath).getFileName().toString();
    int dotIndex = fileName.lastIndexOf(".");
    return fileName.substring(0, dotIndex);  // Remove the .txt
  }

  @Override
  public boolean startup() {
    isRunning = true;
    logger.info("Content Server started on " + serverAddress + ":" + port);
    return true;
  }

  @Override
  public boolean shutdown() {
    isRunning = false;
    logger.info("Content Server shutting down.");
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
   * Reads weather data from a file and returns it as a WeatherDataSerializer object.
   *
   * @return WeatherDataSerializer - the weather data from the file.
   */
  private WeatherDataSerializer readFileToWeatherData() throws IOException {
    logger.info("Reading weather data from file: " + filePath);
    String fileContent = Files.readString(Paths.get(filePath));
    return WeatherDataSerializer.extractDataFromTxt(fileContent);
  }

  /**
   * Builds an HTTP PUT request string.
   *
   * @param jsonBody String - the JSON body.
   * @return String - the HTTP PUT request.
   */
  private String jsonHttpRequest(String jsonBody) {
    HttpWriter httpWriter = new HttpWriter();
    return httpWriter
            .setMethod("PUT")
            .setPath("/weather.json")
            .setHttpVersion("HTTP/1.1")
            .addHeader("Lamport-Timestamp", String.valueOf(lamportClock.getTime()))
            .addHeader("Content-Server-Id", contentServerID)
            .setBody(jsonBody)
            .toString();
  }

  /**
   * Sends the HTTP PUT request to the AggregationServer.
   *
   * @param httpRequest String - the HTTP PUT request to be sent.
   */
  private void sendPutRequest(String httpRequest) {
    logger.info("Sending PUT request to " + serverAddress + ":" + port);
    try (Socket socket = new Socket(serverAddress, port);
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

      // Send the HTTP request
      out.print(httpRequest);
      out.flush();

      // Read the server's response
      StringBuilder response = new StringBuilder();
      String responseLine;
      String statusLine = in.readLine();  // Read the status line (HTTP/1.1 200 OK)
      if (statusLine != null) {
        response.append(statusLine).append("\n");
      } else {
        logger.info("No server response.");
        return;
      }

      // Extract HTTP status code from the status line
      String[] statusParts = statusLine.split(" ");
      int statusCode = Integer.parseInt(statusParts[1]);

      // Read and append the rest of the response
      while ((responseLine = in.readLine()) != null) {
        response.append(responseLine).append("\n");
      }

      // Log the server's response
      logger.info("Server response: " + response.toString());

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error while sending PUT request", e);
    }
  }

  /**
   * Converts the data to JSON format, builds and sends the HTTP request.
   */
  public void makePutRequest() {
    try {
      WeatherDataSerializer weatherDataSerializer = readFileToWeatherData();
      String jsonString = weatherDataSerializer.toJson();
      String httpRequest = jsonHttpRequest(jsonString);
      sendPutRequest(httpRequest);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading file: " + filePath, e);
    }
  }


  @Override
  public void run() {
    startup();
    makePutRequest();
    shutdown();
  }

  /**
   * Initializes the server with command-line arguments, creates a thread to run it,
   * and starts the server.
   *
   * @param args String[] - command-line arguments: <server_address:port> <file_path>.

   */
  public static void main(String[] args) {
    if (args.length < 2) {
      logger.severe("Usage: <server_address:port> <file_path>");
      return;
    }

    String[] serverDetails = args[0].split(":");
    String serverAddress = serverDetails[0];
    int port = Integer.parseInt(serverDetails[1]);
    String filePath = args[1];

    ContentServer contentServer = new ContentServer(serverAddress, port, filePath);
    Thread serverThread = new Thread(contentServer);
    serverThread.start();
  }
}