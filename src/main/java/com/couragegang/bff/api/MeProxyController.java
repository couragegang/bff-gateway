package com.couragegang.bff.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import java.util.Map;

@Controller("/api")
public class MeProxyController {

  /**
   * Заглушка BFF. JWT-проверка через IAM introspect — следующая итерация
   * ({@link com.couragegang.bff.iam.IamIntrospectClient}).
   */
  @Get("/me")
  public Map<String, Object> me(
      @Header(value = "Authorization", defaultValue = "") String authorization) {
    var hasBearer = authorization.startsWith("Bearer ");
    return Map.of(
        "authenticated", hasBearer,
        "message",
        hasBearer
            ? "BFF stub: introspect IAM и прокси /me — в разработке"
            : "Требуется Authorization: Bearer <access_token>");
  }
}
