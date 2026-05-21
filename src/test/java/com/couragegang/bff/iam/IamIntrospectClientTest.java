package com.couragegang.bff.iam;

import com.couragegang.bff.config.BffProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IamIntrospectClientTest {

    @Test
    void unreachableIamReturnsEmpty() {
        var props = new BffProperties();
        props.setIamBaseUrl("http://127.0.0.1:1");
        var client = new IamIntrospectClient(props);
        assertTrue(client.introspect("bad").isEmpty());
    }
}
