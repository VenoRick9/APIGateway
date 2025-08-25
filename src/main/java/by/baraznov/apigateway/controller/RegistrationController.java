package by.baraznov.apigateway.controller;

import by.baraznov.apigateway.dto.AuthRequestDTO;
import by.baraznov.apigateway.dto.RegistrationDTO;
import by.baraznov.apigateway.dto.TokenResponseDTO;
import by.baraznov.apigateway.dto.UserCreateDTO;
import by.baraznov.apigateway.dto.UserGetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final WebClient userClient;
    private final WebClient authClient;

    @PostMapping("/registration")
    public Mono<ResponseEntity<TokenResponseDTO>> register(@RequestBody RegistrationDTO dto) {
        UserCreateDTO userDto = new UserCreateDTO(
                dto.name(), dto.surname(), dto.birthDate(), dto.email()
        );
        return userClient.post()
                .uri("/users")
                .bodyValue(userDto)
                .retrieve()
                .bodyToMono(UserGetDTO.class)
                .flatMap(user -> {
                    AuthRequestDTO authDto = new AuthRequestDTO(
                            user.id(), dto.login(), dto.password()
                    );
                    return authClient.post()
                            .uri("/auth/registration")
                            .bodyValue(authDto)
                            .retrieve()
                            .bodyToMono(TokenResponseDTO.class)
                            .onErrorResume(e -> userClient.delete()
                                    .uri("/users/{id}", user.id())
                                    .retrieve()
                                    .bodyToMono(Void.class)
                                    .then(Mono.error(new RuntimeException("Registration failed, rollback done"))));
                })
                .map(tokens -> ResponseEntity.status(HttpStatus.CREATED).body(tokens));
    }
}
