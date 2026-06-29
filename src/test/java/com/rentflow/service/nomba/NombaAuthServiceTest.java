package com.rentflow.service.nomba;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class NombaAuthServiceTest {

    @Test
    void testTokenCachingAndRetrieve() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NombaAuthService authService = new NombaAuthService(builder, "https://sandbox-api.nomba.com", "real-id", "real-secret");

        // Mock token endpoint to return a token valid for 3600 seconds
        server.expect(requestTo("https://sandbox-api.nomba.com/v1/auth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"code\":\"00\",\"description\":\"Success\",\"data\":{\"accessToken\":\"token-123\",\"expiresIn\":3600,\"tokenType\":\"Bearer\"}}", MediaType.APPLICATION_JSON));

        // Call getValidToken() twice
        String token1 = authService.getValidToken();
        assertEquals("token-123", token1);

        String token2 = authService.getValidToken();
        assertEquals("token-123", token2);

        // Verify the mock server is hit exactly once (second call uses cached instance)
        server.verify();
    }

    @Test
    void testTokenRefreshUnderExpiry() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NombaAuthService authService = new NombaAuthService(builder, "https://sandbox-api.nomba.com", "real-id", "real-secret");

        // Populate the cache with an expired token or expiry time < 5 minutes in future
        authService.setCachedToken("old-token");
        authService.setTokenExpiryTime(LocalDateTime.now().plusMinutes(4)); // Less than 5 minutes in future

        // Mock token endpoint to return a new token
        server.expect(requestTo("https://sandbox-api.nomba.com/v1/auth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"code\":\"00\",\"description\":\"Success\",\"data\":{\"accessToken\":\"new-token-456\",\"expiresIn\":3600,\"tokenType\":\"Bearer\"}}", MediaType.APPLICATION_JSON));

        // Call getValidToken()
        String token = authService.getValidToken();
        assertEquals("new-token-456", token);

        // Verify mock server was called to refresh the token
        server.verify();
    }
}
