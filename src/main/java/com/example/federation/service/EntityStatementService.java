package com.example.federation.service;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.nimbus.JwkTransformerFunction;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for building and signing the OpenID Federation entity statement.
 * <p>
 * Signing keys are obtained from a {@link PkiCredential} and transformed into a Nimbus
 * {@link JWK} using the SwedenConnect credentials-support {@link JwkTransformerFunction}.
 * The resulting self-signed entity statement is a compact-serialised signed JWT with
 * {@code typ: entity-statement+jwt}.
 */
@Service
public class EntityStatementService {

    private static final Logger log = LoggerFactory.getLogger(EntityStatementService.class);

    private final PkiCredential signingCredential;
    private final String entityId;

    public EntityStatementService(
            PkiCredential federationSigningCredential,
            @Value("${federation.entity-id}") String entityId) {
        this.signingCredential = federationSigningCredential;
        this.entityId = entityId;
    }

    /**
     * Builds and returns a compact-serialised, signed entity statement JWT.
     *
     * @return signed entity statement compact serialisation
     * @throws Exception if signing fails
     */
    public String buildSignedEntityStatement() throws Exception {
        log.info("Building entity statement for entity ID: {}", entityId);

        // Transform PkiCredential -> JWK via credentials-support Nimbus integration
        JWK jwk = signingCredential.transform(new JwkTransformerFunction());
        RSAKey rsaKey = (RSAKey) jwk;

        String keyId = rsaKey.getKeyID() != null ? rsaKey.getKeyID() : UUID.randomUUID().toString();
        log.debug("Using key ID '{}' for entity statement signing", keyId);

        // Public JWKS published in the entity statement
        JWKSet publicJwkSet = new JWKSet(rsaKey.toPublicJWK());

        // openid_relying_party metadata
        Map<String, Object> rpMetadata = new LinkedHashMap<>();
        rpMetadata.put("redirect_uris", List.of(entityId + "/callback"));
        rpMetadata.put("response_types", List.of("code"));
        rpMetadata.put("grant_types", List.of("authorization_code"));
        rpMetadata.put("subject_type", "public");
        rpMetadata.put("client_name", "Federation Demo RP");
        rpMetadata.put("token_endpoint_auth_method", "private_key_jwt");

        Map<String, Object> metadata = Map.of("openid_relying_party", rpMetadata);

        // Build JWT claims
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(entityId)
                .subject(entityId)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(24, ChronoUnit.HOURS)))
                .claim("jwks", publicJwkSet.toJSONObject())
                .claim("metadata", metadata)
                .build();

        // Build and sign the JWT
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(keyId)
                .type(new JOSEObjectType("entity-statement+jwt"))
                .build();

        SignedJWT signedJwt = new SignedJWT(header, claims);
        signedJwt.sign(new RSASSASigner(rsaKey));

        log.info("Entity statement signed successfully with key ID '{}'", keyId);
        return signedJwt.serialize();
    }
}
