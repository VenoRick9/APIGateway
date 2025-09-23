package by.baraznov.apigateway.dto;

import java.io.Serializable;
import java.time.LocalDate;

public record CardGetDTO(
        Integer id,
        Integer userId,
        String number,
        String holder,
        LocalDate expirationDate
){
}
