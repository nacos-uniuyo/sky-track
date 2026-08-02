package com.skywatch.weather.service;

import com.skywatch.weather.config.WeatherApiProperties;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse;
import com.skywatch.weather.dto.weather.ForecastResponse;
import com.skywatch.weather.dto.weather.GeoCityResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherServiceTest {

    private MockRestServiceServer server;
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.openweathermap.org");
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        WeatherApiProperties properties = new WeatherApiProperties();
        properties.setBaseUrl("https://api.openweathermap.org");
        properties.setKey("test-key");

        weatherService = new WeatherService(restClient, properties);
    }

    @Test
    void currentWeatherParsesResponse() {
        String json = """
                {
                  "coord": {"lon": 3.3792, "lat": 6.5244},
                  "weather": [{"id": 800, "main": "Clear", "description": "clear sky", "icon": "01d"}],
                  "main": {
                    "temp": 28.4,
                    "feels_like": 29.1,
                    "temp_min": 27.5,
                    "temp_max": 29.3,
                    "pressure": 1012,
                    "humidity": 68
                  },
                  "visibility": 10000,
                  "wind": {"speed": 3.6, "deg": 180},
                  "sys": {"sunrise": 1754160000, "sunset": 1754200000},
                  "name": "Lagos",
                  "dt": 1754200000
                }
                """;

        server.expect(requestTo("https://api.openweathermap.org/data/2.5/weather?lat=6.5244&lon=3.3792&units=metric&appid=test-key"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        CurrentWeatherResponse response = weatherService.currentWeather(
                new BigDecimal("6.5244"), new BigDecimal("3.3792"));

        assertThat(response.name()).isEqualTo("Lagos");
        assertThat(response.main().temp()).isEqualByComparingTo("28.4");
        assertThat(response.main().humidity()).isEqualTo(68);
        assertThat(response.weather().get(0).description()).isEqualTo("clear sky");
        assertThat(response.wind().speed()).isEqualByComparingTo("3.6");
    }

    @Test
    void forecastParsesResponse() {
        String json = """
                {
                  "list": [
                    {
                      "dt": 1754200000,
                      "main": {"temp": 28.4, "feels_like": 29.1, "humidity": 68, "pressure": 1012},
                      "weather": [{"id": 800, "main": "Clear", "description": "clear sky", "icon": "01d"}],
                      "wind": {"speed": 3.6, "deg": 180},
                      "clouds": {"all": 5}
                    }
                  ],
                  "city": {"id": 2332459, "name": "Lagos", "country": "NG"}
                }
                """;

        server.expect(requestTo("https://api.openweathermap.org/data/2.5/forecast?lat=6.5244&lon=3.3792&units=metric&appid=test-key"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        ForecastResponse response = weatherService.forecast(
                new BigDecimal("6.5244"), new BigDecimal("3.3792"));

        assertThat(response.city().name()).isEqualTo("Lagos");
        assertThat(response.list()).hasSize(1);
        assertThat(response.list().get(0).main().temp()).isEqualByComparingTo("28.4");
    }

    @Test
    void searchCitiesParsesResponse() {
        String json = """
                [
                  {"name": "Lagos", "lat": 6.5244, "lon": 3.3792, "country": "NG", "state": "Lagos"}
                ]
                """;

        server.expect(requestTo("https://api.openweathermap.org/geo/1.0/direct?q=Lagos&limit=5&appid=test-key"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<GeoCityResponse> response = weatherService.searchCities("Lagos", 5);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("Lagos");
        assertThat(response.get(0).country()).isEqualTo("NG");
        assertThat(response.get(0).lat()).isEqualByComparingTo("6.5244");
    }
}
