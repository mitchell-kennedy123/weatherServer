import aggregationserver.AggregationServer;
import contentserver.ContentServer;
import org.junit.jupiter.api.*;

import java.io.FileWriter;
import java.io.IOException;

public class ContentServerTest {

  private static final int AGG_SERVER_PORT = 4567;
  private static final String AGG_SERVER_ADDRESS = "localhost";
  private static Thread aggServerThread;
  private static final String filePath = "./data/ContentServerData/weather1.txt";

  // Helper method to create a sample weather.txt file for the test
  private void createTestFile(String filePath) throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write("id:IDS60901\n");
      writer.write("name:Adelaide (West Terrace / ngayirdapira)\n");
      writer.write("state:SA\n");
      writer.write("time_zone:CST\n");
      writer.write("lat:-34.9\n");
      writer.write("lon:138.6\n");
      writer.write("local_date_time:15/04:00pm\n");
      writer.write("local_date_time_full:20230715160000\n");
      writer.write("air_temp:13.3\n");
      writer.write("apparent_t:9.5\n");
      writer.write("cloud:Partly cloudy\n");
      writer.write("dewpt:5.7\n");
      writer.write("press:1023.9\n");
      writer.write("rel_hum:60\n");
      writer.write("wind_dir:S\n");
      writer.write("wind_spd_kmh:15\n");
      writer.write("wind_spd_kt:8");
    }
  }

  // Step 1: Start the real AggregationServer before all tests
  @BeforeEach
  public void startAggregationServer() throws InterruptedException, IOException {
    // Use the AggregationServer's main method to start it
    String[] args = {String.valueOf(AGG_SERVER_PORT)};
    aggServerThread = new Thread(() -> {
      AggregationServer.main(args);});
    System.out.println("AggregationServer started. \n Waiting 2s for connection...");
    Thread.sleep(2000);
    createTestFile(filePath); // Populate a test weather data txt file
  }

  @AfterEach
  public void stopAggregationServer() {
    // Interrupt the aggregation server thread to shut it down
    if (aggServerThread != null && aggServerThread.isAlive()) {
      aggServerThread.interrupt();
    }
  }

  // This test only tests if the server acts as expected using command line arguments
  @Test
  public void testContentServerMainFunction() throws IOException, InterruptedException {
    String[] args = {AGG_SERVER_ADDRESS + ":" + AGG_SERVER_PORT, filePath};

    // Call the main method of ContentServer to simulate command-line execution
    Thread contextServerThread = new Thread(() -> {
      ContentServer.main(args);});

    // Test needs to be validated manually to see if the PUT request worked
    // Can assume it works if no errors are thrown
  }

  @Test
  public void testContentServerConnection() throws IOException, InterruptedException {

    // Step 3: Create contentserver.ContentServer and start in a new thread
    ContentServer contentServer = new ContentServer(AGG_SERVER_ADDRESS, AGG_SERVER_PORT, filePath);
    Thread serverThread = new Thread(contentServer);

    // Start the server thread
    serverThread.start();

    // Wait for the server to start and process the request
    serverThread.join(1000);

    // You can add assertions or validations here if needed, but as we're simulating
    // the server, you might want to check if it's functioning or responding correctly.

    // Optionally, clean up the test file if needed
    // Files.deleteIfExists(Paths.get(filePath));
  }
}