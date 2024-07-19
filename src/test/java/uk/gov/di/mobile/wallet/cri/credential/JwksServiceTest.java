package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwksServiceTest {

    private JwksService jwksService;
    private final JWKSource jwkSource = mock(JWKSource.class);

    @Test
    void shouldReturnJwkWhenFound()
            throws AddressFormatException, KeySourceException, ParseException {
        jwksService = new JwksService(jwkSource);
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
    void shouldThrowKeySourceExceptionWhenJwkNotFound()
            throws AddressFormatException, KeySourceException {
        jwksService = new JwksService(jwkSource);
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

    @Test
    void shouldTestAdditionalClassConstructor()
            throws AddressFormatException, MalformedURLException {
        ConfigurationService configurationService = new ConfigurationService();
        jwksService = new JwksService(configurationService);

        assertThat(jwksService, instanceOf(JwksService.class));
    }
}
