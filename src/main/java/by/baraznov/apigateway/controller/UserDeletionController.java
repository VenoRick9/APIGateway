package by.baraznov.apigateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import by.baraznov.apigateway.security.JwtProvider;



@RestController
@RequestMapping("/gateway/users")
@RequiredArgsConstructor
@Slf4j
public class UserDeletionController {

    private final WebClient userClient;
    private final WebClient keycloakClient;


    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable UUID id) {
        return keycloakClient.post()
                .uri("/realms/master/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=password&client_id=admin-cli&username=admin&password=admin")
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(tokenResp -> {
                    String accessToken = (String) tokenResp.get("access_token");
                    String keycloakId = String.valueOf(id);
                    log.debug(keycloakId);
                    log.debug(accessToken);
                    return keycloakClient.delete()
                            .uri("/admin/realms/innowise-shop/users/" + keycloakId)
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .toBodilessEntity()
                            .flatMap(resp -> {
                                log.debug(resp.toString());
                                log.debug("We are here");
                                if (!resp.getStatusCode().is2xxSuccessful()) {
                                    return Mono.error(new RuntimeException("Failed to delete user in Keycloak, status: " + resp.getStatusCode()));
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
