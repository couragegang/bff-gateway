package com.couragegang.bff.api;

import com.couragegang.bff.client.AiClient;
import com.couragegang.bff.security.SecurityAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.annotation.Nullable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api")
public class AiProxyController {

    private final AiClient ai;
    private final ObjectMapper json = new ObjectMapper();

    public AiProxyController(AiClient ai) {
        this.ai = ai;
    }

    @Post("/chat")
    public HttpResponse<String> chat(
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceFromJwt,
            @RequestAttribute(value = SecurityAttributes.ORG_ID, defaultValue = "") String orgId,
            @RequestAttribute(SecurityAttributes.USER_ID) String userId,
            @Header("X-Workspace-Id") @Nullable String workspaceHeader,
            @Body String body)
            throws Exception {
        var workspaceId = workspaceHeader != null && !workspaceHeader.isBlank()
                ? workspaceHeader
                : workspaceFromJwt;
        if (workspaceId == null || workspaceId.isBlank()) {
            throw new HttpStatusException(
                    io.micronaut.http.HttpStatus.BAD_REQUEST, "workspace_id required (JWT or X-Workspace-Id)");
        }
        var node = (ObjectNode) json.readTree(body != null && !body.isBlank() ? body : "{}");
        node.put("workspaceId", workspaceId);
        if (orgId != null && !orgId.isBlank()) {
            node.put("orgId", orgId);
        }
        if (userId != null && !userId.isBlank()) {
            node.put("userId", userId);
        }
        var downstream = ai.chat(json.writeValueAsString(node));
        if (downstream.getStatus().getCode() >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }
}
