package com.example.federation;

import com.example.federation.Controller.FederationController;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FederationApplicationTests {

    @Autowired
    private FederationController federationController;

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts up without errors
    }

    @Test
    void controllerIsWired() {
        assertThat(federationController).isNotNull();
    }

    @Test
    void entityStatementIsValidSignedJwt() throws Exception {
        ResponseEntity<String> response = federationController.getEntityStatement();
        String body = response.getBody();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Must be a three-part compact serialisation
        assertThat(body.split("\\.")).hasSize(3);

        // Must parse as a valid SignedJWT
        SignedJWT jwt = SignedJWT.parse(body);
        assertThat(jwt).isNotNull();
    }

    @Test
    void entityStatementContainsRequiredClaims() throws Exception {
        ResponseEntity<String> response = federationController.getEntityStatement();
        String body = response.getBody();
        SignedJWT jwt = SignedJWT.parse(body);
        var claims = jwt.getJWTClaimsSet();

        assertThat(claims.getIssuer()).isEqualTo("http://localhost:8080");
        assertThat(claims.getSubject()).isEqualTo("http://localhost:8080");
        assertThat(claims.getIssueTime()).isNotNull();
        assertThat(claims.getExpirationTime()).isNotNull();
        assertThat(claims.getClaim("jwks")).isNotNull();
        assertThat(claims.getClaim("metadata")).isNotNull();
    }

    @Test
    void entityStatementHeaderHasCorrectType() throws Exception {
        ResponseEntity<String> response = federationController.getEntityStatement();
        String body = response.getBody();
        SignedJWT jwt = SignedJWT.parse(body);
        assertThat(jwt.getHeader().getType().getType()).isEqualTo("entity-statement+jwt");
        assertThat(jwt.getHeader().getAlgorithm().getName()).isEqualTo("RS256");
    }
}

