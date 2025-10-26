package by.baraznov.apigateway.dto;

import java.time.LocalDate;

public record FrontendRegistrationRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        String password,
        LocalDate birthday

) {
}
