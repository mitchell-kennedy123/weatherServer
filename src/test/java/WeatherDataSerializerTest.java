import common.WeatherDataSerializer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WeatherDataSerializerTest {

    // Helper method to create an example common.WeatherDataSerializer object
    private WeatherDataSerializer createExampleWeatherData() {
        WeatherDataSerializer weatherDataSerializer = new WeatherDataSerializer();
        weatherDataSerializer.getData().put("id", "IDS60901");
        weatherDataSerializer.getData().put("name", "Adelaide (West Terrace / ngayirdapira)");
        weatherDataSerializer.getData().put("state", "SA");
        weatherDataSerializer.getData().put("time_zone", "CST");
        weatherDataSerializer.getData().put("lat", "-34.9");
        weatherDataSerializer.getData().put("lon", "138.6");
        weatherDataSerializer.getData().put("local_date_time", "15/04:00pm");
        weatherDataSerializer.getData().put("local_date_time_full", "20230715160000");
        weatherDataSerializer.getData().put("air_temp", "13.3");
        weatherDataSerializer.getData().put("apparent_t", "9.5");
        weatherDataSerializer.getData().put("cloud", "Partly cloudy");
        weatherDataSerializer.getData().put("dewpt", "5.7");
        weatherDataSerializer.getData().put("press", "1023.9");
        weatherDataSerializer.getData().put("rel_hum", "60");
        weatherDataSerializer.getData().put("wind_dir", "S");
        weatherDataSerializer.getData().put("wind_spd_kmh", "15");
        weatherDataSerializer.getData().put("wind_spd_kt", "8");
        return weatherDataSerializer;
    }

    @Test
    void writeJson() {
        // Arrange
        WeatherDataSerializer weatherDataSerializer = createExampleWeatherData();

        // Act
        String json = weatherDataSerializer.toJson();

        // Assert
        String expectedJson = "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace / ngayirdapira)\","
                + "\"state\":\"SA\",\"time_zone\":\"CST\",\"lat\":\"-34.9\",\"lon\":\"138.6\","
                + "\"local_date_time\":\"15/04:00pm\",\"local_date_time_full\":\"20230715160000\","
                + "\"air_temp\":\"13.3\",\"apparent_t\":\"9.5\",\"cloud\":\"Partly cloudy\",\"dewpt\":\"5.7\","
                + "\"press\":\"1023.9\",\"rel_hum\":\"60\",\"wind_dir\":\"S\",\"wind_spd_kmh\":\"15\","
                + "\"wind_spd_kt\":\"8\"}";

        assertEquals(expectedJson, json);
    }

    @Test
    void readJson() {
        // Arrange
        String jsonInput = "{\"id\":\"IDS60901\",\"name\":\"Adelaide\",\"state\":\"SA\"}";

        // Act
        WeatherDataSerializer fromJson = WeatherDataSerializer.extractDataFromJson(jsonInput);

        // Assert
        assertEquals("IDS60901", fromJson.getData().get("id"));
        assertEquals("Adelaide", fromJson.getData().get("name"));
        assertEquals("SA", fromJson.getData().get("state"));
    }

    @Test
    void writeTxt() {
        // Arrange
        WeatherDataSerializer weatherDataSerializer = createExampleWeatherData();

        // Act
        String txt = weatherDataSerializer.toTxt();

        // Assert
        String expectedTxt = "id:IDS60901\n" +
                "name:Adelaide (West Terrace / ngayirdapira)\n" +
                "state:SA\n" +
                "time_zone:CST\n" +
                "lat:-34.9\n" +
                "lon:138.6\n" +
                "local_date_time:15/04:00pm\n" +
                "local_date_time_full:20230715160000\n" +
                "air_temp:13.3\n" +
                "apparent_t:9.5\n" +
                "cloud:Partly cloudy\n" +
                "dewpt:5.7\n" +
                "press:1023.9\n" +
                "rel_hum:60\n" +
                "wind_dir:S\n" +
                "wind_spd_kmh:15\n" +
                "wind_spd_kt:8\n";
        assertEquals(expectedTxt, txt);
    }

    @Test
    void readTxt() {
        // Arrange
        String txtInput = "id:IDS60901\nname:Adelaide\nstate:SA\n";

        // Act
        WeatherDataSerializer fromTxt = WeatherDataSerializer.extractDataFromTxt(txtInput);

        // Assert
        assertEquals("IDS60901", fromTxt.getData().get("id"));
        assertEquals("Adelaide", fromTxt.getData().get("name"));
        assertEquals("SA", fromTxt.getData().get("state"));
    }
}