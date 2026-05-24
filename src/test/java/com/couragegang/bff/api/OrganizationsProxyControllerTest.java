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

class OrganizationsProxyControllerTest {

    MockWebServer server;
    OrganizationsProxyController controller;

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.start();
        var props = new BffProperties();
        props.setIamBaseUrl(server.url("/v1/iam").toString());
        controller = new OrganizationsProxyController(new IamUserProxyClient(props));
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    @Test
    void createForwardsToIam() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(201).setBody("{\"id\":\"o1\"}"));

        var req = HttpRequest.POST("/api/organizations", "{\"name\":\"A\"}")
                .header("Authorization", "Bearer tok");
        var res = controller.create("{\"name\":\"A\",\"slug\":\"a\"}", req);

        assertThat(res.getStatus().getCode()).isEqualTo(201);
        assertThat(server.takeRequest().getPath()).isEqualTo("/v1/iam/organizations");
    }
}
