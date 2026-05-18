package com.couragegang.bff.iam;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
public record IntrospectResponse(
        boolean active,
        String sub,
        String orgId,
        List<String> permissions
) {}
