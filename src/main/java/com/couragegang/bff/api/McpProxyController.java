package com.couragegang.bff.api;

import com.couragegang.bff.client.McpClient;
import com.couragegang.bff.security.SecurityAttributes;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.HttpStatus;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/mcp")
public class McpProxyController {

    private final McpClient mcp;

    public McpProxyController(McpClient mcp) {
        this.mcp = mcp;
    }

    @Get("/catalog")
    public HttpResponse<String> catalog() {
        return forward(mcp.catalog());
    }

    @Get("/workspaces/{workspaceId}/installations")
    public HttpResponse<String> installations(@PathVariable String workspaceId) {
        return forward(mcp.listInstallations(workspaceId));
    }

    @Post("/workspaces/{workspaceId}/installations")
    public HttpResponse<String> createInstallation(
            @PathVariable String workspaceId,
            @RequestAttribute(value = SecurityAttributes.ORG_ID, defaultValue = "") String orgId,
            @RequestAttribute(SecurityAttributes.USER_ID) String userId,
            @Body String body) {
        if (orgId.isBlank()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "organization context required");
        }
        return forward(mcp.createInstallation(workspaceId, orgId, userId, body));
    }

    private static HttpResponse<String> forward(HttpResponse<String> downstream) {
        if (downstream.getStatus().getCode() >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }
}
