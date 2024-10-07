package aggregationserver;

import common.*;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

// ClientHandler class responsible for handling each client request in a separate thread
class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final Socket clientSocket;
    private final AggregationServer server;

    public ClientHandler(Socket clientSocket, AggregationServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream os = clientSocket.getOutputStream();
                PrintWriter out = new PrintWriter(os, true)) {

            HttpReader request = new HttpReader(in);

            // Log the incoming request details
            logger.info("Request Method: " + request.getMethod());
            logger.info("Request Path: " + request.getPath());
            logger.info("Request HTTP Version: " + request.getHttpVersion());
            logger.info("Request Headers: " + request.getHeaders());
            logger.info("Request Body: " + request.getBody());

            // Handle PUT or GET requests
            if ("PUT".equals(request.getMethod())) {
                server.handlePutRequest(request, in, out);
            } else if ("GET".equals(request.getMethod())) {
                server.handleGetRequest(request, in, out);
            } else {
                int status = StatusCodes.BAD_REQUEST;
                // Send bad request response back to the client as http request was invalid
                String statusText = StatusCodes.getStatusText(status);
                String statusMessage = StatusCodes.getStatusMessage(status);
                server.sendResponse(out, status, statusText, statusMessage);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while handling client request", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket", e);
            }
        }
    }
}