package com.couragegang.bff.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthInfoControllerTest {

    @Test
    void rootContainsServiceName() {
        assertThat(new HealthInfoController().root().get("service")).isEqualTo("bff-gateway");
    }
}
