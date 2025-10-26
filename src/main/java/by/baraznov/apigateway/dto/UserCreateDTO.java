package by.baraznov.apigateway.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserCreateDTO(
        UUID id,
        String name,
        String surname,
        LocalDate birthDate,
        String email

) {}

