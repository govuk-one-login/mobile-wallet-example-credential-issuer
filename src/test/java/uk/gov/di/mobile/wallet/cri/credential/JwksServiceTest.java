package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwksServiceTest {

    private JwksService jwksService;

    private final JWKSource jwkSource = mock(JWKSource.class);

    @BeforeEach
    void setup() throws KeySourceException, ParseException {
        final JWKSelector jwkSelector = mock(JWKSelector.class);

        JWK publicKey =
                JWK.parse(
                        "{\"kty\":\"EC\",\"crv\":\"P-256\",\"kid\":\"cb5a1a8b-809a-4f32-944d-caae1a57ed91\",\"x\":\"sSdmBkED2EfjTdX-K2_cT6CfBwXQFt-DJ6v8-6tr_n8\",\"y\":\"WTXmQdqLwrmHN5tiFsTFUtNAvDYhhTQB4zyfteCrWIE\",\"alg\":\"ES256\"}");
        final List<JWK> jwkList = Collections.singletonList(publicKey);


        jwksService = new JwksService(jwkSource);
        when(jwkSource.get(any(JWKSelector.class), isNull())).thenReturn(jwkList);
    }

    @Test
    void shouldDecodeBase58EncodedInput() throws AddressFormatException, KeySourceException, MalformedURLException {

//        final JWKSelector jwkSelector = mock(JWKSelector.class);
//        final List<JWK> jwkList = Collections.emptyList();
//        when(jwkSource.get(jwkSelector, null)).thenReturn(jwkList);
        assertEquals("", jwksService.retrieveJwkFromURLWithKeyId(new URL("https://localhost:8080"), "cb5a1a8b-809a-4f32-944d-caae1a57ed91"));
    }


}