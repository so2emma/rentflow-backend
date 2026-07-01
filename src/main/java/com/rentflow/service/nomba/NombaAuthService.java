package com.rentflow.service.nomba;

import com.rentflow.dto.nomba.TokenRequest;
import com.rentflow.dto.nomba.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Service
public class NombaAuthService {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private LocalDateTime tokenExpiryTime;

    public NombaAuthService(
            RestClient.Builder restClientBuilder,
            @Value("${nomba.api.url}") String baseUrl,
            @Value("${nomba.client.id}") String clientId,
            @Value("${nomba.client.secret}") String clientSecret) {
        if (clientId != null && !clientId.trim().isEmpty() && !clientId.contains("placeholder") && !clientId.contains("your-") && !"real-id".equals(clientId) && !"test-id".equals(clientId)) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);
            restClientBuilder.requestFactory(factory);
        }

        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getValidToken() {
        if (isPlaceholderCredentials()) {
            log.debug("Using placeholder Nomba credentials. Returning mock token.");
            return "mock-token";
        }
        if (cachedToken == null || LocalDateTime.now().isAfter(tokenExpiryTime.minusMinutes(5))) {
            log.info("Nomba access token missing or expired. Refreshing token.");
            refreshToken();
        } else {
            log.debug("Using cached Nomba access token.");
        }
        return cachedToken;
    }

    public boolean isPlaceholderCredentials() {
        return clientId == null || clientId.trim().isEmpty() || clientId.contains("placeholder") || clientId.contains("your-")
                || clientSecret == null || clientSecret.trim().isEmpty() || clientSecret.contains("placeholder") || clientSecret.contains("your-");
    }

    private void refreshToken() {
        log.info("Initiating request to fetch new Nomba access token.");
        TokenRequest request = new TokenRequest(clientId, clientSecret);
        TokenResponse response = restClient.post()
                .uri("/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TokenResponse.class);

        if (response != null && "00".equals(response.code())) {
            this.cachedToken = response.data().accessToken();
            this.tokenExpiryTime = LocalDateTime.now().plusSeconds(response.data().expiresIn());
            log.info("Successfully fetched new Nomba access token. Expires in {} seconds.", response.data().expiresIn());
        } else {
            String errorDesc = response != null ? response.description() : "Empty Response";
            log.error("Failed to fetch Nomba Access Token: {}", errorDesc);
            throw new RuntimeException("Failed to fetch Nomba Access Token: " + errorDesc);
        }
    }

    // Package-private setters for testing purposes
    void setCachedToken(String cachedToken) {
        this.cachedToken = cachedToken;
    }

    void setTokenExpiryTime(LocalDateTime tokenExpiryTime) {
        this.tokenExpiryTime = tokenExpiryTime;
    }

    String getCachedToken() {
        return cachedToken;
    }

    LocalDateTime getTokenExpiryTime() {
        return tokenExpiryTime;
    }
}
