package com.couragegang.bff.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BffPropertiesTest {

    @Test
    void defaultsAndSetters() {
        var props = new BffProperties();
        props.setIamBaseUrl("http://iam/");
        props.setPolicyBaseUrl("http://policy/");
        props.setAuditBaseUrl("http://audit/");
        props.setKnowledgeBaseUrl("http://knowledge/");
        props.setConfigBaseUrl("http://config/");

        assertThat(props.getIamBaseUrl()).endsWith("/");
        assertThat(props.getPolicyBaseUrl()).isNotBlank();
        assertThat(props.getConfigBaseUrl()).contains("config");
    }
}
