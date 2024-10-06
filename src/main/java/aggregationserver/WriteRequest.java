package aggregationserver;

import common.WeatherDataSerializer;

public record WriteRequest(long timestamp, String stationId, WeatherDataSerializer data) {
}