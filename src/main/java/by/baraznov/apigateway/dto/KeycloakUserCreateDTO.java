package by.baraznov.apigateway.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record KeycloakUserCreateDTO(
        String username,
        String email,
        String firstName,
        String lastName,
        String password
) {
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("email", email);
        map.put("firstName", firstName);
        map.put("lastName", lastName);

        Map<String, Object> cred = new HashMap<>();
        cred.put("type", "password");
        cred.put("value", password);
        cred.put("temporary", false);
        map.put("credentials", List.of(cred));

        map.put("enabled", true);
        return map;
    }
}
