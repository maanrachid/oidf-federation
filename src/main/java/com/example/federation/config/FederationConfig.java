package com.example.federation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.bundle.CredentialBundles;

/**
 * Spring configuration that exposes the PKI credential used for signing entity statements.
 * The credential is loaded via SwedenConnect credentials-support bundle configuration.
 */
@Configuration
public class FederationConfig {

    private static final Logger log = LoggerFactory.getLogger(FederationConfig.class);

    /**
     * Resolves the signing credential from the configured credentials-support bundle.
     */
    @Bean
    public PkiCredential federationSigningCredential(CredentialBundles credentialBundles) {
        log.info("Loading signing credential 'federation-signing' via credentials-support bundle configuration");
        PkiCredential credential = credentialBundles.getCredential("federation-signing");
        log.info("Signing credential '{}' loaded successfully", credential.getName());
        return credential;
    }
}
