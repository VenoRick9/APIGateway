package by.baraznov.apigateway.dto;

public record KeycloakUserGetDTO(
        String id,
        String username,
        String email
) {}