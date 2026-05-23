package org.renting.rentingservice.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
@Profile("!user & !notification")
@RequiredArgsConstructor
public class RemoteUserIdentityProvider implements UserIdentityProvider {

    private final RestClient userServiceRestClient;

    @Override
    public UserPrincipal loadPrincipal(Long userId, Claims claims, String rawToken) {
        // Verify token with user-service (ensures token is a valid access token and signature matches shared secret).
        // The response is not used beyond validation — userId still comes from the already-parsed JWT claims.
        try {
            Map<?, ?> response = userServiceRestClient.post()
                    .uri("/auth/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("token", rawToken))
                    .retrieve()
                    .body(Map.class);
            Object validatedId = response != null ? response.get("userId") : null;
            if (validatedId == null || Long.parseLong(String.valueOf(validatedId)) != userId) {
                throw new RestClientResponseException("Token validation mismatch", 401, "Unauthorized", null, null, null);
            }
        } catch (RestClientResponseException e) {
            throw e;
        }

        String email = claims.get("email", String.class);
        return UserPrincipal.minimal(userId, email);
    }
}

