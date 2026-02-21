package org.ping_me.service.weather.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.ping_me.dto.response.weather.WeatherResponse;
import org.ping_me.service.weather.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeatherServiceImpl implements WeatherService {

    @Value("${weather.api.base-url}")
    @NonFinal
    String baseUrl;

    @Value("${weather.api.key}")
    @NonFinal
    String apiKey;

    @Value("${weather.api.units}")
    @NonFinal
    String units;

    RestTemplate restTemplate = new RestTemplate();

    @Override
    public WeatherResponse getWeather(double lat, double lon) {

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", units)
                .toUriString();

        return restTemplate.getForObject(url, WeatherResponse.class);
    }
}
