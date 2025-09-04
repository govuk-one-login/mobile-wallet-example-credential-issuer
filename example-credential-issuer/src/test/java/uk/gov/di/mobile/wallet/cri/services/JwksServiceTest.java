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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;
import software.amazon.awssdk.services.kms.model.KeyMetadata;
import uk.gov.di.mobile.wallet.cri.credential.jwt.did_key.exceptions.AddressFormatException;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyNotActiveException;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

import java.net.MalformedURLException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
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
    private final JWKSource<SecurityContext> jwkSource = mock(JWKSource.class);
    private final KeyProvider kmsService = mock(KmsService.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private static final String TEST_ARN =
            "arn:aws:kms:eu-west-2:00000000000:key/1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String TEST_KEY_ID =
            "d7cb2ed24d8f70433e293ebc270bf1de77fcfab02a7f631da396b70e9b3aa8d7";
    private static final String TEST_PUBLIC_KEY_TYPE = "EC";

    @BeforeEach
    void setUp() {
        when(configurationService.getOneLoginAuthServerUrl())
                .thenReturn("https://test-authorization-server.gov.uk");
        when(configurationService.getSigningKeyAlias()).thenReturn("test-signing-key");
    }

    @Test
    void should_Return_Jwk_When_Found()
            throws AddressFormatException, KeySourceException, ParseException {
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
    @DisplayName("Should Throw KeySource Exception When Jwk not found")
    void should_ThrowException_When_Jwk_Not_Found()
            throws AddressFormatException, KeySourceException {
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
    void should_Test_Additional_Class_Constructor()
            throws AddressFormatException, MalformedURLException {
        jwksService = new JwksService(configurationService, kmsService);

        assertThat(jwksService, instanceOf(JwksService.class));
    }

    @Test
    void should_Return_PublicKey_As_Jwks()
            throws AddressFormatException,
                    MalformedURLException,
                    InvalidAlgorithmParameterException,
                    NoSuchAlgorithmException,
                    PEMException,
                    KeyNotActiveException {
        ECKey mockJwk = getMockJwk();
        when(kmsService.describeKey(any(DescribeKeyRequest.class)))
                .thenReturn(getMockDescribeKeyResponse(TEST_ARN, true, null));
        when(kmsService.isKeyActive(any(String.class))).thenReturn(true);
        when(kmsService.getPublicKey(any(String.class))).thenReturn(mockJwk);

        JWKSet result = new JwksService(configurationService, kmsService).generateJwks();
        JWK key = result.getKeyByKeyId(TEST_KEY_ID);
        assertEquals(mockJwk.toString(), key.toJSONString());
    }

    public static DescribeKeyResponse getMockDescribeKeyResponse(
            String keyId, boolean enabled, Instant deletionDate) {
        return DescribeKeyResponse.builder()
                .keyMetadata(
                        KeyMetadata.builder()
                                .keyId(keyId)
                                .enabled(enabled)
                                .deletionDate(deletionDate)
                                .build())
                .build();
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
