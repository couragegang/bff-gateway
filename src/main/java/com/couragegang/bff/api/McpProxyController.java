package com.couragegang.bff.api;

import com.couragegang.bff.client.McpClient;
import com.couragegang.bff.security.SecurityAttributes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.HttpStatus;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.List;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/mcp")
public class McpProxyController {

    private static final String POLICY_MANAGE_PERMISSION = "iam.member.manage";

    private final McpClient mcp;
    private final ObjectMapper json = new ObjectMapper();

    public McpProxyController(McpClient mcp) {
        this.mcp = mcp;
    }

    @Get("/catalog")
    public HttpResponse<String> catalog() {
        return forward(mcp.catalog());
    }

    @Get("/catalog/{connectorKey}")
    public HttpResponse<String> catalogItem(@PathVariable String connectorKey) {
        return forward(mcp.catalogItem(connectorKey));
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
            @RequestAttribute(SecurityAttributes.PERMISSIONS) List<String> permissions,
            @Body String body) {
        if (orgId.isBlank()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "organization context required");
        }
        return forward(mcp.createInstallation(workspaceId, orgId, userId, sanitizeInstallBody(body, permissions)));
    }

    @Get("/workspaces/{workspaceId}/installations/{installationId}")
    public HttpResponse<String> getInstallation(
            @PathVariable String workspaceId, @PathVariable String installationId) {
        return forward(mcp.getInstallation(workspaceId, installationId));
    }

    @Patch("/workspaces/{workspaceId}/installations/{installationId}")
    public HttpResponse<String> patchInstallation(
            @PathVariable String workspaceId, @PathVariable String installationId, @Body String body) {
        return forward(mcp.patchInstallation(workspaceId, installationId, body));
    }

    @Delete("/workspaces/{workspaceId}/installations/{installationId}")
    public HttpResponse<String> deleteInstallation(
            @PathVariable String workspaceId, @PathVariable String installationId) {
        return forward(mcp.deleteInstallation(workspaceId, installationId));
    }

    @Post("/workspaces/{workspaceId}/installations/{installationId}/health")
    public HttpResponse<String> healthInstallation(
            @PathVariable String workspaceId, @PathVariable String installationId) {
        return forward(mcp.healthInstallation(workspaceId, installationId));
    }

    @Post("/workspaces/{workspaceId}/notion/discover")
    public HttpResponse<String> discoverNotion(@PathVariable String workspaceId, @Body String body) {
        return forward(mcp.discoverNotion(workspaceId, body));
    }

    private String sanitizeInstallBody(String body, List<String> permissions) {
        try {
            JsonNode root = json.readTree(body != null && !body.isBlank() ? body : "{}");
            if (!root.isObject()) {
                return body;
            }
            ObjectNode obj = (ObjectNode) root;
            if (obj.has("policyPack") && !canCustomizePolicy(permissions)) {
                obj.remove("policyPack");
            }
            return json.writeValueAsString(obj);
        } catch (Exception e) {
            return body;
        }
    }

    private static boolean canCustomizePolicy(List<String> permissions) {
        return permissions != null && permissions.contains(POLICY_MANAGE_PERMISSION);
    }

    private static HttpResponse<String> forward(HttpResponse<String> downstream) {
        if (downstream.getStatus().getCode() >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }
}
