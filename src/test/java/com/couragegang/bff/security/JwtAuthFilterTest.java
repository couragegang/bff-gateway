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
        var userId = UUID.randomUUID().toString();
        var orgId = UUID.randomUUID().toString();
        var iam = mock(IamIntrospectClient.class);
        when(iam.introspect("tok"))
                .thenReturn(
                        Optional.of(
                                new IamIntrospectClient.IntrospectResult(
                                        userId,
                                        orgId,
                                        UUID.randomUUID().toString(),
                                        UUID.randomUUID().toString(),
                                        List.of("read"))));
        var filter = new JwtAuthFilter(iam);
        var chain = mock(ServerFilterChain.class);
        when(chain.proceed(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        var request = HttpRequest.GET("/api/x").header("Authorization", "Bearer tok");

        Mono.from(filter.doFilter(request, chain)).block();

        assertThat(request.getAttribute(SecurityAttributes.USER_ID, String.class).orElseThrow()).isEqualTo(userId);
        assertThat(request.getAttribute(SecurityAttributes.ORG_ID, String.class).orElseThrow()).isEqualTo(orgId);
    }
}
