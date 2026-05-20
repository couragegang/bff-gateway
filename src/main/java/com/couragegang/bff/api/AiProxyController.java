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

@Controller("/api")
public final class AiProxyController {

    private final AiClient ai;
    private final ObjectMapper json;

    public AiProxyController(AiClient ai, ObjectMapper json) {
        this.ai = ai;
        this.json = json;
    }

    @Post("/chat")
    public HttpResponse<String> chat(
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceFromJwt,
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
        var node = (ObjectNode) json.readTree(body);
        node.put("workspaceId", workspaceId);
        var downstream = ai.chat(json.writeValueAsString(node));
        if (downstream.getStatus().getCode() >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }
}
