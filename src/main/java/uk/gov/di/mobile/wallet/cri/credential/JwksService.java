package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.MalformedURLException;
import java.net.URL;

public class JwksService {

    private final JWKSource<SecurityContext> jwkSource;

    public JwksService(ConfigurationService configurationService) throws MalformedURLException {
        URL url =
                new URL(
                        configurationService.getOneLoginAuthServerUrl()
                                + configurationService.getAuthServerJwksPath());
        this.jwkSource =
                JWKSourceBuilder.create(url)
                        .retrying(true)
                        .refreshAheadCache(false)
                        .cache(false)
                        .rateLimited(false)
                        .build();
    }

    public JwksService(JWKSource<SecurityContext> jwkSource) {
        this.jwkSource = jwkSource;
    }

    public JWK retrieveJwkFromURLWithKeyId(String keyId) throws KeySourceException {
        JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().keyID(keyId).build());
        return jwkSource.get(selector, null).stream()
                .findFirst()
                .orElseThrow(() -> new KeySourceException("No key found with key ID: " + keyId));
    }
}
