package com.couragegang.bff.iam;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TokenRequest(String token) {}
