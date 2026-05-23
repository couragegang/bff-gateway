package com.couragegang.bff.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.couragegang.bff.config.BffProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigProxyControllerTest {

    MockWebServer server;
    ConfigProxyController controller;

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.start();
        var props = new BffProperties();
        props.setConfigBaseUrl(server.url("/v1/config").toString());
        controller = new ConfigProxyController(props);
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    @Test
    void listForwardsToConfig() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"items\":[]}"));

        var res = controller.list("org-1", null, 50);

        assertThat(res.getStatus().getCode()).isEqualTo(200);
        assertThat(server.takeRequest().getPath()).contains("/orgs/org-1/workspaces");
    }

    @Test
    void createForwardsBody() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(201).setBody("{\"slug\":\"x\"}"));

        var res = controller.create("org-1", "{\"name\":\"W\",\"slug\":\"x\",\"groupId\":\"g1\"}");

        assertThat(res.getStatus().getCode()).isEqualTo(201);
        assertThat(server.takeRequest().getMethod()).isEqualTo("POST");
    }
}
