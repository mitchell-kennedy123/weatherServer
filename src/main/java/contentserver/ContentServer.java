package contentserver;

import common.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.*;

public class ContentServer implements NetworkNode, Runnable {

  // Logger instance
  private static final Logger logger = Logger.getLogger(ContentServer.class.getName());

  // Instance variables
  private String serverAddress;
  private int port;
  private String filePath;
  private final String contentServerID;
  private LamportClock lamportClock;
  private boolean isRunning;

  // Constructor
  public ContentServer(String serverAddress, int port, String filePath) {
    this.serverAddress = serverAddress;
    this.port = port;
    this.filePath = filePath;
    this.contentServerID = extractIDFromFilePath(filePath);  // Extract the ID from file path
    this.lamportClock = new LamportClock();
    this.isRunning = false;
    LoggerSetup.setupLogger(logger, "logs/content-server.log");
  }

  // Extract the ID from the file path
  private String extractIDFromFilePath(String filePath) {
    String fileName = Paths.get(filePath).getFileName().toString();

    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex > 0) {
      return fileName.substring(0, dotIndex);  // Remove the extension
    }
    return fileName;  // If no extension, return the entire filename
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

  // Method to create common.WeatherDataSerializer from a file
  private WeatherDataSerializer readFileToWeatherData() throws IOException {
    logger.info("Reading weather data from file: " + filePath);
    String fileContent = Files.readString(Paths.get(filePath));
    return WeatherDataSerializer.extractDataFromTxt(fileContent);
  }

  // Method to build an HTTP request for PUT operation
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
      String statusLine = in.readLine();  // Read the status line (e.g., HTTP/1.1 200 OK)
      if (statusLine != null) {
        response.append(statusLine).append("\n");
      } else {
        logger.info("No server response.");
        // TODO: Retry after some amount of time
        return;
      }

      // Extract HTTP status code from the status line
      String[] statusParts = statusLine.split(" ");
      int statusCode = Integer.parseInt(statusParts[1]);  // Status code is the second element

      // Read and append the rest of the response
      while ((responseLine = in.readLine()) != null) {
        response.append(responseLine).append("\n");
      }

      // Log the server's response
      logger.info("Server response: " + response.toString());

      // Handle the response based on the status code
      handleResponse(statusCode, response.toString());

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error while sending PUT request", e);
    }
  }

  // Method to handle the server's response based on the status code
  // TODO
  private void handleResponse(int statusCode, String response) {
    switch (statusCode) {
      case 200:  // OK

        break;
      case 201:  // Created

        break;
      case 400:  // Bad Request

        break;
      case 404:  // Not Found

        break;
      case 500:  // Internal Server Error

        break;
      default:

    }
  }

  // Method to send the PUT request with weather data
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

  // Runnable implementation
  @Override
  public void run() {
    startup();
    makePutRequest();
    shutdown();
  }

  // Main method for running ContentServer
  public static void main(String[] args) {
    if (args.length < 2) {
      logger.severe("Usage: java ContentServer <server_address:port> <file_path>");
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