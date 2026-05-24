package com.couragegang.bff.client;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

@Client(id = "ai")
public interface AiClient {

    @Post("/v1/ai/chat")
    HttpResponse<String> chat(@Body String body);

    @Get("/v1/ai/conversations{?workspace_id,include_archived}")
    HttpResponse<String> listConversations(
            @QueryValue("workspace_id") String workspaceId,
            @QueryValue(value = "include_archived", defaultValue = "false") boolean includeArchived);

    @Post("/v1/ai/conversations{?workspace_id,org_id,user_id}")
    HttpResponse<String> createConversation(
            @QueryValue("workspace_id") String workspaceId,
            @QueryValue("org_id") String orgId,
            @QueryValue("user_id") String userId,
            @Body String body);

    @Get("/v1/ai/conversations/{conversationId}/messages{?workspace_id}")
    HttpResponse<String> listMessages(
            @PathVariable String conversationId,
            @QueryValue("workspace_id") String workspaceId);

    @Patch("/v1/ai/conversations/{conversationId}{?workspace_id}")
    HttpResponse<String> patchConversation(
            @PathVariable String conversationId,
            @QueryValue("workspace_id") String workspaceId,
            @Body String body);

    @Delete("/v1/ai/conversations/{conversationId}{?workspace_id}")
    HttpResponse<String> deleteConversation(
            @PathVariable String conversationId,
            @QueryValue("workspace_id") String workspaceId);
}
