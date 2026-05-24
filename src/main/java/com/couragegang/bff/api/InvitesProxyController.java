package com.couragegang.bff.api;

import com.couragegang.bff.iam.IamUserProxyClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;
import java.util.Optional;

/** /api/invites/* → IAM /v1/iam/invites/* */
@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/invites")
public class InvitesProxyController {

    private final IamUserProxyClient iam;

    public InvitesProxyController(IamUserProxyClient iam) {
        this.iam = iam;
    }

    @Post("/accept")
    public HttpResponse<String> accept(@Body @Nullable String body, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardPost("/invites/accept", body, authorization(request)));
    }

    private static Optional<String> authorization(HttpRequest<?> request) {
        return request.getHeaders().getAuthorization();
    }

    private static HttpResponse<String> forward(HttpResponse<String> downstream) {
        var code = downstream.getStatus().getCode();
        if (code >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }
}
