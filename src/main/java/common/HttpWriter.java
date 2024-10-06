package common;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpWriter {

  private String method;
  private String path;
  private String httpVersion;
  private Map<String, String> headers;
  private String body;

  public HttpWriter() {
    this.headers = new LinkedHashMap<>();
    this.httpVersion = "HTTP/1.1"; // Default to HTTP/1.1
  }

  public HttpWriter setMethod(String method) {
    this.method = method;
    return this;
  }

  public HttpWriter setPath(String path) {
    this.path = path;
    return this;
  }

  public HttpWriter setHttpVersion(String httpVersion) {
    this.httpVersion = httpVersion;
    return this;
  }

  public HttpWriter addHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  public HttpWriter setBody(String body) {
    this.body = body;
    if (body != null) {
      this.headers.put("Content-Length", String.valueOf(body.length()));
      this.headers.put("Content-Type", "application/json"); // Assuming JSON body
    }
    return this;
  }

  @Override
  public String toString() {
    StringBuilder request = new StringBuilder();

    // Append the request line
    request.append(method).append(" ").append(path).append(" ").append(httpVersion).append("\r\n");

    // Append the headers
    for (Map.Entry<String, String> header : headers.entrySet()) {
      request.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
    }

    // Append a blank line to separate headers from the body
    request.append("\r\n");

    // Append the body, if any
    if (body != null) {
      request.append(body);
    }

    return request.toString();
  }
}