package by.baraznov.apigateway.security;
import by.baraznov.apigateway.util.jwt.JwtExpiredException;
import by.baraznov.apigateway.util.jwt.JwtValidationException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    @Value("${keycloak.url}")
    private String JWKS_URL;

    private JWKSet jwkSet;
    private Instant jwkSetExpiry;
    private static final long CACHE_DURATION_SECONDS = 300;

    public JwtProvider() {
        this.jwkSet = null;
        this.jwkSetExpiry = Instant.EPOCH;
    }

    private synchronized void loadJwkSetIfNeeded() throws IOException, ParseException {
        Instant now = Instant.now();
        if (jwkSet == null || now.isAfter(jwkSetExpiry)) {
            this.jwkSet = JWKSet.load(new URL(JWKS_URL));
            this.jwkSetExpiry = now.plusSeconds(CACHE_DURATION_SECONDS);
        }
    }

    public void validateToken(String token) {
        try {
            loadJwkSetIfNeeded();

            SignedJWT signedJWT = SignedJWT.parse(token);
            String kid = signedJWT.getHeader().getKeyID();
            JWK jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) {
                this.jwkSet = JWKSet.load(new URL(JWKS_URL));
                jwk = jwkSet.getKeyByKeyId(kid);
                if (jwk == null) {
                    throw new RuntimeException("Public key not found for kid: " + kid);
                }
            }

            RSAPublicKey publicKey = ((RSAKey) jwk).toRSAPublicKey();

            if (!signedJWT.verify(new RSASSAVerifier(publicKey))) {
                throw new JwtValidationException("Invalid JWT signature");
            }

            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (exp.before(new Date())) {
                throw new JwtExpiredException("JWT token expired");
            }

        } catch (ParseException | JOSEException | IOException e) {
            throw new RuntimeException("JWT validation failed", e);
        }
    }

    public UUID getAccessClaims(String token) {
        try {
            loadJwkSetIfNeeded();

            SignedJWT signedJWT = SignedJWT.parse(token);
            String kid = signedJWT.getHeader().getKeyID();
            JWK jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) {
                this.jwkSet = JWKSet.load(new URL(JWKS_URL));
                jwk = jwkSet.getKeyByKeyId(kid);
                if (jwk == null) {
                    throw new RuntimeException("Public key not found for kid: " + kid);
                }
            }
            return UUID.fromString(signedJWT.getJWTClaimsSet().getSubject());
        } catch (ParseException | IOException e) {
            throw new RuntimeException("JWT validation failed", e);
        }
    }
}
