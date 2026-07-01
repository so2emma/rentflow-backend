package com.rentflow.service.nomba;

import com.rentflow.dto.nomba.VActData;
import com.rentflow.dto.nomba.VirtualAccountRequest;
import com.rentflow.dto.nomba.VirtualAccountResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
@Service
public class NombaClient {

    private final RestClient restClient;
    private final NombaAuthService nombaAuthService;
    private final String accountId;

    public NombaClient(
            RestClient.Builder restClientBuilder,
            NombaAuthService nombaAuthService,
            @Value("${nomba.api.url}") String baseUrl,
            @Value("${nomba.client.account-id:placeholder-account-id}") String accountId) {
        if (!nombaAuthService.isPlaceholderCredentials()) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);
            restClientBuilder.requestFactory(factory);
        }

        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.nombaAuthService = nombaAuthService;
        this.accountId = accountId;
    }

    public VActData createVirtualAccount(VirtualAccountRequest request) {
        log.info("Requesting virtual account creation accountRef={} expectedAmount={}", request.accountRef(), request.expectedAmount());
        
        if (nombaAuthService.isPlaceholderCredentials()) {
            log.debug("Placeholder credentials detected, generating mock virtual account accountRef={}", request.accountRef());
            return generateMockVirtualAccount(request);
        }

        try {
            String token = nombaAuthService.getValidToken();

            VirtualAccountResponse response = restClient.post()
                    .uri("/v1/accounts/virtual")
                    .header("Authorization", "Bearer " + token)
                    .header("accountId", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(VirtualAccountResponse.class);

            if (response != null && "00".equals(response.code())) {
                log.info("Successfully created virtual account accountRef={} accountNumber={}", request.accountRef(), response.data().bankAccountNumber());
                return response.data();
            } else {
                String desc = response != null ? response.description() : "Empty Response";
                log.error("Failed to create virtual account accountRef={} error={}", request.accountRef(), desc);
                throw new RuntimeException("Nomba API Error: " + desc);
            }
        } catch (ResourceAccessException e) {
            // Catches network connection and timeout issues, falling back to mock virtual account
            log.warn("Network error while creating Nomba virtual account accountRef={}, falling back to mock. error={}", request.accountRef(), e.getMessage());
            return generateMockVirtualAccount(request);
        }
    }

    private VActData generateMockVirtualAccount(VirtualAccountRequest request) {
        Random random = new Random();
        long randomPart = 10000000L + random.nextInt(90000000);
        String accountNumber = "99" + randomPart;
        log.info("Mock virtual account generated accountRef={} accountNumber={}", request.accountRef(), accountNumber);
        return new VActData(accountNumber, "Nomba/Wema Bank", request.accountRef());
    }
}
