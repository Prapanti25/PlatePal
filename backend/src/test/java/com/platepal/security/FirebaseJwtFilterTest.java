package com.platepal.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class FirebaseJwtFilterTest {
    @Test
    void rejectsBearerTokensWhenAdminCredentialsAreMissing() throws Exception {
        FirebaseJwtFilter filter = new FirebaseJwtFilter(new FirebaseTokenVerifier("", ""));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/profile");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(503, response.getStatus());
    }
}