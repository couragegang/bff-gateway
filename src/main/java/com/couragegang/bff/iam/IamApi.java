package com.couragegang.bff.iam;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

@Client(id = "iam")
public interface IamApi {

    @Post("/internal/token/introspect")
    IntrospectResponse introspect(@Body TokenRequest body);
}
