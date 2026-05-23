package com.couragegang.bff.iam;

import com.couragegang.bff.config.BffProperties;
import com.couragegang.bff.metrics.OutboundHttpMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IamIntrospectClient {

    private static final Logger LOG = LoggerFactory.getLogger(IamIntrospectClient.class);

    private final String introspectUrl;
    private final HttpClient http;
    private final OutboundHttpMetrics metrics;
    private final ObjectMapper json;

    public IamIntrospectClient(BffProperties props, OutboundHttpMetrics metrics) {
        var base = props.getIamBaseUrl().endsWith("/") ? props.getIamBaseUrl().substring(0, props.getIamBaseUrl().length() - 1) : props.getIamBaseUrl();
        this.introspectUrl = base + "/internal/token/introspect";
        this.metrics = metrics;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.json = new ObjectMapper();
    }

    public Optional<IntrospectResult> introspect(String bearerToken) {
        try {
            var body = json.writeValueAsString(java.util.Map.of("token", bearerToken));
            var request =
                    HttpRequest.newBuilder(URI.create(introspectUrl))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                            .build();
            var response = metrics.send(http, request, "iam", "introspect");
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOG.warn("iam introspect http {}: {}", response.statusCode(), response.body());
                return Optional.empty();
            }
            JsonNode node = json.readTree(response.body());
            if (!node.path("active").asBoolean(false)) {
                return Optional.empty();
            }
            var sub = textOrNull(node, "sub");
            if (sub == null || sub.isBlank()) {
                return Optional.empty();
            }
            List<String> permissions = new ArrayList<>();
            var perms = node.path("permissions");
            if (perms.isArray()) {
                perms.forEach(p -> permissions.add(p.asText()));
            }
            return Optional.of(
                    new IntrospectResult(
                            sub,
                            textOrNull(node, "orgId"),
                            textOrNull(node, "groupId"),
                            textOrNull(node, "workspaceId"),
                            permissions));
        } catch (Exception e) {
            LOG.warn("iam introspect failed: {}", e.toString());
            return Optional.empty();
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        var v = node.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }

    public record IntrospectResult(
            String userId, String orgId, String groupId, String workspaceId, List<String> permissions) {}
}
