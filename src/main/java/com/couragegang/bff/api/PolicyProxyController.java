package com.couragegang.bff.api;

import com.couragegang.bff.client.DownstreamClient;
import com.couragegang.bff.config.BffProperties;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/policy")
public class PolicyProxyController {

    private final String policyBase;
    private final DownstreamClient http;

    public PolicyProxyController(BffProperties props, DownstreamClient http) {
        this.policyBase = trim(props.getPolicyBaseUrl());
        this.http = http;
    }

    @Get("/orgs/{orgId}/rules")
    public HttpResponse<String> rules(@PathVariable String orgId) throws Exception {
        return forward(http.get(policyBase + "/orgs/" + orgId + "/rules", "policy", "list_rules"));
    }

    @Get("/orgs/{orgId}/pending-approvals")
    public HttpResponse<String> pending(
            @PathVariable String orgId, @QueryValue("workspace_id") @Nullable String workspaceId)
            throws Exception {
        var url = policyBase + "/orgs/" + orgId + "/pending-approvals";
        if (workspaceId != null && !workspaceId.isBlank()) {
            url += "?workspace_id=" + workspaceId;
        }
        return forward(http.get(url, "policy", "list_pending"));
    }

    @Get("/pending-approvals/{id}")
    public HttpResponse<String> pendingGet(@PathVariable String id) throws Exception {
        return forward(http.get(policyBase + "/pending-approvals/" + id, "policy", "get_pending"));
    }

    @Post("/pending-approvals/{id}/approve")
    public HttpResponse<String> approve(@PathVariable String id, @Body @Nullable String body) throws Exception {
        return forward(
                http.post(
                        policyBase + "/pending-approvals/" + id + "/approve",
                        body != null ? body : "{}",
                        "policy",
                        "approve"));
    }

    @Post("/pending-approvals/{id}/reject")
    public HttpResponse<String> reject(@PathVariable String id, @Body @Nullable String body) throws Exception {
        return forward(
                http.post(
                        policyBase + "/pending-approvals/" + id + "/reject",
                        body != null ? body : "{}",
                        "policy",
                        "reject"));
    }

    private static HttpResponse<String> forward(HttpResponse<String> downstream) {
        if (downstream.getStatus().getCode() >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }

    private static String trim(String base) {
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
