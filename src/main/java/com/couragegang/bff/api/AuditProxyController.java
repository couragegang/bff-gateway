package com.couragegang.bff.api;

import com.couragegang.bff.client.DownstreamClient;
import com.couragegang.bff.config.BffProperties;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/audit")
public class AuditProxyController {

    private final String auditBase;
    private final DownstreamClient http = new DownstreamClient();

    public AuditProxyController(BffProperties props) {
        this.auditBase = trim(props.getAuditBaseUrl());
    }

    @Get("/orgs/{orgId}/tool-events")
    public HttpResponse<String> toolEvents(
            @PathVariable String orgId, @QueryValue("workspace_id") @Nullable String workspaceId)
            throws Exception {
        var url = auditBase + "/orgs/" + orgId + "/tool-events";
        if (workspaceId != null && !workspaceId.isBlank()) {
            url += "?workspace_id=" + workspaceId;
        }
        return forward(http.get(url));
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
