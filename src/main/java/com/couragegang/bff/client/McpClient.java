package com.couragegang.bff.client;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

@Client(id = "mcp")
public interface McpClient {

    @Get("/v1/mcp/catalog")
    HttpResponse<String> catalog();

    @Get("/v1/mcp/catalog/{connectorKey}")
    HttpResponse<String> catalogItem(@PathVariable String connectorKey);

    @Get("/v1/mcp/workspaces/{workspaceId}/installations")
    HttpResponse<String> listInstallations(@PathVariable String workspaceId);

    @Post("/v1/mcp/workspaces/{workspaceId}/installations")
    HttpResponse<String> createInstallation(
            @PathVariable String workspaceId,
            @Header("X-Org-Id") String orgId,
            @Header("X-User-Id") String userId,
            @Body String body);

    @Get("/v1/mcp/workspaces/{workspaceId}/installations/{installationId}")
    HttpResponse<String> getInstallation(
            @PathVariable String workspaceId, @PathVariable String installationId);

    @Patch("/v1/mcp/workspaces/{workspaceId}/installations/{installationId}")
    HttpResponse<String> patchInstallation(
            @PathVariable String workspaceId, @PathVariable String installationId, @Body String body);

    @Delete("/v1/mcp/workspaces/{workspaceId}/installations/{installationId}")
    HttpResponse<String> deleteInstallation(
            @PathVariable String workspaceId, @PathVariable String installationId);

    @Post("/v1/mcp/workspaces/{workspaceId}/installations/{installationId}/health")
    HttpResponse<String> healthInstallation(
            @PathVariable String workspaceId, @PathVariable String installationId);

    @Post("/v1/mcp/workspaces/{workspaceId}/notion/discover")
    HttpResponse<String> discoverNotion(
            @PathVariable String workspaceId, @Body String body);
}
