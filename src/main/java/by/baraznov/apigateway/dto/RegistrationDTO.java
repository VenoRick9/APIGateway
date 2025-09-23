package by.baraznov.apigateway.dto;

import java.time.LocalDate;

public record RegistrationDTO (
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        String login,
        String password
){
}
