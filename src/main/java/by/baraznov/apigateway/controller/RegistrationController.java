package by.baraznov.apigateway.controller;


import by.baraznov.apigateway.dto.KeycloakUserGetDTO;
import by.baraznov.apigateway.dto.RegistrationDTO;

import by.baraznov.apigateway.dto.UserCreateDTO;
import by.baraznov.apigateway.dto.UserGetDTO;
import by.baraznov.apigateway.util.RegistrationFailed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final WebClient userClient;
    private final WebClient keycloakClient;

    @PostMapping("/registration")
    public Mono<ResponseEntity<UserGetDTO>> register(@RequestBody RegistrationDTO dto) {
        return keycloakClient.post()
                .uri("/realms/master/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=password&client_id=admin-cli&username=admin&password=admin")
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(tokenResp -> {
                    String accessToken = (String) tokenResp.get("access_token");
                    Map<String, Object> kcPayload = new HashMap<>();
                    kcPayload.put("username", dto.login());
                    kcPayload.put("email", dto.email());
                    kcPayload.put("firstName", dto.name());
                    kcPayload.put("lastName", dto.surname());
                    kcPayload.put("enabled", true);
                    Map<String, Object> credentials = new HashMap<>();
                    credentials.put("type", "password");
                    credentials.put("value", dto.password());
                    credentials.put("temporary", false);
                    kcPayload.put("credentials", List.of(credentials));

                    return keycloakClient.post()
                            .uri("/admin/realms/innowise-shop/users")
                            .header("Authorization", "Bearer " + accessToken)
                            .bodyValue(kcPayload)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .then(
                                    keycloakClient.get()
                                            .uri(uriBuilder -> uriBuilder
                                                    .path("/admin/realms/innowise-shop/users")
                                                    .queryParam("username", dto.login())
                                                    .build())
                                            .header("Authorization", "Bearer " + accessToken)
                                            .retrieve()
                                            .bodyToFlux(KeycloakUserGetDTO.class)
                                            .next()
                            );
                })
                .flatMap(kcUserGet -> {
                    UUID keycloakId = UUID.fromString(kcUserGet.id());
                    UserCreateDTO userDto = new UserCreateDTO(
                            keycloakId,
                            dto.name(),
                            dto.surname(),
                            dto.birthDate(),
                            dto.email()
                    );
                    return userClient.post()
                            .uri("/users")
                            .bodyValue(userDto)
                            .retrieve()
                            .bodyToMono(UserGetDTO.class);
                })
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
                .onErrorResume(kcError -> Mono.error(new RegistrationFailed(
                        "Failed to register user in Keycloak", kcError)));
    }

}
