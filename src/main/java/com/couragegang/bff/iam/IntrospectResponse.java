package com.couragegang.bff.iam;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import java.util.List;

@Serdeable
public record IntrospectResponse(
        boolean active,
        @Nullable String sub,
        @Nullable String orgId,
        @Nullable String groupId,
        @Nullable String workspaceId,
        @Nullable String scope,
        @Nullable List<String> roles,
        @Nullable List<String> permissions,
        @Nullable Long exp) {}
