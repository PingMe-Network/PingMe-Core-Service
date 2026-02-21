package org.ping_me.service.weather;

import org.ping_me.dto.response.weather.WeatherResponse;

public interface WeatherService {
    WeatherResponse getWeather(double lat, double lon);
}
