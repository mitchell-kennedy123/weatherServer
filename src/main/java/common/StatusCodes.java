package common;

public class StatusCodes {

  public static final int OK = 200;
  public static final int CREATED = 201;
  public static final int NO_CONTENT = 204;
  public static final int BAD_REQUEST = 400;
  public static final int INTERNAL_SERVER_ERROR = 500;

  // Method to get a status code based on the status code
  public static String getStatusText(int statusCode) {
    return switch (statusCode) {
      case OK -> "OK";
      case CREATED -> "CREATED";
      case NO_CONTENT -> "NO_CONTENT";
      case BAD_REQUEST -> "BAD_REQUEST";
      case INTERNAL_SERVER_ERROR -> "INTERNAL_SERVER_ERROR";
      default -> "UNKNOWN_STATUS_CODE";
    };
  }

  // Method to get a status message based on the status code
  public static String getStatusMessage(int statusCode) {
    return switch (statusCode) {
      case OK -> "OK - Successful PUT/GET request";
      case CREATED -> "Created - First successful PUT from a Content Server";
      case NO_CONTENT -> "No Content - Empty PUT request";
      case BAD_REQUEST -> "Bad Request - Invalid request method";
      case INTERNAL_SERVER_ERROR -> "Internal Server Error - Invalid JSON data";
      default -> "Unknown Status Code";
    };
  }
}