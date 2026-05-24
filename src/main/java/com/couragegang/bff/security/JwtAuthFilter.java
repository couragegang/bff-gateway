package com.couragegang.bff.security;

import com.couragegang.bff.iam.IamIntrospectClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Filter("/api/**")
public class JwtAuthFilter implements HttpServerFilter {

    private final IamIntrospectClient iam;

    public JwtAuthFilter(IamIntrospectClient iam) {
        this.iam = iam;
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.order();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        if (isPublicAuthRoute(request)) {
            return chain.proceed(request);
        }
        var auth = request.getHeaders().getAuthorization().orElse(null);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Mono.just(unauthorized("Bearer token required"));
        }
        var token = auth.substring("Bearer ".length()).trim();
        var intro = iam.introspect(token);
        if (intro.isEmpty()) {
            return Mono.just(unauthorized("invalid or expired token"));
        }
        var r = intro.get();
        request.setAttribute(SecurityAttributes.USER_ID, r.userId());
        if (r.orgId() != null) {
            request.setAttribute(SecurityAttributes.ORG_ID, r.orgId());
        }
        if (r.groupId() != null) {
            request.setAttribute(SecurityAttributes.GROUP_ID, r.groupId());
        }
        if (r.workspaceId() != null) {
            request.setAttribute(SecurityAttributes.WORKSPACE_ID, r.workspaceId());
        }
        request.setAttribute(SecurityAttributes.PERMISSIONS, r.permissions());
        return chain.proceed(request);
    }

    private static MutableHttpResponse<?> unauthorized(String message) {
        return HttpResponse.status(HttpStatus.UNAUTHORIZED)
                .body(java.util.Map.of("code", "UNAUTHORIZED", "message", message));
    }

    static boolean isPublicAuthRoute(HttpRequest<?> request) {
        var path = normalizeApiPath(request.getPath());
        return path.equals("/api/auth") || path.startsWith("/api/auth/");
    }

    /** Strip Micronaut context-path (/v1/bff) so public routes match behind nginx and vite proxy. */
    static String normalizeApiPath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        if (path.startsWith("/v1/bff")) {
            var rest = path.substring("/v1/bff".length());
            return rest.isEmpty() ? "/" : rest;
        }
        return path;
    }
}
