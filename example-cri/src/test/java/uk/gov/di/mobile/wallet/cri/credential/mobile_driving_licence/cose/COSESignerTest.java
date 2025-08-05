package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class COSESignerTest {

    @Mock private CBOREncoder cborEncoder;
    @Mock private KeyProvider keyProvider;
    @Mock private X509Certificate certificate;
    @Mock private SignResponse signResponse;

    private COSESigner coseSigner;

    private static final byte[] TEST_PAYLOAD = "test payload".getBytes();
    private static final byte[] TEST_CERTIFICATE_ENCODED = "certificate data".getBytes();
    private static final byte[] TEST_PROTECTED_HEADER_ENCODED = "protected header".getBytes();
    private static final byte[] TEST_SIG_STRUCTURE = "sig structure".getBytes();
    private static final byte[] TEST_SIGNATURE = "signature".getBytes();
    private static final String TEST_KEY_ARN = "test-key-arn";

    @BeforeEach
    void setUp() {
        coseSigner = new COSESigner(cborEncoder, keyProvider, TEST_KEY_ARN);
    }

    @Test
    void Should_SuccessfullySignPayload() throws Exception {
        // Arrange: Setup mocks for successful signing
        when(certificate.getEncoded()).thenReturn(TEST_CERTIFICATE_ENCODED);
        when(cborEncoder.encode(any()))
                .thenReturn(TEST_PROTECTED_HEADER_ENCODED, TEST_SIG_STRUCTURE);
        when(keyProvider.sign(any(SignRequest.class))).thenReturn(signResponse);
        when(signResponse.signature()).thenReturn(SdkBytes.fromByteArray(TEST_SIGNATURE));

        // Act: Execute the sign method
        COSESign1 result = coseSigner.sign(TEST_PAYLOAD, certificate);

        // Assert: Verify the result is not null
        assertNotNull(result.protectedHeader());
        assertNotNull(result.unprotectedHeader());
        assertNotNull(result.payload());
        assertNotNull(result.signature());
        // Assert: Verify all expected interactions occurred
        verify(certificate).getEncoded();
        verify(cborEncoder, times(2))
                .encode(any()); // Once for protected header, once for sig structure
        verify(keyProvider).sign(any(SignRequest.class));
    }

    @Test
    void Should_CreatCorrectSigStructureFormat() throws Exception {
        // Arrange: Setup mocks for successful signing
        when(certificate.getEncoded()).thenReturn(TEST_CERTIFICATE_ENCODED);
        when(cborEncoder.encode(any()))
                .thenReturn(TEST_PROTECTED_HEADER_ENCODED, TEST_SIG_STRUCTURE);
        when(keyProvider.sign(any(SignRequest.class))).thenReturn(signResponse);
        when(signResponse.signature()).thenReturn(SdkBytes.fromByteArray(TEST_SIGNATURE));

        // Act: Execute the sign method
        coseSigner.sign(TEST_PAYLOAD, certificate);

        // Capture the argument passed to cborEncoder.encode() method
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(cborEncoder).encode(captor.capture());
        Object[] sigStructure = captor.getValue();
        // Assert
        assertEquals(4, sigStructure.length, "Sig_structure should have 4 elements");
        assertEquals("Signature1", sigStructure[0], "First element should be 'Signature1'");
        assertArrayEquals(
                TEST_PROTECTED_HEADER_ENCODED,
                (byte[]) sigStructure[1],
                "Second element should be the protected header bytes");
        assertTrue(sigStructure[2] instanceof byte[], "Third element should be a byte array");
        assertEquals(
                0,
                ((byte[]) sigStructure[2]).length,
                "Third element (external_aad) should be an empty byte array");
        assertArrayEquals(
                TEST_PAYLOAD,
                (byte[]) sigStructure[3],
                "Fourth element should be the payload bytes");
    }

    @Test
    void Should_UseCorrectSigningParameters() throws Exception {
        // Arrange: Setup mocks for successful signing
        when(certificate.getEncoded()).thenReturn(TEST_CERTIFICATE_ENCODED);
        when(cborEncoder.encode(any()))
                .thenReturn(TEST_PROTECTED_HEADER_ENCODED, TEST_SIG_STRUCTURE);
        when(keyProvider.sign(any(SignRequest.class))).thenReturn(signResponse);
        when(signResponse.signature()).thenReturn(SdkBytes.fromByteArray(TEST_SIGNATURE));

        // Act: Execute the sign method
        coseSigner.sign(TEST_PAYLOAD, certificate);

        // Assert: Verify that KMS signing uses correct parameters
        // Should use "DIGEST" message type and "ECDSA_SHA_256" algorithm
        verify(keyProvider)
                .sign(
                        argThat(
                                signRequest ->
                                        signRequest.keyId().equals(TEST_KEY_ARN) // Correct key ID
                                                && signRequest.messageType()
                                                        == MessageType
                                                                .DIGEST // Signing a hash digest
                                                && signRequest.signingAlgorithm()
                                                        == SigningAlgorithmSpec
                                                                .ECDSA_SHA_256 // ES256 algorithm
                                ));
    }

    @Test
    void Should_ThrowCertificateEncodingException_When_CertificateEncodingFails() throws Exception {
        // Arrange: Setup mock to throw exception when encoding certificate
        when(certificate.getEncoded())
                .thenThrow(new CertificateEncodingException("Certificate encoding failed"));

        // Act & Assert: Execute and verify exception is thrown
        assertThrows(
                CertificateEncodingException.class,
                () -> coseSigner.sign(TEST_PAYLOAD, certificate));
    }

    @Test
    void Should_ThrowSigningException_When_KeyProviderFails() throws Exception {
        // Arrange: Setup mocks up to the point where signing fails
        when(certificate.getEncoded()).thenReturn(TEST_CERTIFICATE_ENCODED);
        when(cborEncoder.encode(any()))
                .thenReturn(TEST_PROTECTED_HEADER_ENCODED, TEST_SIG_STRUCTURE);
        // Make the signing operation fail
        when(keyProvider.sign(any(SignRequest.class)))
                .thenThrow(new RuntimeException("KMS signing failed"));

        // Act & Assert: Execute and verify custom SigningException is thrown
        SigningException exception =
                assertThrows(
                        SigningException.class, () -> coseSigner.sign(TEST_PAYLOAD, certificate));
        assertEquals("Error signing MSO: KMS signing failed", exception.getMessage());
    }

    @Test
    void Should_ThrowMDLException_When_CborEncodingFails() throws Exception {
        // Arrange: Setup certificate encoding to succeed but CBOR encoding to fail
        when(certificate.getEncoded()).thenReturn(TEST_CERTIFICATE_ENCODED);
        when(cborEncoder.encode(any()))
                .thenThrow(new MDLException("CBOR encoding failed", new Exception()));

        // Act & Assert: Execute and verify MDLException is thrown
        assertThrows(RuntimeException.class, () -> coseSigner.sign(TEST_PAYLOAD, certificate));
    }
}
