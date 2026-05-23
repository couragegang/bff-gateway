package com.couragegang.bff.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.couragegang.bff.iam.IamIntrospectClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.filter.ServerFilterChain;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class JwtAuthFilterTest {

    @Test
    void unauthorizedWithoutBearer() {
        var iam = mock(IamIntrospectClient.class);
        var filter = new JwtAuthFilter(iam);
        var chain = mock(ServerFilterChain.class);

        var response = Mono.from(filter.doFilter(HttpRequest.GET("/api/x"), chain)).block();

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void unauthorizedWhenIntrospectFails() {
        var iam = mock(IamIntrospectClient.class);
        when(iam.introspect("tok")).thenReturn(Optional.empty());
        var filter = new JwtAuthFilter(iam);
        var chain = mock(ServerFilterChain.class);
        var request = HttpRequest.GET("/api/x").header("Authorization", "Bearer tok");

        var response = Mono.from(filter.doFilter(request, chain)).block();

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void proceedsAndSetsAttributes() {
        var iam = mock(IamIntrospectClient.class);
        when(iam.introspect("tok"))
                .thenReturn(
                        Optional.of(
                                new IamIntrospectClient.IntrospectResult(
                                        "user-1", "org-1", "group-1", "ws-1", List.of("read"))));
        var filter = new JwtAuthFilter(iam);
        var chain = mock(ServerFilterChain.class);
        when(chain.proceed(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        var request = HttpRequest.GET("/api/x").header("Authorization", "Bearer tok");

        Mono.from(filter.doFilter(request, chain)).block();

        assertThat(request.getAttribute(SecurityAttributes.USER_ID, String.class).orElseThrow()).isEqualTo("user-1");
        assertThat(request.getAttribute(SecurityAttributes.ORG_ID, String.class).orElseThrow()).isEqualTo("org-1");
        assertThat(request.getAttribute(SecurityAttributes.GROUP_ID, String.class).orElseThrow()).isEqualTo("group-1");
        assertThat(request.getAttribute(SecurityAttributes.WORKSPACE_ID, String.class).orElseThrow())
                .isEqualTo("ws-1");
    }

    @Test
    void proceedsWhenOptionalContextFieldsNull() {
        var iam = mock(IamIntrospectClient.class);
        when(iam.introspect("tok"))
                .thenReturn(
                        Optional.of(
                                new IamIntrospectClient.IntrospectResult("user-1", null, null, null, List.of())));
        var filter = new JwtAuthFilter(iam);
        var chain = mock(ServerFilterChain.class);
        when(chain.proceed(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        var request = HttpRequest.GET("/api/x").header("Authorization", "Bearer tok");

        Mono.from(filter.doFilter(request, chain)).block();

        assertThat(request.getAttribute(SecurityAttributes.USER_ID, String.class)).isPresent();
        assertThat(request.getAttribute(SecurityAttributes.ORG_ID, String.class)).isEmpty();
    }

    @Test
    void rejectsBearerWithoutSpace() {
        var iam = mock(IamIntrospectClient.class);
        var filter = new JwtAuthFilter(iam);
        var request = HttpRequest.GET("/api/x").header("Authorization", "Bearertok");

        var response = Mono.from(filter.doFilter(request, mock(ServerFilterChain.class))).block();

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.getCode());
    }
}
