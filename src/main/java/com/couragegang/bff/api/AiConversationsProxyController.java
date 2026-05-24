package com.couragegang.bff.api;

import com.couragegang.bff.client.AiClient;
import com.couragegang.bff.security.SecurityAttributes;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/conversations")
public class AiConversationsProxyController {

    private final AiClient ai;

    public AiConversationsProxyController(AiClient ai) {
        this.ai = ai;
    }

    @Get
    public HttpResponse<String> list(
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceFromJwt,
            @Header("X-Workspace-Id") @Nullable String workspaceHeader,
            @QueryValue(value = "include_archived", defaultValue = "false") boolean includeArchived) {
        var workspaceId = resolveWorkspace(workspaceFromJwt, workspaceHeader);
        return forward(ai.listConversations(workspaceId, includeArchived));
    }

    @Post
    public HttpResponse<String> create(
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceFromJwt,
            @RequestAttribute(value = SecurityAttributes.ORG_ID, defaultValue = "") String orgId,
            @RequestAttribute(SecurityAttributes.USER_ID) String userId,
            @Header("X-Workspace-Id") @Nullable String workspaceHeader,
            @Body @Nullable String body) {
        var workspaceId = resolveWorkspace(workspaceFromJwt, workspaceHeader);
        return forward(ai.createConversation(workspaceId, orgId, userId, body != null ? body : "{}"));
    }

    @Get("/{conversationId}/messages")
    public HttpResponse<String> messages(
            @PathVariable String conversationId,
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceFromJwt,
            @Header("X-Workspace-Id") @Nullable String workspaceHeader) {
        var workspaceId = resolveWorkspace(workspaceFromJwt, workspaceHeader);
        return forward(ai.listMessages(conversationId, workspaceId));
    }

    @Patch("/{conversationId}")
    public HttpResponse<String> archive(
            @PathVariable String conversationId,
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceFromJwt,
            @Header("X-Workspace-Id") @Nullable String workspaceHeader,
            @Body String body) {
        var workspaceId = resolveWorkspace(workspaceFromJwt, workspaceHeader);
        return forward(ai.patchConversation(conversationId, workspaceId, body));
    }

    @Delete("/{conversationId}")
    public HttpResponse<String> delete(
            @PathVariable String conversationId,
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceFromJwt,
            @Header("X-Workspace-Id") @Nullable String workspaceHeader) {
        var workspaceId = resolveWorkspace(workspaceFromJwt, workspaceHeader);
        return forward(ai.deleteConversation(conversationId, workspaceId));
    }

    private static String resolveWorkspace(String fromJwt, @Nullable String header) {
        if (header != null && !header.isBlank()) {
            return header;
        }
        if (fromJwt != null && !fromJwt.isBlank()) {
            return fromJwt;
        }
        throw new HttpStatusException(
                io.micronaut.http.HttpStatus.BAD_REQUEST, "workspace_id required (JWT or X-Workspace-Id)");
    }

    private static HttpResponse<String> forward(HttpResponse<String> downstream) {
        if (downstream.getStatus().getCode() >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }
}
