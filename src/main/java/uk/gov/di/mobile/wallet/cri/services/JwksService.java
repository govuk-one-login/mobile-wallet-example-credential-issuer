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

public class JwksService {

    private final JWKSource<SecurityContext> jwkSource;
    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;
    private static final String JWKS_PATH = "/.well-known/jwks.json"; // NOSONAR

    public JwksService(ConfigurationService configurationService, KeyProvider keyProvider)
            throws MalformedURLException {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        URL url = new URL(configurationService.getOneLoginAuthServerUrl() + JWKS_PATH);
        this.jwkSource =
                JWKSourceBuilder.create(url)
                        .retrying(true)
                        .refreshAheadCache(false)
                        .cache(false)
                        .rateLimited(false)
                        .build();
    }

    public JwksService(
            ConfigurationService configurationService,
            KeyProvider keyProvider,
            JWKSource<SecurityContext> jwkSource) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.jwkSource = jwkSource;
    }

    public JWK retrieveJwkFromURLWithKeyId(String keyId) throws KeySourceException {
        JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().keyID(keyId).build());
        return jwkSource.get(selector, null).stream()
                .findFirst()
                .orElseThrow(() -> new KeySourceException("No key found with key ID: " + keyId));
    }

    public JWKSet generateJwks()
            throws PEMException, NoSuchAlgorithmException, KeyNotActiveException {

        String keyAlias = configurationService.getSigningKeyAlias();
        if (!keyProvider.isKeyActive(keyAlias)) {
            throw new KeyNotActiveException("Public key is not active");
        }

        return new JWKSet(keyProvider.getPublicKey(keyAlias));
    }
}
