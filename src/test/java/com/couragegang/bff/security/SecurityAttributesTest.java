package com.couragegang.bff.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecurityAttributesTest {

    @Test
    void attributeKeysAreStable() {
        assertThat(SecurityAttributes.USER_ID).isEqualTo("bff.userId");
        assertThat(SecurityAttributes.ORG_ID).isEqualTo("bff.orgId");
        assertThat(SecurityAttributes.PERMISSIONS).isEqualTo("bff.permissions");
    }
}
