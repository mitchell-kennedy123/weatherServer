import common.WeatherDataSerializer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

/// Test file for the JSON parser
public class WeatherDataSerializerTest {

    /**
     * Test the conversion of data to a JSON string.
     */
    @Test
    public void testToJson() {
        WeatherDataSerializer weatherDataSerializer = new WeatherDataSerializer();

        Map<String, String> data = new LinkedHashMap<>();
        data.put("id", "IDS60901");
        data.put("air_temp", "13.3");
        weatherDataSerializer.setData(data);

        // Convert to JSON string
        String jsonString = weatherDataSerializer.toJson();

        // Expected JSON string
        String expectedJson = "{\n" +
                "\t\"id\" : \"IDS60901\",\n" +
                "\t\"air_temp\" : \"13.3\"\n" +
                "}";

        assertEquals(expectedJson, jsonString, "JSON string should match the expected format.");
    }

    /**
     * Test extracting data from a JSON string.
     */
    @Test
    public void testExtractDataFromJson() {
        // Sample JSON input
        String jsonInput = "{\n" +
                "\t\"id\" : \"IDS60901\",\n" +
                "\t\"air_temp\" : \"13.3\"\n" +
                "}";

        // Extract data from JSON string
        WeatherDataSerializer weatherDataSerializer = WeatherDataSerializer.extractDataFromJson(jsonInput);
        Map<String, String> data = weatherDataSerializer.getData();

        assertEquals(2, data.size(), "Data map should contain 2 entries.");
        assertEquals("IDS60901", data.get("id"), "ID should match.");
        assertEquals("13.3", data.get("air_temp"), "Air temperature should match.");
    }

    /**
     * Test the conversion of data to text format.
     */
    @Test
    public void testToTxt() {
        WeatherDataSerializer weatherDataSerializer = new WeatherDataSerializer();

        Map<String, String> data = new LinkedHashMap<>();
        data.put("id", "IDS60901");
        data.put("air_temp", "13.3");
        weatherDataSerializer.setData(data);

        // Convert to text format
        String txtString = weatherDataSerializer.toTxt();

        // Expected text format
        String expectedTxt = "id:IDS60901\n" +
                "air_temp:13.3\n";

        assertEquals(expectedTxt, txtString, "Text format should match the expected format.");
    }

    /**
     * Test extracting data from a text string.
     */
    @Test
    public void testExtractDataFromTxt() {
        // Sample text input
        String txtInput = "id:IDS60901\n" +
                "air_temp:13.3\n";

        // Extract data from the text string
        WeatherDataSerializer weatherDataSerializer = WeatherDataSerializer.extractDataFromTxt(txtInput);
        Map<String, String> data = weatherDataSerializer.getData();

        assertEquals(2, data.size(), "Data map should contain 2 entries.");
        assertEquals("IDS60901", data.get("id"), "ID should match.");
        assertEquals("13.3", data.get("air_temp"), "Air temperature should match.");
    }
}