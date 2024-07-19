package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;

import java.net.URL;

public class JwksService {

    private final JWKSource<SecurityContext> jwkSource;

    public JwksService(URL url) {
        this.jwkSource =  JWKSourceBuilder.create(url)
                .retrying(true)
                .refreshAheadCache(false)
                .cache(false)
                .rateLimited(false)
                .build();;
    }

    public JwksService(JWKSource jwkSource) {
        this.jwkSource = jwkSource;
    }

    public JWK retrieveJwkFromURLWithKeyId(URL url, String keyId) throws KeySourceException {
        JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().keyID(keyId).build());
//        JWKSource<SecurityContext> jwkSource =
//                JWKSourceBuilder.create(url)
//                        .retrying(true)
//                        .refreshAheadCache(false)
//                        .cache(false)
//                        .rateLimited(false)
//                        .build();
        System.out.println(jwkSource.get(selector, null).stream().toList());
        return jwkSource.get(selector, null).stream()
                .findFirst()
                .orElseThrow(() -> new KeySourceException("No key found with key ID: " + keyId));
    }
}
