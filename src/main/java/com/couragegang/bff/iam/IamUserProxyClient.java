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

/** Прокси IAM для аутентифицированных маршрутов (/me, /organizations/*). */
@Singleton
public class IamUserProxyClient {

    private final String iamBase;
    private final HttpClient http;

    public IamUserProxyClient(BffProperties props) {
        var base = props.getIamBaseUrl();
        this.iamBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public HttpResponse<String> forwardGet(String path, Optional<String> authorization) throws Exception {
        return forward("GET", path, null, authorization);
    }

    public HttpResponse<String> forwardPost(String path, String body, Optional<String> authorization) throws Exception {
        return forward("POST", path, body, authorization);
    }

    public HttpResponse<String> forwardPatch(String path, String body, Optional<String> authorization) throws Exception {
        return forward("PATCH", path, body, authorization);
    }

    public HttpResponse<String> forwardDelete(String path, Optional<String> authorization) throws Exception {
        return forward("DELETE", path, null, authorization);
    }

    private HttpResponse<String> forward(String method, String path, String body, Optional<String> authorization)
            throws Exception {
        var normalized = path.startsWith("/") ? path : "/" + path;
        var builder =
                HttpRequest.newBuilder(URI.create(iamBase + normalized)).timeout(Duration.ofSeconds(30));
        authorization.filter(a -> !a.isBlank()).ifPresent(a -> builder.header("Authorization", a));
        switch (method) {
            case "POST" -> builder.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}", StandardCharsets.UTF_8));
            case "PATCH" -> builder.header("Content-Type", "application/json")
                    .method(
                            "PATCH",
                            HttpRequest.BodyPublishers.ofString(
                                    body != null ? body : "{}", StandardCharsets.UTF_8));
            case "DELETE" -> builder.DELETE();
            default -> builder.GET();
        }
        var response = http.send(builder.build(), BodyHandlers.ofString());
        return HttpResponse.status(HttpStatus.valueOf(response.statusCode())).body(response.body());
    }
}
