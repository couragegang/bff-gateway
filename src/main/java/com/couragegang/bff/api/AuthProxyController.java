package com.couragegang.bff.api;

import com.couragegang.bff.iam.IamAuthProxyClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;

/** /api/auth/* → IAM /v1/iam/auth/* (login, register, refresh, OIDC start, …). */
@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/auth")
public class AuthProxyController {

    private final IamAuthProxyClient iamAuth;

    public AuthProxyController(IamAuthProxyClient iamAuth) {
        this.iamAuth = iamAuth;
    }

    @Get("/{+path}")
    public HttpResponse<String> get(@PathVariable String path, HttpRequest<?> request) throws Exception {
        return iamAuth.forwardGet(path, request.getUri().getQuery(), request.getHeaders().getAuthorization());
    }

    @Post("/{+path}")
    public HttpResponse<String> post(
            @PathVariable String path, @Body @Nullable String body, HttpRequest<?> request) throws Exception {
        return iamAuth.forwardPost(path, request.getUri().getQuery(), body, request.getHeaders().getAuthorization());
    }
}
