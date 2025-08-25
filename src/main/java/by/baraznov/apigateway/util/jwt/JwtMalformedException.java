package by.baraznov.apigateway.util.jwt;

public class JwtMalformedException extends RuntimeException {
    public JwtMalformedException(String message) {
        super(message);
    }
}
