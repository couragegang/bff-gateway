package com.couragegang.bff.iam;

import static org.assertj.core.api.Assertions.assertThat;

import com.couragegang.bff.config.BffProperties;
import com.couragegang.bff.metrics.OutboundHttpMetrics;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IamIntrospectClientTest {

    HttpServer server;
    String baseUrl;
    int port;
    OutboundHttpMetrics metrics;

    @BeforeEach
    void initMetrics() {
        metrics = new OutboundHttpMetrics(new SimpleMeterRegistry());
    }

    private IamIntrospectClient client(BffProperties props) {
        return new IamIntrospectClient(props, metrics);
    }

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        baseUrl = "http://127.0.0.1:" + port + "/v1/iam";
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void unreachableIamReturnsEmpty() {
        var props = new BffProperties();
        props.setIamBaseUrl("http://127.0.0.1:1");
        assertThat(client(props).introspect("bad")).isEmpty();
    }

    @Test
    void introspectActiveToken() {
        server.createContext(
                "/v1/iam/internal/token/introspect",
                exchange -> {
                    var body =
                            """
                            {"active":true,"sub":"user-1","orgId":"org-1","groupId":"g1","workspaceId":"ws1","permissions":["read"]}
                            """
                                    .trim();
                    exchange.sendResponseHeaders(200, body.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                });

        var props = new BffProperties();
        props.setIamBaseUrl(baseUrl);
        var result = client(props).introspect("token");

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo("user-1");
        assertThat(result.get().orgId()).isEqualTo("org-1");
        assertThat(result.get().groupId()).isEqualTo("g1");
        assertThat(result.get().workspaceId()).isEqualTo("ws1");
        assertThat(result.get().permissions()).containsExactly("read");
    }

    @Test
    void introspectBlankSubReturnsEmpty() {
        server.createContext(
                "/v1/iam/internal/token/introspect",
                exchange -> {
                    var body = "{\"active\":true,\"sub\":\"  \"}";
                    exchange.sendResponseHeaders(200, body.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                });

        var props = new BffProperties();
        props.setIamBaseUrl(baseUrl + "/");
        assertThat(client(props).introspect("t")).isEmpty();
    }

    @Test
    void introspectInactiveReturnsEmpty() {
        server.createContext(
                "/v1/iam/internal/token/introspect",
                exchange -> {
                    var body = "{\"active\":false}";
                    exchange.sendResponseHeaders(200, body.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                });

        var props = new BffProperties();
        props.setIamBaseUrl(baseUrl);
        assertThat(client(props).introspect("t")).isEmpty();
    }

    @Test
    void introspectNonArrayPermissions() {
        server.createContext(
                "/v1/iam/internal/token/introspect",
                exchange -> {
                    var body = "{\"active\":true,\"sub\":\"u1\",\"permissions\":\"read\"}";
                    exchange.sendResponseHeaders(200, body.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                });

        var props = new BffProperties();
        props.setIamBaseUrl(baseUrl);
        var result = client(props).introspect("t");

        assertThat(result).isPresent();
        assertThat(result.get().permissions()).isEmpty();
    }

    @Test
    void introspectHttpErrorReturnsEmpty() {
        server.createContext(
                "/v1/iam/internal/token/introspect",
                exchange -> exchange.sendResponseHeaders(500, -1));

        var props = new BffProperties();
        props.setIamBaseUrl(baseUrl);
        assertThat(client(props).introspect("t")).isEmpty();
    }
}
