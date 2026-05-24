package com.couragegang.bff.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Проверяет, что BFF не отдаёт 404 на маршруты UI.
 * Без JWT ожидаем 401 (маршрут есть), а не 404.
 */
@MicronautTest(startApplication = true, environments = "test")
class BffUiRoutesCoverageTest {

    @Inject
    @Client("/v1/bff")
    HttpClient client;

    @Test
    void allUiRoutesExistInBff() {
        for (var route : BffUiRoutes.ALL) {
            if (route.path().startsWith("/api/auth/")) {
                continue;
            }
            var method = HttpMethod.valueOf(route.method());
            var path = samplePath(route.path());
            var request =
                    HttpRequest.create(method, path).body(method == HttpMethod.GET || method == HttpMethod.DELETE ? null : "{}");
            int status = exchangeStatus(request);
            assertThat(status)
                    .as("%s %s → HTTP %d (404 = маршрут не зарегистрирован в BFF)", route.method(), route.path(), status)
                    .isNotEqualTo(HttpStatus.NOT_FOUND.getCode());
        }
    }

    private int exchangeStatus(HttpRequest<?> request) {
        try {
            return client.toBlocking().exchange(request, String.class).getStatus().getCode();
        } catch (HttpClientResponseException e) {
            return e.getStatus().getCode();
        }
    }

    private static String samplePath(String pattern) {
        return pattern
                .replace("{orgId}", "00000000-0000-0000-0000-000000000001")
                .replace("{workspaceId}", "00000000-0000-0000-0000-000000000002")
                .replace("{conversationId}", "00000000-0000-0000-0000-000000000003")
                .replace("{installationId}", "00000000-0000-0000-0000-000000000004")
                .replace("{connectorKey}", "notion")
                .replace("{inviteId}", "00000000-0000-0000-0000-000000000005")
                .replace("{id}", "00000000-0000-0000-0000-000000000006");
    }
}
