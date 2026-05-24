package com.couragegang.bff.api;

import com.couragegang.bff.iam.IamUserProxyClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;
import java.util.Optional;

/** /api/organizations/* → IAM /v1/iam/organizations/* */
@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api/organizations")
public class OrganizationsProxyController {

    private final IamUserProxyClient iam;

    public OrganizationsProxyController(IamUserProxyClient iam) {
        this.iam = iam;
    }

    @Post
    public HttpResponse<String> create(@Body @Nullable String body, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardPost("/organizations", body, authorization(request)));
    }

    @Get("/{orgId}")
    public HttpResponse<String> get(@PathVariable String orgId, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardGet("/organizations/" + orgId, authorization(request)));
    }

    @Patch("/{orgId}")
    public HttpResponse<String> patch(
            @PathVariable String orgId, @Body @Nullable String body, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardPatch("/organizations/" + orgId, body, authorization(request)));
    }

    @Get("/{orgId}/my-groups")
    public HttpResponse<String> myGroups(@PathVariable String orgId, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardGet("/organizations/" + orgId + "/my-groups", authorization(request)));
    }

    @Get("/{orgId}/groups")
    public HttpResponse<String> groups(@PathVariable String orgId, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardGet("/organizations/" + orgId + "/groups", authorization(request)));
    }

    @Post("/{orgId}/groups")
    public HttpResponse<String> createGroup(
            @PathVariable String orgId, @Body @Nullable String body, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardPost("/organizations/" + orgId + "/groups", body, authorization(request)));
    }

    @Get("/{orgId}/invites")
    public HttpResponse<String> invites(@PathVariable String orgId, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardGet("/organizations/" + orgId + "/invites", authorization(request)));
    }

    @Post("/{orgId}/invites")
    public HttpResponse<String> createInvite(
            @PathVariable String orgId, @Body @Nullable String body, HttpRequest<?> request) throws Exception {
        return forward(iam.forwardPost("/organizations/" + orgId + "/invites", body, authorization(request)));
    }

    @Delete("/{orgId}/invites/{inviteId}")
    public HttpResponse<String> revokeInvite(
            @PathVariable String orgId, @PathVariable String inviteId, HttpRequest<?> request) throws Exception {
        return forward(
                iam.forwardDelete("/organizations/" + orgId + "/invites/" + inviteId, authorization(request)));
    }

    private static Optional<String> authorization(HttpRequest<?> request) {
        return request.getHeaders().getAuthorization();
    }

    private static HttpResponse<String> forward(HttpResponse<String> downstream) {
        var code = downstream.getStatus().getCode();
        if (code >= 400) {
            throw new HttpStatusException(downstream.getStatus(), downstream.body());
        }
        if (code == 204 || downstream.body() == null || downstream.body().isBlank()) {
            return HttpResponse.status(downstream.getStatus());
        }
        return HttpResponse.status(downstream.getStatus()).body(downstream.body());
    }
}
