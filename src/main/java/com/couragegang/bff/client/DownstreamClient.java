package com.couragegang.bff.client;

import io.micronaut.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class DownstreamClient {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public HttpResponse<String> get(String url) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15)).GET().build();
        var response = http.send(request, BodyHandlers.ofString());
        return HttpResponse.status(io.micronaut.http.HttpStatus.valueOf(response.statusCode()))
                .body(response.body());
    }

    public HttpResponse<String> post(String url, String body) throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}", StandardCharsets.UTF_8))
                        .build();
        var response = http.send(request, BodyHandlers.ofString());
        return HttpResponse.status(io.micronaut.http.HttpStatus.valueOf(response.statusCode()))
                .body(response.body());
    }

    public HttpResponse<String> patch(String url, String body) throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json")
                        .method(
                                "PATCH",
                                HttpRequest.BodyPublishers.ofString(
                                        body != null ? body : "{}", StandardCharsets.UTF_8))
                        .build();
        var response = http.send(request, BodyHandlers.ofString());
        return HttpResponse.status(io.micronaut.http.HttpStatus.valueOf(response.statusCode()))
                .body(response.body());
    }
}
