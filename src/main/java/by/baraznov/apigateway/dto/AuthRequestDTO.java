package by.baraznov.apigateway.dto;

public record AuthRequestDTO(
        Integer id,
        String login,
        String password
) {}

