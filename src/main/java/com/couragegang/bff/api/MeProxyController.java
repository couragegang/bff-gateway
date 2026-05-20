package com.couragegang.bff.api;

import com.couragegang.bff.security.SecurityAttributes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.RequestAttribute;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller("/api")
public class MeProxyController {

    @Get("/me")
    public Map<String, Object> me(
            @RequestAttribute(SecurityAttributes.USER_ID) String userId,
            @RequestAttribute(value = SecurityAttributes.ORG_ID, defaultValue = "") String orgId,
            @RequestAttribute(value = SecurityAttributes.GROUP_ID, defaultValue = "") String groupId,
            @RequestAttribute(value = SecurityAttributes.WORKSPACE_ID, defaultValue = "") String workspaceId,
            @RequestAttribute(SecurityAttributes.PERMISSIONS) List<String> permissions) {
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
        return out;
    }
}
