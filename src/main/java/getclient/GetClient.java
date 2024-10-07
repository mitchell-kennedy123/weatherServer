package getclient;

import common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetClient implements NetworkNode, Runnable {

    private static final Logger logger = Logger.getLogger(GetClient.class.getName());

    private String serverAddress;
    private int port;
    private String stationID;
    private LamportClock lamportClock;
    private Boolean isRunning;
    private ServerSocket serverSocket;

    /**
     * Constructor for GetClient.
     * Initializes the client with server address, port, and station ID.
     *
     * @param serverAddress The IP address of the server to connect to.
     * @param port The port number of the server.
     * @param stationID The station ID for which weather data is requested.
     */
    public GetClient(String serverAddress, int port, String stationID) {
        this.serverAddress = serverAddress != null ? serverAddress : DEFAULT_SERVER_ADDRESS;
        this.port = port;
        this.lamportClock = new LamportClock();
        this.isRunning = false;
        LoggerSetup.setupLogger(logger, "logs/client-server.log");
    }

    @Override
    public String getServerAddress() {
        return serverAddress;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public LamportClock getLamportClock() {
        return lamportClock;
    }

    @Override
    public boolean startup() {
        isRunning = true;
        logger.info("Server started on " + serverAddress + ":" + port + " for Station ID: " + stationID);
        return true;
    }

    @Override
    public boolean shutdown() {
            isRunning = false;
            logger.info("Server shut down successfully.");
            return true;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        startup();
        makeGetRequest();
        shutdown();  // Ensure shutdown when the loop exits
    }

    /**
     * Builds an HTTP GET request for weather data in JSON format.
     *
     * @return String - HTTP GET request
     */
    private String jsonHttpRequest() {
        HttpWriter httpWriter = new HttpWriter();
        if (stationID!=null) {
            httpWriter.addHeader("Station-Id", stationID);
        }

        return httpWriter
            .setMethod("GET")
            .setPath("/weather.json")
            .setHttpVersion("HTTP/1.1")
            .addHeader("Lamport-Timestamp", String.valueOf(lamportClock.getTime()))
            .toString();
    }

    /**
     * Sends the GET request to the server and processes the response.
     * Converts the response into a readable format and prints to the command line.
     */
    private void makeGetRequest() {
        try {
            // Send the HTTP request
            String httpRequest = jsonHttpRequest();
            String httpResponse = sendGetRequest(httpRequest);

            if (httpResponse == null) {
                return;
            }
            //  Split the response
            String[] httpsParts = httpResponse.split("\\{", 2);
            String data = (httpsParts.length > 1) ? "{" + httpsParts[1] : null; // Ensure the JSON part is correctly formatted

            // Display Data
            String outputStr;
            if (data != null) {
                WeatherDataSerializer weatherData = WeatherDataSerializer.extractDataFromJson(data);
                outputStr = weatherData.toTxt();
            } else {
                outputStr = "No Data Received";
            }
            System.out.println("Data Received from: " + serverAddress + "\n\n" + outputStr);

        } catch (Exception e) {
            // Handle exceptions and print errors
            System.err.println("Error while making GET request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends the GET request reads the server's response.
     *
     * @param httpRequest String - the HTTP GET request.
     * @return String - the server's response.
     */
    private String sendGetRequest(String httpRequest) {
        logger.info("Sending GET request to " + serverAddress + ":" + port + " for Station ID: " + stationID);
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
                return null;
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

            return response.toString();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while sending PUT request", e);
            return null;
        }
    }

    /**
     * Initializes the server with command-line arguments, creates a thread to run it,
     * and starts the server.
     *
     * @param args String[] - command-line arguments: <server:port> <stationID (optional)>.
     */
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: GETClient <server:port stationID>");
            System.exit(1);
        }

        // Parse the server URL and port from the first argument
        String[] serverParts = args[0].split(":");
        String serverAddress = serverParts[0];
        int port = Integer.parseInt(serverParts[1]);

        // Handle station ID if provided
        String stationID = null;
        if (args.length == 2) {
            stationID = args[1];
        }

        // Create a new ContentServerNode with the parsed information
        GetClient node = new GetClient(serverAddress, port, stationID);
        Thread serverThread = new Thread(node);
        serverThread.start();
    }
}