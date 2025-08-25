package by.baraznov.apigateway.dto;


public record TokenResponseDTO(
        String accessToken,
        String refreshToken
) {
}
