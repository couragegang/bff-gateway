package com.couragegang.bff.iam;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IamIntrospectClientTest {

  @Test
  void inactiveTokenReturnsEmpty() {
    var api = mock(IamApi.class);
    when(api.introspect(new TokenRequest("bad"))).thenReturn(new IntrospectResponse(false, null, null, null));
    var client = new IamIntrospectClient(api);
    assertTrue(client.introspect("bad").isEmpty());
  }
}
