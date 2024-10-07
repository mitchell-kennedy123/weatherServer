
# Assignment 2

TODO: Description 

### Bonus Marks - JSON Parsing

My JSON parser functionality for the bonus marks is within the `common/WeatherDataSerializer` class. 

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

# Testing Strategy

This document outlines the testing strategy and architecture used to ensure the functionality, reliability, and performance of the system components. Our tests cover the main functionalities across server management, client handling, file management, and data serialization. Each test has been crafted to ensure that all core components behave as expected in various scenarios.

## List of Tests

These test cases can be found in the `test/java` directory. They were run using maven in IntelliJ.

### 1. ContentServer 
- **Test Case 10**: Start ContentServer and Validate Initialization
   - Expected Outcome: ContentServer logs successful startup.
- **Test Case 11**: Handle Invalid Paths in GET Request
   - Expected Outcome: 404 error returned for invalid path requests.

### 2. GetClient
- **Test Case 10**: Start ContentServer and Validate Initialization
   - Expected Outcome: ContentServer logs successful startup.
- **Test Case 11**: Handle Invalid Paths in GET Request
   - Expected Outcome: 404 error returned for invalid path requests.

### 3. AggregationServer
- **Test Case 1**: Start Server and Handle Initial Connections
   - Expected Outcome: Server logs successful initialization and client connection.
- **Test Case 2**: Handle Multiple Client Connections
   - Expected Outcome: Server handles multiple connections without crashing.
- **Test Case 3**: Aggregation of Data from Multiple Clients
   - Expected Outcome: Data is correctly aggregated and stored.

### 4. ClientHandler
- **Test Case 4**: Handle GET Request for Existing Data
   - Expected Outcome: Correct data is returned with a 200 status.
- **Test Case 5**: Handle GET Request for Non-existing Data
   - Expected Outcome: 404 error returned for non-existing station ID.
- **Test Case 6**: Handle POST Request to Add Data
   - Expected Outcome: Data is successfully added, and 200 status returned.

### 5. FileManager
- **Test Case 7**: Writing Data to File
   - Expected Outcome: Data is written correctly, and no race conditions occur.
- **Test Case 8**: Reading Data from File
   - Expected Outcome: Correct data is retrieved from the file.
- **Test Case 9**: Handle Expired Files Cleanup
   - Expected Outcome: Expired files are cleaned up, and logs reflect this.

## Unit Testing for Utility Classes

### 1. HttpReader
- **Test Case 12**: Valid GET Request Handling
   - Expected Outcome: GET request processed correctly, and response sent.
- **Test Case 13**: Invalid Request Handling
   - Expected Outcome: 400 error returned for malformed requests.

### 2. HttpWriter
- **Test Case 12**: Valid GET Request Handling
   - Expected Outcome: GET request processed correctly, and response sent.
- **Test Case 13**: Invalid Request Handling
   - Expected Outcome: 400 error returned for malformed requests.

### 2. WeatherDataSerializer
- **Test Case 14**: JSON to WeatherData Object Parsing
   - Expected Outcome: JSON data correctly parsed into WeatherData object.
- **Test Case 15**: WeatherData Object to JSON Conversion
   - Expected Outcome: WeatherData object serialized back into correct JSON format.
