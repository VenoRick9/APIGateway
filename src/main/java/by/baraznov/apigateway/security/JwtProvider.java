package by.baraznov.apigateway.security;

import by.baraznov.apigateway.util.jwt.JwtExpiredException;
import by.baraznov.apigateway.util.jwt.JwtMalformedException;
import by.baraznov.apigateway.util.jwt.JwtSignatureException;
import by.baraznov.apigateway.util.jwt.JwtValidationException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtProvider {
    private final SecretKey jwtAccessSecret;

    public JwtProvider(
            @Value("${jwt.secret.access}") String jwtAccessSecret
    ) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtAccessSecret)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException expEx) {
            throw new JwtExpiredException("Expired JWT token");
        } catch (SignatureException sEx) {
            throw new JwtSignatureException("Token signature is invalid");
        } catch (MalformedJwtException mjEx) {
            throw new JwtMalformedException("Malformed JWT token");
        } catch (Exception e) {
            throw new JwtValidationException("Invalid JWT token");
        }
    }
}
