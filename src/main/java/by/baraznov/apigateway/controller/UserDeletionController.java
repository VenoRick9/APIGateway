package by.baraznov.apigateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;



@RestController
@RequestMapping("/gateway/users")
@RequiredArgsConstructor
@Slf4j
public class UserDeletionController {

    private final WebClient userClient;
    private final WebClient keycloakClient;

    @Value("${keycloak.admin.realm}")
    private String adminRealm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.realm}")
    private String targetRealm;


    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable UUID id) {
        return keycloakClient.post()
                .uri("/realms/" + adminRealm + "/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(String.format(
                        "grant_type=password&client_id=%s&username=%s&password=%s",
                        adminClientId,
                        adminUsername,
                        adminPassword
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(tokenResp -> {
                    String accessToken = (String) tokenResp.get("access_token");
                    String keycloakId = id.toString();
                    return keycloakClient.delete()
                            .uri("/admin/realms/" + targetRealm + "/users/" + keycloakId)
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .toBodilessEntity()
                            .flatMap(resp -> {
                                if (!resp.getStatusCode().is2xxSuccessful()) {
                                    return Mono.error(new RuntimeException(
                                            "Failed to delete user in Keycloak, status: " + resp.getStatusCode()));
                                }
                                return userClient.delete()
                                        .uri("/users/" + keycloakId)
                                        .retrieve()
                                        .toBodilessEntity()
                                        .map(r -> ResponseEntity.noContent().build());
                            });
                });
    }
}
