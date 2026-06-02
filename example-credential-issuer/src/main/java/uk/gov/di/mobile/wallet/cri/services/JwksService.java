package uk.gov.di.mobile.wallet.cri.services;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import org.bouncycastle.openssl.PEMException;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyNotActiveException;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

/**
 * Service for managing JSON Web Key Sets (JWKS).
 *
 * <p>Provides two capabilities:
 *
 * <ul>
 *   <li>Retrieving public keys from the authorization server's JWKS endpoint for access token
 *       signature verification.
 *   <li>Generating the credential issuer's own JWKS from KMS for publication.
 * </ul>
 *
 * <p>The JWKS source is lazily initialised on first use and cached for subsequent calls.
 */
public class JwksService {

    private JWKSource<SecurityContext> jwkSource;
    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;

    /**
     * Constructs a JwksService that lazily resolves the JWKS endpoint at runtime.
     *
     * @param configurationService Service providing the authorization server URL and JWKS endpoint.
     * @param keyProvider Provider for signing key operations.
     */
    public JwksService(ConfigurationService configurationService, KeyProvider keyProvider) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
    }

    /**
     * Constructs a JwksService with a pre-configured JWK source. Intended for testing.
     *
     * @param configurationService Service providing configuration values.
     * @param keyProvider Provider for signing key operations.
     * @param jwkSource Pre-configured JWK source to use for key retrieval.
     */
    public JwksService(
            ConfigurationService configurationService,
            KeyProvider keyProvider,
            JWKSource<SecurityContext> jwkSource) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.jwkSource = jwkSource;
    }

    /**
     * Retrieves a JWK from the authorization server's JWKS endpoint by key ID.
     *
     * @param keyId The key ID to search for.
     * @return The matching JWK.
     * @throws KeySourceException If the JWKS URL is malformed, the endpoint cannot be reached, or
     *     no key is found with the given ID.
     */
    public JWK retrieveJwkFromURLWithKeyId(String keyId) throws KeySourceException {
        JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().keyID(keyId).build());
        return getJwkSource().get(selector, null).stream()
                .findFirst()
                .orElseThrow(() -> new KeySourceException("No key found with key ID: " + keyId));
    }

    /**
     * Generates the credential issuer's public JWKS from KMS.
     *
     * @return A JWKSet containing the issuer's public signing key.
     * @throws PEMException If the public key cannot be parsed.
     * @throws NoSuchAlgorithmException If the required algorithm is not available.
     * @throws KeyNotActiveException If the signing key is not in an active state.
     */
    public JWKSet generateJwks()
            throws PEMException, NoSuchAlgorithmException, KeyNotActiveException {

        String keyAlias = configurationService.getSigningKeyAlias();
        if (!keyProvider.isKeyActive(keyAlias)) {
            throw new KeyNotActiveException("Public key is not active");
        }

        return new JWKSet(keyProvider.getPublicKey(keyAlias));
    }

    private JWKSource<SecurityContext> getJwkSource() throws KeySourceException {
        if (jwkSource == null) {
            try {
                URL url =
                        new URL(
                                configurationService.getOneLoginAuthServerUrl()
                                        + configurationService.getJwksEndpoint());
                jwkSource =
                        JWKSourceBuilder.create(url)
                                .retrying(true)
                                .refreshAheadCache(false)
                                .cache(false)
                                .rateLimited(false)
                                .build();
            } catch (MalformedURLException e) {
                throw new KeySourceException("Failed to build JWKS URL", e);
            }
        }
        return jwkSource;
    }
}
