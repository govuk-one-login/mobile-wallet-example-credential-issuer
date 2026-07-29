package uk.gov.di.mobile.wallet.cri.services;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.bouncycastle.openssl.PEMException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyNotActiveException;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwksServiceTest {

    private JwksService jwksService;
    @Mock private JWKSource<SecurityContext> jwkSource;
    @Mock private KeyProvider kmsService;
    @Mock private ConfigurationService configurationService;
    private static final String TEST_KEY_ID =
            "d7cb2ed24d8f70433e293ebc270bf1de77fcfab02a7f631da396b70e9b3aa8d7";
    private static final String TEST_PUBLIC_KEY_TYPE = "EC";

    @BeforeEach
    void setUp() {
        lenient().when(configurationService.getSigningKeyAlias()).thenReturn("test-signing-key");
    }

    @Test
    void should_ReturnMatchingJwk_WhenKeyIdExists() throws KeySourceException, ParseException {
        jwksService = new JwksService(configurationService, kmsService, jwkSource);
        JWK publicKey =
                JWK.parse(
                        "{\"kty\":\"EC\",\"crv\":\"P-256\",\"kid\":\"d7cb2ed24d8f70433e293ebc270bf1de77fcfab02a7f631da396b70e9b3aa8d7\",\"x\":\"sSdmBkED2EfjTdX-K2_cT6CfBwXQFt-DJ6v8-6tr_n8\",\"y\":\"WTXmQdqLwrmHN5tiFsTFUtNAvDYhhTQB4zyfteCrWIE\",\"alg\":\"ES256\"}");
        final List<JWK> jwkList = Collections.singletonList(publicKey);
        when(jwkSource.get(any(JWKSelector.class), isNull())).thenReturn(jwkList);

        JWK result = jwksService.retrieveJwkFromURLWithKeyId(TEST_KEY_ID);

        assertEquals(publicKey, result);
    }

    @Test
    void should_ThrowKeySourceException_When_KeyIdNotFound() throws KeySourceException {
        jwksService = new JwksService(configurationService, kmsService, jwkSource);
        final List<JWK> jwkList = Collections.emptyList();
        when(jwkSource.get(any(JWKSelector.class), isNull())).thenReturn(jwkList);

        KeySourceException exception =
                assertThrows(
                        KeySourceException.class,
                        () -> jwksService.retrieveJwkFromURLWithKeyId(TEST_KEY_ID));

        assertEquals(
                "No key found with key ID: d7cb2ed24d8f70433e293ebc270bf1de77fcfab02a7f631da396b70e9b3aa8d7",
                exception.getMessage());
    }

    @Test
    void should_ConstructWithoutThrowing() {
        jwksService = new JwksService(configurationService, kmsService);

        assertThat(jwksService, instanceOf(JwksService.class));
    }

    @Test
    void should_ThrowKeySourceException_When_JwksUrlIsMalformed() {
        when(configurationService.getOneLoginAuthServerUrl()).thenReturn("not a valid url");
        when(configurationService.getJwksEndpoint()).thenReturn("/.well-known/jwks.json");
        jwksService = new JwksService(configurationService, kmsService);

        KeySourceException exception =
                assertThrows(
                        KeySourceException.class,
                        () -> jwksService.retrieveJwkFromURLWithKeyId(TEST_KEY_ID));

        assertEquals("Failed to build JWKS URL", exception.getMessage());
    }

    @Test
    void should_BuildJwkSourceFromConfig_WhenUrlIsValid() {
        when(configurationService.getOneLoginAuthServerUrl())
                .thenReturn("https://oidc.example.com");
        when(configurationService.getJwksEndpoint()).thenReturn("/.well-known/jwks.json");
        jwksService = new JwksService(configurationService, kmsService);

        // Source is built successfully; exception comes from the subsequent network call
        KeySourceException exception =
                assertThrows(
                        KeySourceException.class,
                        () -> jwksService.retrieveJwkFromURLWithKeyId(TEST_KEY_ID));
        assertNotEquals("Failed to build JWKS URL", exception.getMessage());
        assertNotEquals("No key found with key ID: " + TEST_KEY_ID, exception.getMessage());
    }

    @Test
    void should_ReturnPublicKeyAsJwks_WhenKeyIsActive()
            throws InvalidAlgorithmParameterException,
                    NoSuchAlgorithmException,
                    PEMException,
                    KeyNotActiveException {
        ECKey mockJwk = getMockJwk();
        when(kmsService.isKeyActive(any(String.class))).thenReturn(true);
        when(kmsService.getPublicKey(any(String.class))).thenReturn(mockJwk);

        JWKSet result = new JwksService(configurationService, kmsService).generateJwks();
        JWK key = result.getKeyByKeyId(TEST_KEY_ID);
        assertEquals(mockJwk.toString(), key.toJSONString());
    }

    private ECKey getMockJwk() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(TEST_PUBLIC_KEY_TYPE);
        gen.initialize(Curve.P_256.toECParameterSpec());
        KeyPair keyPair = gen.generateKeyPair();

        return new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                .keyID(TEST_KEY_ID)
                .algorithm(ES256)
                .build();
    }
}
