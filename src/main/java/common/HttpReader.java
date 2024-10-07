package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpReader {

  private String method;
  private String path;
  private String httpVersion;
  private Map<String, String> headers;
  private String body;

  // Getters for accessing the parsed data

  public String getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public String getHttpVersion() {
    return httpVersion;
  }

  public String getHeader(String headerName) {
    return headers.get(headerName);
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getBody() {
    return body;
  }

  public HttpReader(BufferedReader reader) throws IOException {
    headers = new HashMap<>();
    parseRequest(reader);
  }

  public void parseRequest(BufferedReader in) throws IOException {
    String line = in.readLine();

    if (line == null) {
      throw new IOException("No data in the request. BufferedReader returned null.");
    }

    // First line should be (method, path, version)
    String[] requestLine = line.split(" ");
    if (requestLine.length == 3) {
      method = requestLine[0];
      path = requestLine[1];
      httpVersion = requestLine[2];
    }

    // Read headers
    while ((line = in.readLine()) != null && !line.isEmpty()) {
      String[] header = line.split(":", 2);
      if (header.length == 2) {
        headers.put(header[0].trim(), header[1].trim());
      }
    }

    // If Content-Length is in the header, read the body
    if (headers.containsKey("Content-Length")) {
      int contentLength = Integer.parseInt(headers.get("Content-Length"));
      char[] bodyChars = new char[contentLength];
      in.read(bodyChars, 0, contentLength);
      body = new String(bodyChars);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(method).append(" ").append(path).append(" ").append(httpVersion).append("\n");
    for (Map.Entry<String, String> header : headers.entrySet()) {
      result.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
    }
    result.append("\n").append(body);
    return result.toString();
  }
}