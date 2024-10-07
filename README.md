# Assignment 2

Building an aggregation server with consistency management and a RESTful API.

See the build instructions and test documentation below.

### Bonus Marks - JSON Parsing

My JSON parser functionality for the bonus marks is within the `common/WeatherDataSerializer` class and is tested in the `WeatherDataSerializerTest` class.

### Initial Design Sketch

The initial design sketch `DesignSketchWeatherServer.pdf` can be found in the root of the project directory.

### Changes from draft (Instead of Changes.pdf)

In the draft I only had two servers connected through sockets which were able to send and receive http text data. Since then, I have Implemented all others functionality

## How to Compile and Run the System with Maven

### Prerequisites

- Ensure you have Java JDK 8 or higher installed.
- Apache Maven must be installed and configured.

### Step 1: Compile the Project

Before running the servers and client, compile the project by navigating to the project root directory in your terminal and running the following command:

```bash
mvn compile
```

### Step 2: Running the Servers and Client

Each server and client should be **run in a separate terminal**. Follow the instructions below to run them with the necessary command-line arguments.

Ensure that the `./data/ContentServerData/weather.txt` file is present. The below commands simulates the startup of each server, a PUT request from the content server and a GET request from the get client.

#### 1. Aggregation Server
```bash
mvn exec:java -Dexec.mainClass="aggregationserver.AggregationServer" -Dexec.args="4567"
```
#### 2. Content Server

```bash
mvn exec:java -Dexec.mainClass="contentserver.ContentServer" -Dexec.args="localhost:4567 ./data/ContentServerData/weather.txt"
```
#### 3. Get Client

```bash
mvn exec:java -Dexec.mainClass="getclient.GetClient" -Dexec.args="localhost:4567 weather"
```

## List of Tests

These test cases can be found in the `test/java` directory. They were run using maven in IntelliJ.

All tests can be run using `mvn test`. (note some test will take upwards of 30 seconds to test delays)

In the `LoggerSetup.java` file it is recommended setting the debug to SEVERE when running all tests.

### 1. Command Line Parsing
- Command line parsing testing is provided through the above section on running the servers in the command line.


### 2. Integration Testing
Given the complex output of integration tests, verification is done manually.
The files from the `data/AggregationServerData` can be manually inspected and deleted after each test.

`ServerIntegrationTest` Tests the integration of AggregationServer, ContentServer, and GetClient.

- **testPutRequest**: Sends a PUT request to store data.
- **testPutAndGetRequestByID**: Verifies data can be retrieved by station ID.
- **testPutAndGetRequestByLatest**: Verifies the most recent data is retrieved.
- **testManyPutRequests**: Tests 25 PUT requests and checks the server keeps only the latest 20 entries.
- **testPutRequestsWithDelay**: Tests file cleanup by sending two PUT requests with a delay and checks if outdated files are removed.

### 3. Unit Testing for JSON Parser

`WeatherDataSerializerTest` Tests the JSON parser and text converter.

- **testToJson**: Verifies that data is correctly converted to JSON format.
- **testExtractDataFromJson**: Verifies data extraction from a JSON string.
- **testToTxt**: Verifies the conversion of data to text format.
- **testExtractDataFromTxt**: Verifies data extraction from a text string.
