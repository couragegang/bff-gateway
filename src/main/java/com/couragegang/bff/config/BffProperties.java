package com.couragegang.bff.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("bff")
public class BffProperties {

    private String iamBaseUrl = "http://localhost:8080/v1/iam";

    public String getIamBaseUrl() {
        return iamBaseUrl;
    }

    public void setIamBaseUrl(String iamBaseUrl) {
        this.iamBaseUrl = iamBaseUrl;
    }
}
