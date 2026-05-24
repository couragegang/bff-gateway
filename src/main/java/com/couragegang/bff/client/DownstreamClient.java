package com.couragegang.bff.client;

import com.couragegang.bff.metrics.OutboundHttpMetrics;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Singleton
public final class DownstreamClient {

    private final HttpClient http;
    private final OutboundHttpMetrics metrics;

    public DownstreamClient(OutboundHttpMetrics metrics) {
        this.metrics = metrics;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public HttpResponse<String> get(String url, String integration, String operation) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15)).GET().build();
        var response = metrics.send(http, request, integration, operation);
        return HttpResponse.status(io.micronaut.http.HttpStatus.valueOf(response.statusCode()))
                .body(response.body());
    }

    public HttpResponse<String> post(String url, String body, String integration, String operation)
            throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}", StandardCharsets.UTF_8))
                        .build();
        var response = metrics.send(http, request, integration, operation);
        return HttpResponse.status(io.micronaut.http.HttpStatus.valueOf(response.statusCode()))
                .body(response.body());
    }

    public HttpResponse<String> patch(String url, String body, String integration, String operation)
            throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json")
                        .method(
                                "PATCH",
                                HttpRequest.BodyPublishers.ofString(
                                        body != null ? body : "{}", StandardCharsets.UTF_8))
                        .build();
        var response = metrics.send(http, request, integration, operation);
        return HttpResponse.status(io.micronaut.http.HttpStatus.valueOf(response.statusCode()))
                .body(response.body());
    }

    public HttpResponse<String> delete(String url, String integration, String operation) throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .DELETE()
                        .build();
        var response = metrics.send(http, request, integration, operation);
        return HttpResponse.status(io.micronaut.http.HttpStatus.valueOf(response.statusCode()))
                .body(response.body());
    }
}
