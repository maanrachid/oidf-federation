package com.example.federation.service;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import se.swedenconnect.security.credential.BasicCredential;
import se.swedenconnect.security.credential.PkiCredential;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class EntityStatementServiceTest {

    private final PkiCredential credential;
    private final EntityStatementService service;

    EntityStatementServiceTest() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.credential = new BasicCredential(keyPair.getPublic(), keyPair.getPrivate());
        this.service = new EntityStatementService(credential, "https://example.com");
    }

    @Test
    void buildSignedEntityStatement_returnsCompactJwt() throws Exception {
        String token = service.buildSignedEntityStatement();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void buildSignedEntityStatement_issuerMatchesEntityId() throws Exception {
        SignedJWT jwt = SignedJWT.parse(service.buildSignedEntityStatement());
        assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo("https://example.com");
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("https://example.com");
    }

    @Test
    void buildSignedEntityStatement_containsJwksAndMetadata() throws Exception {
        SignedJWT jwt = SignedJWT.parse(service.buildSignedEntityStatement());
        assertThat(jwt.getJWTClaimsSet().getClaim("jwks")).isNotNull();
        assertThat(jwt.getJWTClaimsSet().getClaim("metadata")).isNotNull();
    }

    @Test
    void buildSignedEntityStatement_usesCorrectTypAndAlg() throws Exception {
        SignedJWT jwt = SignedJWT.parse(service.buildSignedEntityStatement());
        assertThat(jwt.getHeader().getType().getType()).isEqualTo("entity-statement+jwt");
        assertThat(jwt.getHeader().getAlgorithm().getName()).isEqualTo("RS256");
    }

    @Test
    void buildSignedEntityStatement_metadataContainsOpenIdRelyingParty() throws Exception {
        SignedJWT jwt = SignedJWT.parse(service.buildSignedEntityStatement());
        @SuppressWarnings("unchecked")
        var metadata = (java.util.Map<String, Object>) jwt.getJWTClaimsSet().getClaim("metadata");
        assertThat(metadata).containsKey("openid_relying_party");
    }
}
