package by.baraznov.apigateway.util;

public class RegistrationFailed extends RuntimeException {
    public RegistrationFailed(String message, Throwable cause) {
        super(message, cause);
        System.out.println(cause.getMessage());
    }
}
