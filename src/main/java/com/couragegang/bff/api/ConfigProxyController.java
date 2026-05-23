package com.couragegang.bff.api;

import com.couragegang.bff.client.DownstreamClient;
import com.couragegang.bff.config.BffProperties;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/config")
public class ConfigProxyController {

    private final String configBase;
    private final DownstreamClient http = new DownstreamClient();

    public ConfigProxyController(BffProperties props) {
        this.configBase = trim(props.getConfigBaseUrl());
    }

    @Get("/orgs/{orgId}/workspaces")
    public HttpResponse<String> list(
            @PathVariable String orgId,
            @QueryValue("group_id") @Nullable String groupId,
            @QueryValue(defaultValue = "50") int limit)
            throws Exception {
        var url = configBase + "/orgs/" + orgId + "/workspaces?limit=" + limit;
        if (groupId != null && !groupId.isBlank()) {
            url += "&group_id=" + groupId;
        }
        return forward(http.get(url));
    }

    @Post("/orgs/{orgId}/workspaces")
    public HttpResponse<String> create(
            @PathVariable String orgId, @Body @Nullable String body) throws Exception {
        return forward(http.post(configBase + "/orgs/" + orgId + "/workspaces", body != null ? body : "{}"));
    }

    @Get("/workspaces/{workspaceId}")
    public HttpResponse<String> get(@PathVariable String workspaceId) throws Exception {
        return forward(http.get(configBase + "/workspaces/" + workspaceId));
    }

    @Patch("/workspaces/{workspaceId}")
    public HttpResponse<String> patch(
            @PathVariable String workspaceId, @Body @Nullable String body) throws Exception {
        return forward(http.patch(configBase + "/workspaces/" + workspaceId, body != null ? body : "{}"));
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
