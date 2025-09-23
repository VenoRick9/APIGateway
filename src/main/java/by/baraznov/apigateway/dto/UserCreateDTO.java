package by.baraznov.apigateway.dto;

import java.time.LocalDate;

public record UserCreateDTO(
        String name,
        String surname,
        LocalDate birthDate,
        String email
) {}

