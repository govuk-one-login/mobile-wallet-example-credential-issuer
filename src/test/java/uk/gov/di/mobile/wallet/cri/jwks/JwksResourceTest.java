package uk.gov.di.mobile.wallet.cri.jwks;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.Response;
import org.bouncycastle.openssl.PEMException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyNotActiveException;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class JwksResourceTest {

    private static final JwksService jwksService = mock(JwksService.class);
    private final ResourceExtension resource =
            ResourceExtension.builder().addResource(new JwksResource(jwksService)).build();

    @BeforeEach
    void setUp() {
        Mockito.reset(jwksService);
    }

    @Test
    @DisplayName("Should return 500 when Jwks Service Throws an Exception")
    void should_Return_500_When_JwksService_ThrowsException()
            throws KeyNotActiveException, PEMException, NoSuchAlgorithmException {
        doThrow(new PEMException("Mock error message")).when(jwksService).generateJwks();

        final Response response = resource.target("/.well-known/jwks.json").request().get();

        verify(jwksService, Mockito.times(1)).generateJwks();
        assertThat(response.getStatus(), is(500));
        reset(jwksService);
    }

    @Test
    void should_Return_200_And_JwksJson()
            throws ParseException, KeyNotActiveException, PEMException, NoSuchAlgorithmException {
        JWK publicKey =
                JWK.parse(
                        "{\"kty\":\"EC\",\"crv\":\"P-256\",\"kid\":\"d7cb2ed24d8f70433e293ebc270bf1de77fcfab02a7f631da396b70e9b3aa8d7\",\"x\":\"sSdmBkED2EfjTdX-K2_cT6CfBwXQFt-DJ6v8-6tr_n8\",\"y\":\"WTXmQdqLwrmHN5tiFsTFUtNAvDYhhTQB4zyfteCrWIE\",\"alg\":\"ES256\",\"use\":\"sig\"}");
        var expectedJWKSet = new JWKSet(List.of(publicKey));
        when(jwksService.generateJwks()).thenReturn(expectedJWKSet);

        final Response response = resource.target("/.well-known/jwks.json").request().get();

        verify(jwksService, Mockito.times(1)).generateJwks();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(expectedJWKSet.toString()));
    }
}
