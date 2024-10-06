package common;

import java.util.LinkedHashMap;
import java.util.Map;

public class WeatherDataSerializer {
  private Map<String, String> data;

  // Constructor to initialize the data map
  public WeatherDataSerializer() {
    data = new LinkedHashMap<>();
  }

  public Map<String, String> getData() {
    return data;
  }

  public void setData(Map<String, String> data) {
    this.data = data;
  }

  // Method to convert the data into a JSON-like string manually
  public String toJson() {
    StringBuilder jsonBuilder = new StringBuilder();
    jsonBuilder.append("{\n");
    int entryCount = 0;
    for (Map.Entry<String, String> entry : data.entrySet()) {
      jsonBuilder.append("\t\"").append(entry.getKey()).append("\" : ")
              .append("\"").append(entry.getValue()).append("\"");
      entryCount++;
      if (entryCount < data.size()) {
        jsonBuilder.append(",").append("\n");
      }
    }
    jsonBuilder.append("\n}");
    return jsonBuilder.toString();
  }

  // Method to populate the data map from a JSON string manually
  public static WeatherDataSerializer extractDataFromJson(String jsonString) {
    WeatherDataSerializer weatherDataSerializer = new WeatherDataSerializer();
    jsonString = jsonString.trim();
    if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
      jsonString = jsonString.substring(1, jsonString.length() - 1); // Remove the braces
      String[] pairs = jsonString.split(",");
      for (String pair : pairs) {
        String[] keyValue = pair.split(":");
        if (keyValue.length == 2) {
          String key = keyValue[0].replace("\"", "").trim();
          String value = keyValue[1].replace("\"", "").trim();
          weatherDataSerializer.data.put(key, value);
        }
      }
    }
    return weatherDataSerializer;
  }

  // Method to convert the weather data to the text format manually
  public String toTxt() {
    StringBuilder txtBuilder = new StringBuilder();
    for (Map.Entry<String, String> entry : data.entrySet()) {
      txtBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
    }
    return txtBuilder.toString();
  }

  // Method to populate the data map from a text string manually
  public static WeatherDataSerializer extractDataFromTxt(String txtString) {
    WeatherDataSerializer weatherDataSerializer = new WeatherDataSerializer();
    String[] lines = txtString.split("\n");
    for (String line : lines) {
      String[] keyValue = line.split(":");
      if (keyValue.length == 2) {
        String key = keyValue[0].trim();
        String value = keyValue[1].trim();
        weatherDataSerializer.data.put(key, value);
      }
    }
    return weatherDataSerializer;
  }
}