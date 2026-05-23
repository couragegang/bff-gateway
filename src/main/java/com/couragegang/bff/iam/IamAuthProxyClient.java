package com.couragegang.bff.iam;

import com.couragegang.bff.config.BffProperties;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/** Прокси IAM /auth/* для маршрутов BFF /api/auth/*. */
@Singleton
public class IamAuthProxyClient {

    private final String authBase;
    private final HttpClient http;

    public IamAuthProxyClient(BffProperties props) {
        var base = props.getIamBaseUrl().endsWith("/") ? props.getIamBaseUrl().substring(0, props.getIamBaseUrl().length() - 1) : props.getIamBaseUrl();
        this.authBase = base + "/auth";
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public HttpResponse<String> forwardGet(String subPath, String query, Optional<String> authorization) throws Exception {
        return forward("GET", subPath, query, null, authorization);
    }

    public HttpResponse<String> forwardPost(String subPath, String query, String body, Optional<String> authorization)
            throws Exception {
        return forward("POST", subPath, query, body, authorization);
    }

    private HttpResponse<String> forward(
            String method, String subPath, String query, String body, Optional<String> authorization)
            throws Exception {
        var path = subPath.startsWith("/") ? subPath.substring(1) : subPath;
        var url = authBase + (path.isBlank() ? "" : "/" + path);
        if (query != null && !query.isBlank()) {
            url = url + "?" + query;
        }
        var builder = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30));
        authorization.filter(a -> !a.isBlank()).ifPresent(a -> builder.header("Authorization", a));
        if ("POST".equals(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}", StandardCharsets.UTF_8));
        } else {
            builder.GET();
        }
        var response = http.send(builder.build(), BodyHandlers.ofString());
        var status = HttpStatus.valueOf(response.statusCode());
        var out = HttpResponse.status(status).body(response.body());
        response.headers().firstValue("location").ifPresent(loc -> out.header("Location", loc));
        return out;
    }
}
