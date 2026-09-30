package com.astrotech.transport.jwt;

import com.astrotech.transport.config.JwtConfig;
import com.astrotech.transport.exceptions.InvalidTokenException;
import com.astrotech.transport.exceptions.TokenEncryptionException;
import com.astrotech.transport.exceptions.TokenExpiredException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class Jwt {
    private static final JWEAlgorithm JWE_ALGORITHM = JWEAlgorithm.RSA_OAEP_256;
    private static final EncryptionMethod JWE_ENCRYPTION_METHOD = EncryptionMethod.A256GCM;
    private static final String JWT_CONTENT_TYPE = "JWT";
    private final JwtConfig jwtConfig;

    private final RSAKey signingPrivateKey;
    private final RSAKey signingPublicKey;

    private final RSAKey encryptionPrivateKey;
    private final RSAKey encryptionPublicKey;

    public Jwt(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;

        try {

            var signPriv =
                    RSAKeyParser.parsePrivateKey(jwtConfig.getSigningPrivateKey());

            var signPub =
                    RSAKeyParser.parsePublicKey(jwtConfig.getSigningPublicKey());


            var encPriv =
                    RSAKeyParser.parsePrivateKey(jwtConfig.getEncryptionPrivateKey());

            var encPub =
                    RSAKeyParser.parsePublicKey(jwtConfig.getEncryptionPublicKey());


            RSAKeyParser.validateKeyPair(signPriv, signPub, "signing");

            RSAKeyParser.validateKeyPair(encPriv, encPub, "encryption");

            this.signingPrivateKey = new RSAKey.Builder(signPub)
                    .privateKey(signPriv)
                    .keyID("transport-signing")
                    .build();

            this.signingPublicKey = new RSAKey.Builder(signPub)
                    .keyID("transport-signing")
                    .build();

            this.encryptionPrivateKey = new RSAKey.Builder(encPub)
                    .privateKey(encPriv)
                    .keyID("transport-encryption")
                    .build();

            this.encryptionPublicKey = new RSAKey.Builder(encPub)
                    .keyID("transport-encryption")
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to initialize JWT RSA keys",
                    e
            );
        }
    }


    public String buildToken(Map<String, Object> claims, String subject, SecretKey secretKey, Date expiration, boolean withSigningPrivateKey) {
        var now = System.currentTimeMillis();
        if (expiration == null || !expiration.after(new Date(now))) {
            throw new IllegalArgumentException("Token expiration must be in the future");
        }
        var jti = UUID.randomUUID().toString();

        try {
            String signedJwt;
            if (withSigningPrivateKey) {
                signedJwt = Jwts.builder()
                        .id(jti)
                        .claims(claims)
                        .subject(subject)
                        .issuer(jwtConfig.getIssuer())
                        .issuedAt(new Date(now))
                        .expiration(expiration)
                        .signWith(
                                signingPrivateKey.toPrivateKey(),
                                Jwts.SIG.RS256
                        )
                        .compact();

            } else {

                signedJwt = Jwts.builder()
                        .id(jti)
                        .claims(claims)
                        .subject(subject)
                        .issuer(jwtConfig.getIssuer())
                        .issuedAt(new Date(now))
                        .expiration(expiration)
                        .signWith(secretKey, Jwts.SIG.HS512)
                        .compact();
            }
            return encryptJweLayer(signedJwt);
        } catch (JOSEException e) {
            throw new InvalidTokenException("Error building token: " + e);
        }


    }


    public Claims getClaims(String jweString, SecretKey secretKey, boolean withSigningPublicKey) {
        if (jweString == null || jweString.isBlank()) {
            throw new InvalidTokenException("Token is missing");
        }
        if (secretKey == null) {
            throw new IllegalArgumentException("JWT signing key must not be null");
        }
        try {
            var signedJwt = decryptJweLayer(jweString);
            if (withSigningPublicKey) {
                return Jwts.parser()
                        .verifyWith(signingPublicKey.toPublicKey())
                        .requireIssuer(jwtConfig.getIssuer())
                        .build()
                        .parseSignedClaims(signedJwt)
                        .getPayload();
            } else {
                return Jwts.parser()
                        .verifyWith(secretKey)
                        .requireIssuer(jwtConfig.getIssuer())
                        .build()
                        .parseSignedClaims(signedJwt)
                        .getPayload();
            }


        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired", e);
        } catch (SignatureException e) {
            throw new InvalidTokenException("Invalid token signature: " + e);
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("Malformed JWT token: " + e);
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("Unsupported JWT token: " + e);
        } catch (IllegalArgumentException | JwtException e) {
            throw new InvalidTokenException("Invalid JWT token: " + e);
        } catch (JOSEException e) {
            throw new TokenExpiredException("Error getting token: " + e);
        }
    }


    public Optional<Claims> validateAndExtractClaims(String token, boolean withSigningPublicKey) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(getClaims(token, jwtConfig.getSecretKey(), withSigningPublicKey));
        } catch (TokenExpiredException | InvalidTokenException e) {
            return Optional.empty();
        }
    }

    private String encryptJweLayer(String signedJwt) {
        if (signedJwt == null || signedJwt.isBlank()) {
            throw new IllegalArgumentException("Signed JWT must not be empty");
        }
        try {
            var header = new JWEHeader.Builder(JWE_ALGORITHM, JWE_ENCRYPTION_METHOD).contentType(JWT_CONTENT_TYPE).build();
            var jweObject = new JWEObject(header, new Payload(signedJwt));
            var encrypter = new RSAEncrypter(encryptionPublicKey);
            jweObject.encrypt(encrypter);
            return jweObject.serialize();
        } catch (JOSEException e) {
            log.error("Failed to encrypt JWT using JWE", e);
            throw new TokenEncryptionException("Failed to encrypt token: " + e);
        }
    }


    private String decryptJweLayer(String jweString) {
        try {
            var jweObject = JWEObject.parse(jweString);
            var header = jweObject.getHeader();
            if (!JWE_ALGORITHM.equals(header.getAlgorithm())) {
                throw new InvalidTokenException("Unsupported JWE algorithm");
            }
            if (!JWE_ENCRYPTION_METHOD.equals(header.getEncryptionMethod())) {
                throw new InvalidTokenException("Unsupported JWE encryption method");
            }
            if (!JWT_CONTENT_TYPE.equals(header.getContentType())) {
                throw new InvalidTokenException("Invalid JWE content type");
            }
            var decrypter = new RSADecrypter(encryptionPrivateKey);
            jweObject.decrypt(decrypter);
            var payload = jweObject.getPayload();
            if (payload == null) {
                throw new InvalidTokenException("JWE payload is missing");
            }
            var signedJwt = payload.toString();
            if (signedJwt.isBlank()) {
                throw new InvalidTokenException("JWE payload is empty");
            }
            return signedJwt;
        } catch (JOSEException |
                 ParseException e) {
            log.debug("Failed to decrypt JWE token");
            throw new InvalidTokenException("Invalid encrypted token: " + e);
        }
    }

}
