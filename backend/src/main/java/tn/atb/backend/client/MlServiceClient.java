package tn.atb.backend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tn.atb.backend.dto.ml.MlPredictionRequest;
import tn.atb.backend.dto.ml.MlPredictionResponse;
import tn.atb.backend.exception.BadRequestException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Talks to the FastAPI ML service over the JDK HTTP client.
 *
 * Two constraints shaped this class. HTTP/1.1 is forced because the JDK client
 * otherwise sends an HTTP/2 (h2c) upgrade header that uvicorn rejects, dropping the
 * request body. And (de)serialization goes through Jackson 3 explicitly rather than
 * Spring's RestClient message converters: this project carries two Jackson major
 * versions (Jackson 2 via jjwt, Jackson 3 via Spring Boot 4.1), which left RestClient
 * sending empty bodies.
 */
@Slf4j
@Component
public class MlServiceClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // The Python service speaks snake_case throughout, so one naming strategy covers
    // every field — no per-field annotations to keep in sync on either side.
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    private final String mlServiceUrl;

    public MlServiceClient(@Value("${ml.service.url}") String mlServiceUrl) {
        this.mlServiceUrl = mlServiceUrl;
    }

    public MlPredictionResponse predict(MlPredictionRequest request) {
        String requestJson = objectMapper.writeValueAsString(request);
        log.info("ML request payload: {}", requestJson);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(mlServiceUrl + "/predict"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("ML service call failed", ex);
            Thread.currentThread().interrupt();
            throw new BadRequestException(
                    "Le service d'analyse IA est indisponible. Vérifiez qu'il est démarré (uvicorn app.main:app --port 8000).");
        }

        if (response.statusCode() >= 400) {
            log.error("ML service rejected the request ({}): {}", response.statusCode(), response.body());
            throw new BadRequestException(
                    "Le service d'analyse IA a rejeté la requête (" + response.statusCode() + "): " + response.body());
        }

        try {
            return objectMapper.readValue(response.body(), MlPredictionResponse.class);
        } catch (Exception ex) {
            log.error("Could not read the ML service response: {}", response.body(), ex);
            throw new BadRequestException("Réponse inattendue du service d'analyse IA.");
        }
    }
}
