package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void setup() {
        jwksService = new JwksService(jwkSource);
    }

    @Test
    void shouldReturnJwksIsFound()
            throws AddressFormatException,
                    KeySourceException,
                    ParseException {
        JWK publicKey =
                JWK.parse(
                        "{\"kty\":\"EC\",\"crv\":\"P-256\",\"kid\":\"cb5a1a8b-809a-4f32-944d-caae1a57ed91\",\"x\":\"sSdmBkED2EfjTdX-K2_cT6CfBwXQFt-DJ6v8-6tr_n8\",\"y\":\"WTXmQdqLwrmHN5tiFsTFUtNAvDYhhTQB4zyfteCrWIE\",\"alg\":\"ES256\"}");
        final List<JWK> jwkList = Collections.singletonList(publicKey);
        when(jwkSource.get(any(JWKSelector.class), isNull())).thenReturn(jwkList);

        JWK response =
                jwksService.retrieveJwkFromURLWithKeyId("cb5a1a8b-809a-4f32-944d-caae1a57ed91");

        assertEquals(publicKey, response);
    }

    @Test
    void shouldThrowKeySourceExceptionWhenJwksNotFound()
            throws AddressFormatException, KeySourceException {
        final List<JWK> jwkList = Collections.emptyList();
        when(jwkSource.get(any(JWKSelector.class), isNull())).thenReturn(jwkList);

        KeySourceException exception =
                assertThrows(
                        KeySourceException.class,
                        () ->
                                jwksService.retrieveJwkFromURLWithKeyId(
                                        "cb5a1a8b-809a-4f32-944d-caae1a57ed91"));

        assertEquals(
                "No key found with key ID: cb5a1a8b-809a-4f32-944d-caae1a57ed91",
                exception.getMessage());
    }
}
