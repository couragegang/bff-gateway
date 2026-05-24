package com.couragegang.bff.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.couragegang.bff.config.BffProperties;
import com.couragegang.bff.iam.IamUserProxyClient;
import io.micronaut.http.HttpRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvitesProxyControllerTest {

    MockWebServer server;
    InvitesProxyController controller;

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.start();
        var props = new BffProperties();
        props.setIamBaseUrl(server.url("/v1/iam").toString());
        controller = new InvitesProxyController(new IamUserProxyClient(props));
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    @Test
    void acceptForwardsToIam() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"ok\":true}"));

        var req = HttpRequest.POST("/api/invites/accept", "{\"token\":\"t\"}")
                .header("Authorization", "Bearer tok");
        var res = controller.accept("{\"token\":\"t\"}", req);

        assertThat(res.getStatus().getCode()).isEqualTo(200);
        assertThat(server.takeRequest().getPath()).isEqualTo("/v1/iam/invites/accept");
    }
}
