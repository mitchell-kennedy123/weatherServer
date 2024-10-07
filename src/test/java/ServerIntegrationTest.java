import aggregationserver.AggregationServer;

import java.util.concurrent.Semaphore;
import contentserver.ContentServer;
import getclient.GetClient;
import org.junit.jupiter.api.*;

import java.io.*;



public class ServerIntegrationTest {
    private static final String AGG_SERVER_ADDRESS = "localhost";
    private static final int AGG_SERVER_PORT = 4567;
    private String filePath = "./data/ContentServerData/"; // Test weather data file

    // Helper method to create a sample weather.txt file for the test
    private void createTestFile(String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(filePath + fileName + ".txt")) {
            writer.write("id:IDS60901\n");
            writer.write("air_temp:13.3\n");
        }
    }

    /**
     * Test to verify that a PUT request is successfully sent to the AggregationServer.
     * This test creates a ContentServer, sends a PUT request, and then shuts down the AggregationServer.
     */
    @Test
    public void testPutRequest() throws IOException, InterruptedException {
        // Start the AggregationServer in a separate thread
        Thread aggServerThread = new Thread(() -> {
            AggregationServer.main(new String[]{"4567"});
        });
        aggServerThread.start();
        Thread.sleep(2000); // Wait for aggregation server to start up

        String fileName = "test_data_put";
        System.out.println("getRequest");
        String thisFilePath = filePath + fileName + ".txt";
        ContentServer contentServer = new ContentServer(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, thisFilePath);
        Thread contentServerThread = new Thread(contentServer);
        // Create the test file for the server to use
        createTestFile(fileName);
        contentServerThread.start();
        contentServerThread.join(2000);

        aggServerThread.interrupt(); // triggers shut down process

        // test_data should be in the ./data/AggregationServerData/ folder
    }

    /**
     * Test to verify both a PUT and GET request to the AggregationServer using a station ID.
     * This test sends a PUT request to store data, and then a GET request to retrieve the data by station ID.
     */
    @Test
    public void testPutAndGetRequestByID() throws IOException, InterruptedException {
        // Start the AggregationServer in a separate thread
        Thread aggServerThread = new Thread(() -> {
            AggregationServer.main(new String[]{"4567"});
        });
        aggServerThread.start();
        Thread.sleep(2000); // Wait for aggregation server to start up

        // PUT request
        String fileName = "test_data_get";
        String thisFilePath = filePath + fileName + ".txt";
        ContentServer contentServer = new ContentServer(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, thisFilePath);
        Thread contentServerThread = new Thread(contentServer);

        // Create the test file for the ContentServer to use
        createTestFile(fileName);
        contentServerThread.start();
        contentServerThread.join(2000); // Wait for the PUT request to complete

        // GET request to retrieve the data
        GetClient getClient = new GetClient(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, "test_data");
        Thread getClientThread = new Thread(getClient);
        getClientThread.start();
        getClientThread.join(2000); // Wait for the GET request to complete

        aggServerThread.interrupt(); // triggers shut down process

        // Expected data printed to the terminal is     id:IDS60901
        //                                              air_temp:13.3;
    }

    /**
     * Test to verify both a PUT and GET request to the AggregationServer using the most recent data.
     * This test sends a PUT request to store data, and then a GET request to retrieve the latest data.
     */
    @Test
    public void testPutAndGetRequestByLatest() throws IOException, InterruptedException {
        // Start the AggregationServer in a separate thread
        Thread aggServerThread = new Thread(() -> {
            AggregationServer.main(new String[]{"4567"});
        });
        aggServerThread.start();
        Thread.sleep(2000); // Wait for aggregation server to start up

        // PUT request
        String fileName = "test_data_get";
        String thisFilePath = filePath + fileName + ".txt";
        ContentServer contentServer = new ContentServer(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, thisFilePath);
        Thread contentServerThread = new Thread(contentServer);

        // Create the test file for the ContentServer to use
        createTestFile(fileName);
        contentServerThread.start();
        contentServerThread.join(2000); // Wait for the PUT request to complete

        // GET request to retrieve the data
        GetClient getClient = new GetClient(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, null);
        Thread getClientThread = new Thread(getClient);
        getClientThread.start();
        getClientThread.join(2000); // Wait for the GET request to complete

        aggServerThread.interrupt(); // triggers shut down process

        // Expected data printed to the terminal is     id:IDS60901
        //                                              air_temp:13.3;
    }

    /**
     * Test to verify the correct ordering of multiple PUT requests.
     * This test sends 25 PUT requests in sequence,  and that
     * the aggregation server only saves the 20 latest PUT requests.
     */
    @Test
    public void testManyPutRequests() throws IOException, InterruptedException {
        // Start the AggregationServer in a separate thread
        Thread aggServerThread = new Thread(() -> {
            AggregationServer.main(new String[]{"4567"});
        });
        aggServerThread.start();
        Thread.sleep(2000); // Wait for aggregation server to start up

        // Send 25 PUT requests using ContentServer
        for (int i = 1; i <= 25; i++) {
            String fileName = "test_data_" + i;
            String thisFilePath = filePath + fileName + ".txt";

            // Create the test file for the ContentServer to use
            createTestFile(fileName);

            // Start a new ContentServer for each request
            Thread contentServerThread = new Thread(() -> {
                ContentServer contentServer = new ContentServer(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, thisFilePath);
                contentServer.run();  // Directly run the request in the current thread

                System.out.println("PUT request " + fileName + " sent.");

            });
            contentServerThread.start();
            contentServerThread.join(2000);
        }

        aggServerThread.interrupt(); // triggers shut down process

        // test_data_6 -> test_data_25 should be in the ./data/AggregationServerData/ folder
    }

    /**
     * Test to verify multiple PUT requests with a delay between them.
     * This test sends two PUT requests, with a 5-second delay between them, and stops the server after a total of 31 seconds.
     * This test if the file deleting daemon works
     */
    @Test
    public void testPutRequestsWithDelay() throws IOException, InterruptedException {
        // Start the AggregationServer in a separate thread
        Thread aggServerThread = new Thread(() -> {
            AggregationServer.main(new String[]{"4567"});
        });
        aggServerThread.start();
        Thread.sleep(2000); // Wait for aggregation server to start up

        // First PUT request
        String fileName1 = "test_data_old";
        String thisFilePath1 = filePath + fileName1 + ".txt";
        ContentServer contentServer1 = new ContentServer(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, thisFilePath1);
        Thread contentServerThread1 = new Thread(contentServer1);

        // Create the test file for the first PUT request
        createTestFile(fileName1);
        contentServerThread1.start();

        // Wait for 5 seconds before the second PUT request
        Thread.sleep(5000);

        // Step 3: Second PUT request
        String fileName2 = "test_data_new";
        String thisFilePath2 = filePath + fileName2 + ".txt";
        ContentServer contentServer2 = new ContentServer(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, thisFilePath2);
        Thread contentServerThread2 = new Thread(contentServer2);

        // Create the test file for the second PUT request
        createTestFile(fileName2);
        contentServerThread2.start();


        // Wait for the remaining time to hit 31 seconds after first PUT
        // 26 + 5 second wait
        Thread.sleep(26000); // Wait to complete the 31-second total duration

        aggServerThread.interrupt(); // Trigger the shutdown process

        // Verify that only test_data_new is in the ./data/AggregationServerData/ folder
    }
}