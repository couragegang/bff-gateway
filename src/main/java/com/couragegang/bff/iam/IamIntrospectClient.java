package com.couragegang.bff.iam;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public final class IamIntrospectClient {

    private final IamApi iamApi;

    public IamIntrospectClient(IamApi iamApi) {
        this.iamApi = iamApi;
    }

    public Optional<IntrospectResult> introspect(String bearerToken) {
        try {
            var r = iamApi.introspect(new TokenRequest(bearerToken));
            if (r == null || !r.active()) {
                return Optional.empty();
            }
            return Optional.of(
                    new IntrospectResult(r.sub(), r.orgId(), r.groupId(), r.workspaceId(), r.permissions()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public record IntrospectResult(
            String userId, String orgId, String groupId, String workspaceId, List<String> permissions) {}
}
