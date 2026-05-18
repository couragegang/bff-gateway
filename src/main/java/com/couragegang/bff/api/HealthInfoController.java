package com.couragegang.bff.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import java.util.Map;

@Controller
public final class HealthInfoController {

    @Get("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "bff-gateway",
                "health", "/v1/bff/health",
                "metrics", "/v1/bff/metrics"
        );
    }
}
