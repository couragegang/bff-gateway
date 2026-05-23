package com.couragegang.bff.client;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DownstreamClientTest {

    MockWebServer server;
    DownstreamClient client = new DownstreamClient();

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    @Test
    void getReturnsBody() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"ok\":true}"));
        var url = server.url("/rules").toString();

        var res = client.get(url);

        assertThat(res.getStatus().getCode()).isEqualTo(200);
        assertThat(res.body()).contains("ok");
    }

    @Test
    void postSendsJson() throws Exception {
        server.enqueue(new MockResponse().setBody("{}"));
        var url = server.url("/approve").toString();

        var res = client.post(url, "{\"decidedByUserId\":null}");

        assertThat(res.getStatus().getCode()).isEqualTo(200);
        assertThat(server.takeRequest().getMethod()).isEqualTo("POST");
    }

    @Test
    void patchSendsJson() throws Exception {
        server.enqueue(new MockResponse().setBody("{}"));
        var url = server.url("/workspaces/ws-1").toString();

        var res = client.patch(url, "{\"name\":\"Renamed\"}");

        assertThat(res.getStatus().getCode()).isEqualTo(200);
        assertThat(server.takeRequest().getMethod()).isEqualTo("PATCH");
    }
}
