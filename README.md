
# Assignment 2

TODO: Description 
### Bonus Marks - JSON Parsing

My JSON parser for the bonus marks is within the common/WeatherDataSerializer class. 

## How to Build the Project

To compile the project, run the following Maven command:

```bash
mvn compile
```

## How to Run the Project

### Running with Maven (Verbose Output)

You can run the `TestJackson` class directly using Maven:

```bash
mvn exec:java -Dexec.mainClass="TestJackson"
```

### Running with Maven (Quiet Mode)

To minimize the output from Maven while running the program, use the `-q` (quiet) option:

```bash
mvn -q exec:java -Dexec.mainClass="TestJackson"
```

### Running the Compiled Class Directly

After compiling the project, you can run the compiled class directly using the `java` command:

1. Compile the project:

   ```bash
   mvn compile
   ```

2. Navigate to the `target/classes` directory:

   ```bash
   cd target
   ```

3. Run the `TestJackson` class:

   ```bash
   java test-classes/ContentServerTest
   ```

### Creating and Running a Runnable JAR

You can package the application into a runnable JAR file and execute it:

1. Package the project into a JAR file:

   ```bash
   mvn clean package
   ```

2. Run the JAR file using the `java -jar` command:

   ```bash
   java -jar target/yourproject-1.0-SNAPSHOT.jar
   ```

   This will run the application directly with minimal output.

## Testing Documentation

- Ensure that your `pom.xml` file is correctly configured with the necessary dependencies and the `maven-jar-plugin` for packaging the project into a runnable JAR.
- The quiet mode is useful when you want to see only the essential output from your application, suppressing the usual Maven build logs.

## Changes from draft 

In the draft I only had two servers connected through sockets which were able to send and receive http text data. Since then I have Implemneted all other functionallity 