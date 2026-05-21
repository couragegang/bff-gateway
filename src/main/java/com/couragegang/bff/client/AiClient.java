package com.couragegang.bff.client;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

@Client(id = "ai")
public interface AiClient {

    @Post("/v1/ai/chat")
    HttpResponse<String> chat(@Body String body);
}
