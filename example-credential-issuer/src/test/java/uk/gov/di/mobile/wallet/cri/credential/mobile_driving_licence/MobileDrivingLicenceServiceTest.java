package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Namespaces;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.NamespacesFactory;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.interfaces.ECPublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileDrivingLicenceServiceTest {

    @Mock private CBOREncoder cborEncoder;
    @Mock private NamespacesFactory namespacesFactory;
    @Mock private IssuerSignedFactory issuerSignedFactory;
    @Mock private DrivingLicenceDocument mockDrivingLicenceDocument;
    @Mock private Namespaces namespaces;
    @Mock private IssuerSigned issuerSigned;
    @Mock private ECPublicKey mockEcPublicKey;
    private MobileDrivingLicenceService mobileDrivingLicenceService;

    private static final long CREDENTIAL_TTL_MINUTES = 43200L;

    @BeforeEach
    void setUp() {
        mobileDrivingLicenceService =
                new MobileDrivingLicenceService(
                        cborEncoder, namespacesFactory, issuerSignedFactory);
    }

    @Test
    void Should_ReturnBase64UrlEncodedIssuerSigned() throws Exception {
        byte[] mockCborData =
                new byte[] {
                    0x00, 0x01, 0x02, 0x03, 0x04,
                    0x05, 0x06, 0x07, 0x08, 0x09
                }; // 10 bytes length, will yield padding if encoded with standard Base64
        String expectedBase64 = "AAECAwQFBgcICQ";
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(issuerSignedFactory.build(namespaces, mockEcPublicKey, CREDENTIAL_TTL_MINUTES))
                .thenReturn(issuerSigned);
        when(cborEncoder.encode(issuerSigned)).thenReturn(mockCborData);
        when(mockDrivingLicenceDocument.getCredentialTtlMinutes())
                .thenReturn(CREDENTIAL_TTL_MINUTES);

        String result =
                mobileDrivingLicenceService.createMobileDrivingLicence(
                        mockDrivingLicenceDocument, mockEcPublicKey);

        assertEquals(
                expectedBase64,
                result,
                "The actual base64url encoded string should match the expected result");
        assertFalse(result.contains("="), "Base64url encoded string should not contain padding");
        assertEquals(mockDrivingLicenceDocument.getCredentialTtlMinutes(), CREDENTIAL_TTL_MINUTES);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory).build(namespaces, mockEcPublicKey, CREDENTIAL_TTL_MINUTES);
        verify(cborEncoder).encode(issuerSigned);
    }

    @Test
    void Should_PropagateMDLException_When_NamespacesFactoryThrows() throws Exception {
        MDLException expectedException =
                new MDLException("Some error message", new RuntimeException());
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenThrow(expectedException);

        MDLException actualException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument, mockEcPublicKey));

        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory, never())
                .build(any(), eq(mockEcPublicKey), eq(CREDENTIAL_TTL_MINUTES));
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagateSigningException_When_IssuerSignedFactoryThrows() throws Exception {
        SigningException expectedException =
                new SigningException("Some error message", new RuntimeException());
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(mockDrivingLicenceDocument.getCredentialTtlMinutes())
                .thenReturn(CREDENTIAL_TTL_MINUTES);
        when(issuerSignedFactory.build(namespaces, mockEcPublicKey, CREDENTIAL_TTL_MINUTES))
                .thenThrow(expectedException);

        SigningException actualException =
                assertThrows(
                        SigningException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument, mockEcPublicKey));
        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory).build(namespaces, mockEcPublicKey, CREDENTIAL_TTL_MINUTES);
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagatePropagateMDLException_When_CborEncoderThrows() throws Exception {
        MDLException expectedException =
                new MDLException("Some error message", new RuntimeException());
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(mockDrivingLicenceDocument.getCredentialTtlMinutes())
                .thenReturn(CREDENTIAL_TTL_MINUTES);
        when(issuerSignedFactory.build(namespaces, mockEcPublicKey, CREDENTIAL_TTL_MINUTES))
                .thenReturn(issuerSigned);
        when(cborEncoder.encode(issuerSigned)).thenThrow(expectedException);

        MDLException actualException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument, mockEcPublicKey));

        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory).build(namespaces, mockEcPublicKey, CREDENTIAL_TTL_MINUTES);
        verify(cborEncoder).encode(issuerSigned);
    }
}
