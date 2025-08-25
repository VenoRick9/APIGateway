package by.baraznov.apigateway.dto;



import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record UserGetDTO(
        Integer id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        List<CardGetDTO> cards
) {
}
