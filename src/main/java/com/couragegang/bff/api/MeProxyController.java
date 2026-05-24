package com.couragegang.bff.api;

import com.couragegang.bff.iam.IamUserProxyClient;
import com.couragegang.bff.security.SecurityAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GET /api/me — JWT-контекст + профиль IAM (один ответ для UI).
 * PATCH /api/me → IAM /me.
 */
@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api")
public class MeProxyController {

    private final IamUserProxyClient iam;
    private final ObjectMapper json = new ObjectMapper();

    @Inject
    public MeProxyController(IamUserProxyClient iam) {
        this.iam = iam;
    }

    @Get("/me")
    public String me(
            HttpRequest<?> request,
            @RequestAttribute(SecurityAttributes.USER_ID) String userId,
            @RequestAttribute(value = SecurityAttributes.ORG_ID, defaultValue = "") String orgId,
            @RequestAttribute(value = SecurityAttributes.GROUP_ID, defaultValue = "") String groupId,
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceId,
            @RequestAttribute(SecurityAttributes.PERMISSIONS) List<String> permissions)
            throws Exception {
        var out = new LinkedHashMap<String, Object>();
        out.put("authenticated", true);
        out.put("userId", userId);
        if (!orgId.isBlank()) {
            out.put("orgId", orgId);
        }
        if (!groupId.isBlank()) {
            out.put("groupId", groupId);
        }
        if (!workspaceId.isBlank()) {
            out.put("workspaceId", workspaceId);
        }
        out.put("permissions", permissions);

        var iamResp = iam.forwardGet("/me", authorization(request));
        if (iamResp.getStatus().getCode() < 400 && iamResp.body() != null && !iamResp.body().isBlank()) {
            var iamNode = json.readTree(iamResp.body());
            if (iamNode.has("user")) {
                out.put("user", json.convertValue(iamNode.get("user"), Map.class));
            }
            if (iamNode.has("organizations")) {
                out.put("organizations", json.convertValue(iamNode.get("organizations"), List.class));
            }
        }
        return json.writeValueAsString(out);
    }

    @Patch("/me")
    public HttpResponse<String> mePatch(@Body @Nullable String body, HttpRequest<?> request) throws Exception {
        var downstream = iam.forwardPatch("/me", body, authorization(request));
        if (downstream.getStatus().getCode() >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }

    private static Optional<String> authorization(HttpRequest<?> request) {
        return request.getHeaders().getAuthorization();
    }
}
