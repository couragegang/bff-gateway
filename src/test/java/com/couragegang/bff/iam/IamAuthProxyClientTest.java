package com.couragegang.bff.iam;

import static org.assertj.core.api.Assertions.assertThat;

import com.couragegang.bff.config.BffProperties;
import io.micronaut.http.HttpStatus;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IamAuthProxyClientTest {

    MockWebServer server;

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    private IamAuthProxyClient client(String iamBaseUrl) {
        var props = new BffProperties();
        props.setIamBaseUrl(iamBaseUrl);
        return new IamAuthProxyClient(props);
    }

    @Test
    void forwardGetProxiesPathAndQuery() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"ok\":true}"));
        var iamBase = server.url("/v1/iam/").toString();

        var response = client(iamBase).forwardGet("/register", "x=1", Optional.empty());

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.OK.getCode());
        assertThat(response.body()).contains("ok");
        var req = server.takeRequest();
        assertThat(req.getPath()).contains("/v1/iam/auth/register");
        assertThat(req.getPath()).contains("x=1");
    }

    @Test
    void forwardGetStripsLeadingSlashFromSubPath() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(204));
        var iamBase = server.url("/v1/iam").toString();

        var response = client(iamBase).forwardGet("login", null, Optional.empty());

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.NO_CONTENT.getCode());
        assertThat(server.takeRequest().getPath()).contains("/auth/login");
    }

    @Test
    void forwardPostSendsBodyAndAuthorization() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(201).addHeader("Location", "http://iam/callback"));
        var iamBase = server.url("/v1/iam").toString();

        var response =
                client(iamBase).forwardPost("register", "", "{\"email\":\"a@b.c\"}", Optional.of("Bearer tok"));

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.CREATED.getCode());
        assertThat(response.getHeaders().get("Location", String.class)).contains("http://iam/callback");
        var req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getHeader("Authorization")).isEqualTo("Bearer tok");
    }

    @Test
    void forwardPostUsesEmptyJsonWhenBodyNull() throws Exception {
        server.enqueue(new MockResponse().setBody("{}"));
        var iamBase = server.url("/v1/iam").toString();

        var response = client(iamBase).forwardPost("refresh", null, null, Optional.of(""));

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.OK.getCode());
        assertThat(server.takeRequest().getBody().readUtf8()).isEqualTo("{}");
    }
}
