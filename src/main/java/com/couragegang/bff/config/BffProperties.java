package com.couragegang.bff.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("bff")
public class BffProperties {

    private String iamBaseUrl = "http://localhost:8080/v1/iam";
    private String policyBaseUrl = "http://localhost:8085/v1/policy";
    private String auditBaseUrl = "http://localhost:8086/v1/audit";
    private String knowledgeBaseUrl = "http://localhost:8088/v1/knowledge";
    private String configBaseUrl = "http://localhost:8084/v1/config";

    public String getIamBaseUrl() {
        return iamBaseUrl;
    }

    public void setIamBaseUrl(String iamBaseUrl) {
        this.iamBaseUrl = iamBaseUrl;
    }

    public String getPolicyBaseUrl() {
        return policyBaseUrl;
    }

    public void setPolicyBaseUrl(String policyBaseUrl) {
        this.policyBaseUrl = policyBaseUrl;
    }

    public String getAuditBaseUrl() {
        return auditBaseUrl;
    }

    public void setAuditBaseUrl(String auditBaseUrl) {
        this.auditBaseUrl = auditBaseUrl;
    }

    public String getKnowledgeBaseUrl() {
        return knowledgeBaseUrl;
    }

    public void setKnowledgeBaseUrl(String knowledgeBaseUrl) {
        this.knowledgeBaseUrl = knowledgeBaseUrl;
    }

    public String getConfigBaseUrl() {
        return configBaseUrl;
    }

    public void setConfigBaseUrl(String configBaseUrl) {
        this.configBaseUrl = configBaseUrl;
    }
}
