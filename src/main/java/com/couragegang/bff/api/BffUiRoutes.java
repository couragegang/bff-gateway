package com.couragegang.bff.api;

import java.util.List;

/**
 * Канонический список HTTP-маршрутов, которые вызывает Web UI ({@code createBffApi} + {@code api.ts}).
 * Используется тестом {@link BffUiRoutesCoverageTest} и документацией.
 */
public final class BffUiRoutes {

    public record Route(String method, String path) {}

    /** Пути относительно {@code /api} (без context-path {@code /v1/bff}). */
    public static final List<Route> ALL = List.of(
            // session / IAM profile
            new Route("GET", "/api/me"),
            new Route("PATCH", "/api/me"),
            // auth (public)
            new Route("POST", "/api/auth/login"),
            new Route("POST", "/api/auth/register"),
            new Route("POST", "/api/auth/logout"),
            new Route("POST", "/api/auth/switch-org"),
            // organizations
            new Route("POST", "/api/organizations"),
            new Route("GET", "/api/organizations/{orgId}"),
            new Route("PATCH", "/api/organizations/{orgId}"),
            new Route("GET", "/api/organizations/{orgId}/my-groups"),
            new Route("GET", "/api/organizations/{orgId}/groups"),
            new Route("POST", "/api/organizations/{orgId}/groups"),
            new Route("GET", "/api/organizations/{orgId}/invites"),
            new Route("POST", "/api/organizations/{orgId}/invites"),
            new Route("DELETE", "/api/organizations/{orgId}/invites/{inviteId}"),
            new Route("POST", "/api/invites/accept"),
            // config / workspaces
            new Route("GET", "/api/config/orgs/{orgId}/workspaces"),
            new Route("POST", "/api/config/orgs/{orgId}/workspaces"),
            new Route("PATCH", "/api/config/workspaces/{workspaceId}"),
            // chat / conversations
            new Route("POST", "/api/chat"),
            new Route("GET", "/api/conversations"),
            new Route("POST", "/api/conversations"),
            new Route("GET", "/api/conversations/{conversationId}/messages"),
            new Route("PATCH", "/api/conversations/{conversationId}"),
            new Route("DELETE", "/api/conversations/{conversationId}"),
            // mcp
            new Route("GET", "/api/mcp/catalog"),
            new Route("GET", "/api/mcp/catalog/{connectorKey}"),
            new Route("GET", "/api/mcp/workspaces/{workspaceId}/installations"),
            new Route("POST", "/api/mcp/workspaces/{workspaceId}/installations"),
            new Route("GET", "/api/mcp/workspaces/{workspaceId}/installations/{installationId}"),
            new Route("PATCH", "/api/mcp/workspaces/{workspaceId}/installations/{installationId}"),
            new Route("DELETE", "/api/mcp/workspaces/{workspaceId}/installations/{installationId}"),
            new Route("POST", "/api/mcp/workspaces/{workspaceId}/installations/{installationId}/health"),
            new Route("POST", "/api/mcp/workspaces/{workspaceId}/notion/discover"),
            // policy (HITL в чате)
            new Route("POST", "/api/policy/pending-approvals/{id}/approve"),
            new Route("POST", "/api/policy/pending-approvals/{id}/reject"));

    private BffUiRoutes() {}
}
