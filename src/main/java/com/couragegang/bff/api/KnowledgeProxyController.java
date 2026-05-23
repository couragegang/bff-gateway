package com.couragegang.bff.api;

import com.couragegang.bff.client.DownstreamClient;
import com.couragegang.bff.config.BffProperties;
import com.couragegang.bff.security.SecurityAttributes;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/knowledge")
public class KnowledgeProxyController {

    private final String knowledgeBase;
    private final DownstreamClient http;

    public KnowledgeProxyController(BffProperties props, DownstreamClient http) {
        this.knowledgeBase = trim(props.getKnowledgeBaseUrl());
        this.http = http;
    }

    @Get("/connectors")
    public HttpResponse<String> connectors() throws Exception {
        return forward(http.get(knowledgeBase + "/connectors", "knowledge", "list_connectors"));
    }

    @Get("/workspaces/{workspaceId}/sources")
    public HttpResponse<String> sources(
            @PathVariable String workspaceId,
            @RequestAttribute(value = SecurityAttributes.ORG_ID, defaultValue = "") String orgId)
            throws Exception {
        return forward(
                http.get(
                        knowledgeBase + "/workspaces/" + workspaceId + "/sources?org_id=" + orgId,
                        "knowledge",
                        "list_sources"));
    }

    @Post("/workspaces/{workspaceId}/sources")
    public HttpResponse<String> createSource(
            @PathVariable String workspaceId,
            @RequestAttribute(value = SecurityAttributes.ORG_ID, defaultValue = "") String orgId,
            @Body String body)
            throws Exception {
        return forward(
                http.post(
                        knowledgeBase + "/workspaces/" + workspaceId + "/sources?org_id=" + orgId,
                        body,
                        "knowledge",
                        "create_source"));
    }

    @Post("/search")
    public HttpResponse<String> search(@Body String body) throws Exception {
        return forward(http.post(knowledgeBase + "/search", body, "knowledge", "search"));
    }

    @Post("/sources/{sourceId}/reindex")
    public HttpResponse<String> reindex(@PathVariable String sourceId, @Body @Nullable String body) throws Exception {
        return forward(
                http.post(
                        knowledgeBase + "/sources/" + sourceId + "/reindex",
                        body != null ? body : "{}",
                        "knowledge",
                        "reindex"));
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
