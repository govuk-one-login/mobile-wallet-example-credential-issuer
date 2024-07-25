package uk.gov.di.mobile.wallet.cri.services.signing;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyType;
import org.bouncycastle.openssl.PEMException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.*;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KmsServiceTest {

    private final KmsService kmsService = mock(KmsService.class);
    private static final String TEST_KEY_ID = "1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String HASHED_TEST_KEY_ID =
            "0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2";
    private static final String TEST_ARN =
            "arn:aws:kms:eu-west-2:00000000000:key/1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String TEST_KEY_ALIAS = "test-signing-key";

    @Test
    void shouldCreateKmsService() {
        assertNotNull(kmsService);
    }

    @Test
    void shouldReturnKeyId() {
        when(kmsService.describeKey(any(DescribeKeyRequest.class)))
                .thenReturn(getMockDescribeKeyResponse(TEST_KEY_ID, true, null));
        when(kmsService.getKeyId(TEST_KEY_ALIAS)).thenCallRealMethod();

        String keyId = kmsService.getKeyId(TEST_KEY_ALIAS);
        assertEquals("1234abcd-12ab-34cd-56ef-1234567890ab", keyId);
    }

    @Test
    void shouldReturnPublicKeyAsJwk() throws PEMException, NoSuchAlgorithmException {
        String mockPublicKey =
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEZS4QGXEhtywj9ivxlgx1dIJkFS7l2TInfT9r3Onmpvq64gfgiSQcFQ6eBIJDb9udSzWgi9+Z4Ls+wRkRqzghgQ==";
        when(kmsService.describeKey(any(DescribeKeyRequest.class)))
                .thenReturn(getMockDescribeKeyResponse(TEST_KEY_ID, true, null));
        when(kmsService.getKmsPublicKey(TEST_KEY_ALIAS))
                .thenReturn(getMockPublicKeyResponse(TEST_ARN, mockPublicKey));

        when(kmsService.getPublicKey(TEST_KEY_ALIAS)).thenCallRealMethod();

        ECKey publicKey = kmsService.getPublicKey(TEST_KEY_ALIAS);
        assertEquals(HASHED_TEST_KEY_ID, publicKey.getKeyID());
        assertEquals(Curve.P_256, publicKey.getCurve());
        assertEquals(KeyType.EC, publicKey.getKeyType());
        assertEquals("uuIH4IkkHBUOngSCQ2_bnUs1oIvfmeC7PsEZEas4IYE", publicKey.getY().toString());
        assertEquals("ZS4QGXEhtywj9ivxlgx1dIJkFS7l2TInfT9r3Onmpvo", publicKey.getX().toString());
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

    public static GetPublicKeyResponse getMockPublicKeyResponse(String keyArn, String keyString) {
        byte[] publicKey = Base64.getDecoder().decode(keyString);
        return GetPublicKeyResponse.builder()
                .keyId(keyArn)
                .publicKey(SdkBytes.fromByteArray(publicKey))
                .signingAlgorithms(SigningAlgorithmSpec.ECDSA_SHA_256)
                .build();
    }
}
