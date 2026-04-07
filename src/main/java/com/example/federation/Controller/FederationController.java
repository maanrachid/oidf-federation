package com.example.federation.Controller;

import com.example.federation.service.EntityStatementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the OpenID Federation entity statement endpoint.
 */
@RestController
public class FederationController {

    private static final Logger log = LoggerFactory.getLogger(FederationController.class);

    private final EntityStatementService entityStatementService;

    public FederationController(EntityStatementService entityStatementService) {
        this.entityStatementService = entityStatementService;
    }

    /**
     * Returns a signed entity statement JWT as required by OpenID Federation 1.0 spec.
     * Content-Type is {@code application/entity-statement+jwt}.
     *
     * <p>Example: {@code curl http://localhost:8080/.well-known/openid-federation}
     */
    @GetMapping(value = "/.well-known/openid-federation",
            produces = "application/entity-statement+jwt")
    public ResponseEntity<String> getEntityStatement() throws Exception {
        log.info("Incoming request: GET /.well-known/openid-federation");
        return ResponseEntity.ok(entityStatementService.buildSignedEntityStatement());
    }
}
