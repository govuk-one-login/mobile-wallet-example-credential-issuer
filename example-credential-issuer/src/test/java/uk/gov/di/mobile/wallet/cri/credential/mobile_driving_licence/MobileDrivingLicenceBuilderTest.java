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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileDrivingLicenceBuilderTest {

    private MobileDrivingLicenceBuilder mobileDrivingLicenceBuilder;

    @Mock private CBOREncoder mockCborEncoder;
    @Mock private NamespacesFactory mockNamespacesFactory;
    @Mock private IssuerSignedFactory mockIssuerSignedFactory;
    @Mock private DrivingLicenceDocument mockDrivingLicenceDocument;
    @Mock private Namespaces mockNamespaces;
    @Mock private IssuerSigned mockIssuerSigned;
    @Mock private ECPublicKey mockEcPublicKey;

    private static final int IDX = 0;
    private static final String URI = "https://test-status-list.gov.uk/t/3B0F3BD087A7";
    private static final long CREDENTIAL_TTL_MINUTES = 43200L;

    @BeforeEach
    void setUp() {
        mobileDrivingLicenceBuilder =
                new MobileDrivingLicenceBuilder(
                        mockCborEncoder, mockNamespacesFactory, mockIssuerSignedFactory);
    }

    @Test
    void Should_ReturnBase64UrlEncodedIssuerSigned() throws Exception {
        byte[] mockCborData =
                new byte[] {
                    0x00, 0x01, 0x02, 0x03, 0x04,
                    0x05, 0x06, 0x07, 0x08, 0x09
                }; // 10 bytes length, will yield padding if encoded with standard Base64
        String expectedBase64 = "AAECAwQFBgcICQ";
        when(mockNamespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(mockNamespaces);
        when(mockDrivingLicenceDocument.getCredentialTtlMinutes())
                .thenReturn(CREDENTIAL_TTL_MINUTES);
        when(mockIssuerSignedFactory.build(
                        mockNamespaces, mockEcPublicKey, IDX, URI, CREDENTIAL_TTL_MINUTES))
                .thenReturn(mockIssuerSigned);
        when(mockCborEncoder.encode(mockIssuerSigned)).thenReturn(mockCborData);

        String result =
                mobileDrivingLicenceBuilder.createMobileDrivingLicence(
                        mockDrivingLicenceDocument, mockEcPublicKey, IDX, URI);

        assertEquals(
                expectedBase64,
                result,
                "The actual base64url encoded string should match the expected result");
        assertFalse(result.contains("="), "Base64url encoded string should not contain padding");
        assertEquals(CREDENTIAL_TTL_MINUTES, mockDrivingLicenceDocument.getCredentialTtlMinutes());
        verify(mockNamespacesFactory).build(mockDrivingLicenceDocument);
        verify(mockIssuerSignedFactory)
                .build(mockNamespaces, mockEcPublicKey, IDX, URI, CREDENTIAL_TTL_MINUTES);
        verify(mockCborEncoder).encode(mockIssuerSigned);
    }

    @Test
    void Should_PropagateMDLException_When_NamespacesFactoryThrows() throws Exception {
        MDLException expectedException =
                new MDLException("Some error message", new RuntimeException());
        when(mockNamespacesFactory.build(mockDrivingLicenceDocument)).thenThrow(expectedException);

        MDLException actualException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceBuilder.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument, mockEcPublicKey, IDX, URI));

        assertEquals(expectedException, actualException);
        verify(mockNamespacesFactory).build(mockDrivingLicenceDocument);
        verify(mockIssuerSignedFactory, never())
                .build(mockNamespaces, mockEcPublicKey, IDX, URI, CREDENTIAL_TTL_MINUTES);
        verify(mockCborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagateSigningException_When_IssuerSignedFactoryThrows() throws Exception {
        SigningException expectedException =
                new SigningException("Some error message", new RuntimeException());
        when(mockNamespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(mockNamespaces);
        when(mockDrivingLicenceDocument.getCredentialTtlMinutes())
                .thenReturn(CREDENTIAL_TTL_MINUTES);
        when(mockIssuerSignedFactory.build(
                        mockNamespaces, mockEcPublicKey, IDX, URI, CREDENTIAL_TTL_MINUTES))
                .thenThrow(expectedException);

        SigningException actualException =
                assertThrows(
                        SigningException.class,
                        () ->
                                mobileDrivingLicenceBuilder.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument, mockEcPublicKey, IDX, URI));
        assertEquals(expectedException, actualException);
        verify(mockNamespacesFactory).build(mockDrivingLicenceDocument);
        verify(mockIssuerSignedFactory)
                .build(mockNamespaces, mockEcPublicKey, IDX, URI, CREDENTIAL_TTL_MINUTES);
        verify(mockCborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagatePropagateMDLException_When_CborEncoderThrows() throws Exception {
        MDLException expectedException =
                new MDLException("Some error message", new RuntimeException());
        when(mockNamespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(mockNamespaces);
        when(mockDrivingLicenceDocument.getCredentialTtlMinutes())
                .thenReturn(CREDENTIAL_TTL_MINUTES);
        when(mockIssuerSignedFactory.build(
                        mockNamespaces, mockEcPublicKey, IDX, URI, CREDENTIAL_TTL_MINUTES))
                .thenReturn(mockIssuerSigned);
        when(mockCborEncoder.encode(mockIssuerSigned)).thenThrow(expectedException);

        MDLException actualException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceBuilder.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument, mockEcPublicKey, IDX, URI));

        assertEquals(expectedException, actualException);
        verify(mockNamespacesFactory).build(mockDrivingLicenceDocument);
        verify(mockIssuerSignedFactory)
                .build(mockNamespaces, mockEcPublicKey, IDX, URI, CREDENTIAL_TTL_MINUTES);
        verify(mockCborEncoder).encode(mockIssuerSigned);
    }
}
