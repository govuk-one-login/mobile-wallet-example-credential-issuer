package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.CredentialType;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.DrivingLicenceDocument;
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
class MdocCredentialBuilderTest {

    @Mock private CBOREncoder cborEncoder;
    @Mock private NamespacesFactory<DrivingLicenceDocument> namespacesFactory;
    @Mock private IssuerSignedFactory issuerSignedFactory;
    @Mock private DrivingLicenceDocument mockDrivingLicenceDocument;
    @Mock private Namespaces namespaces;
    @Mock private IssuerSigned issuerSigned;
    @Mock private ECPublicKey mockEcPublicKey;

    private static final StatusListClient.StatusListInformation STATUS_LIST_INFORMATION =
            new StatusListClient.StatusListInformation(
                    0, "https://test-status-list.gov.uk/t/3B0F3BD087A7");
    private static final long CREDENTIAL_TTL_MINUTES = 43200L;
    private static final String DOC_TYPE = CredentialType.MOBILE_DRIVING_LICENCE.getType();

    private MdocCredentialBuilder<DrivingLicenceDocument> mdocCredentialBuilder;

    @BeforeEach
    void setUp() {
        mdocCredentialBuilder =
                new MdocCredentialBuilder<>(
                        cborEncoder, namespacesFactory, issuerSignedFactory, DOC_TYPE);
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
        when(issuerSignedFactory.build(
                        namespaces,
                        mockEcPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES,
                        DOC_TYPE))
                .thenReturn(issuerSigned);
        when(cborEncoder.encode(issuerSigned)).thenReturn(mockCborData);

        String result =
                mdocCredentialBuilder.buildCredential(
                        mockDrivingLicenceDocument,
                        mockEcPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES);

        assertEquals(
                expectedBase64,
                result,
                "The actual base64url encoded string should match the expected result");
        assertFalse(result.contains("="), "Base64url encoded string should not contain padding");
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory)
                .build(
                        namespaces,
                        mockEcPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES,
                        DOC_TYPE);
        verify(cborEncoder).encode(issuerSigned);
    }

    @Test
    void Should_PropagateMDLException_When_NamespacesFactoryThrows() throws Exception {
        MdocException expectedException =
                new MdocException("Some error message", new RuntimeException());
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenThrow(expectedException);

        MdocException actualException =
                assertThrows(
                        MdocException.class,
                        () ->
                                mdocCredentialBuilder.buildCredential(
                                        mockDrivingLicenceDocument,
                                        mockEcPublicKey,
                                        STATUS_LIST_INFORMATION,
                                        CREDENTIAL_TTL_MINUTES));

        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory, never())
                .build(
                        namespaces,
                        mockEcPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES,
                        DOC_TYPE);
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagateSigningException_When_IssuerSignedFactoryThrows() throws Exception {
        SigningException expectedException =
                new SigningException("Some error message", new RuntimeException());
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(issuerSignedFactory.build(
                        namespaces,
                        mockEcPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES,
                        DOC_TYPE))
                .thenThrow(expectedException);

        SigningException actualException =
                assertThrows(
                        SigningException.class,
                        () ->
                                mdocCredentialBuilder.buildCredential(
                                        mockDrivingLicenceDocument,
                                        mockEcPublicKey,
                                        STATUS_LIST_INFORMATION,
                                        CREDENTIAL_TTL_MINUTES));
        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory)
                .build(
                        namespaces,
                        mockEcPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES,
                        DOC_TYPE);
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagatePropagateMDLException_When_CborEncoderThrows() throws Exception {
        MdocException expectedException =
                new MdocException("Some error message", new RuntimeException());
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(issuerSignedFactory.build(
                        namespaces,
                        mockEcPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES,
                        DOC_TYPE))
                .thenReturn(issuerSigned);
        when(cborEncoder.encode(issuerSigned)).thenThrow(expectedException);

        MdocException actualException =
                assertThrows(
                        MdocException.class,
                        () ->
                                mdocCredentialBuilder.buildCredential(
                                        mockDrivingLicenceDocument,
                                        mockEcPublicKey,
                                        STATUS_LIST_INFORMATION,
                                        CREDENTIAL_TTL_MINUTES));

        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory)
                .build(
                        namespaces,
                        mockEcPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES,
                        DOC_TYPE);
        verify(cborEncoder).encode(issuerSigned);
    }
}
