package com.couragegang.bff.iam;

import static org.assertj.core.api.Assertions.assertThat;

import com.couragegang.bff.config.BffProperties;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IamUserProxyClientTest {

    MockWebServer server;
    IamUserProxyClient client;

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.start();
        var props = new BffProperties();
        props.setIamBaseUrl(server.url("/v1/iam/").toString());
        client = new IamUserProxyClient(props);
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    @Test
    void forwardGet() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"id\":\"u1\"}"));
        var res = client.forwardGet("/me", Optional.of("Bearer t"));
        assertThat(res.getStatus().getCode()).isEqualTo(200);
        assertThat(server.takeRequest().getPath()).isEqualTo("/v1/iam/me");
    }

    @Test
    void forwardPostPatchDelete() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        assertThat(client.forwardPost("/orgs", "{}", Optional.empty()).getStatus().getCode()).isEqualTo(201);

        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        assertThat(client.forwardPatch("/me", "{}", Optional.of("Bearer x")).getStatus().getCode()).isEqualTo(200);

        server.enqueue(new MockResponse().setResponseCode(204).setBody(""));
        assertThat(client.forwardDelete("/invites/x", Optional.empty()).getStatus().getCode()).isEqualTo(204);
    }
}
