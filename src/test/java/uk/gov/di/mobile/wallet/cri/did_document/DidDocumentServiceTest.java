package uk.gov.di.mobile.wallet.cri.did_document;

import org.bouncycastle.openssl.PEMException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.*;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class DidDocumentServiceTest {
    private DidDocumentService didDocumentService;
    private final KmsService kmsService = mock(KmsService.class);
    private final ConfigurationService configurationService = new ConfigurationService();
    private static final String KEY_ID =
            "arn:aws:kms:eu-west-2:00000000000:key/1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEZS4QGXEhtywj9ivxlgx1dIJkFS7l2TInfT9r3Onmpvq64gfgiSQcFQ6eBIJDb9udSzWgi9+Z4Ls+wRkRqzghgQ==";

    @BeforeEach
    void setUp() {
        didDocumentService = new DidDocumentService(configurationService, kmsService);
    }

    @Test
    void shouldReturnDidDocument() throws PEMException, NoSuchAlgorithmException {
        when(kmsService.getPublicKey(any(GetPublicKeyRequest.class)))
                .thenReturn(getMockPublicKeyResponse(KEY_ID, PUBLIC_KEY));
        when(kmsService.describeKey(any(DescribeKeyRequest.class)))
                .thenReturn(getMockDescribeKeyResponse(KEY_ID, true, null));

        DidDocument didDocument = didDocumentService.generateDidDocument();
        assertEquals("did:web:localhost:8080", didDocument.getId());
        assertEquals(
                List.of("https://www.w3.org/ns/did/v1", "https://www.w3.org/ns/security/jwk/v1"),
                didDocument.getContext());
        assertEquals(1, didDocument.getVerificationMethod().size());
        assertEquals(1, didDocument.getAssertionMethod().size());
        assertEquals(
                "did:web:localhost:8080#0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2",
                didDocument.getAssertionMethod().get(0));

        Did did = didDocument.getVerificationMethod().get(0);
        assertEquals(
                "did:web:localhost:8080#0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2",
                did.getId());
        assertEquals("did:web:localhost:8080", did.getController());
        assertEquals("JsonWebKey2020", did.getType());

        PublicKeyJwk jwk = did.getPublicKeyJwk();
        assertEquals(
                "0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2", jwk.getKid());
        assertEquals("EC", jwk.getKty());
        assertEquals("P-256", jwk.getCrv());
        assertEquals("ZS4QGXEhtywj9ivxlgx1dIJkFS7l2TInfT9r3Onmpvo", jwk.getX());
        assertEquals("uuIH4IkkHBUOngSCQ2_bnUs1oIvfmeC7PsEZEas4IYE", jwk.getY());
    }

    public static GetPublicKeyResponse getMockPublicKeyResponse(String keyId, String keyString) {
        byte[] publicKey = Base64.getDecoder().decode(keyString);
        return GetPublicKeyResponse.builder()
                .keyId(keyId)
                .publicKey(SdkBytes.fromByteArray(publicKey))
                .signingAlgorithms(SigningAlgorithmSpec.ECDSA_SHA_256)
                .build();
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
}
