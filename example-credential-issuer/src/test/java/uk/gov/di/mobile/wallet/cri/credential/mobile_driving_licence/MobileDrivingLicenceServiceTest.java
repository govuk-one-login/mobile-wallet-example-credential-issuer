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

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    private MobileDrivingLicenceService mobileDrivingLicenceService;

    @BeforeEach
    void setUp() {
        mobileDrivingLicenceService =
                new MobileDrivingLicenceService(
                        cborEncoder, namespacesFactory, issuerSignedFactory);
    }

    @Test
    void Should_ReturnBase64EncodedIssuerSigned() throws Exception {
        byte[] mockCborData = "mock-cbor-data".getBytes();
        String expectedBase64 = Base64.getUrlEncoder().encodeToString(mockCborData);
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(issuerSignedFactory.build(namespaces)).thenReturn(issuerSigned);
        when(cborEncoder.encode(issuerSigned)).thenReturn(mockCborData);

        String result =
                mobileDrivingLicenceService.createMobileDrivingLicence(mockDrivingLicenceDocument);

        assertEquals(
                expectedBase64,
                result,
                "The actual base64 string should match the expected result");
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory).build(namespaces);
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
                                        mockDrivingLicenceDocument));

        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory, never()).build(any());
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagateSigningException_When_IssuerSignedFactoryThrows() throws Exception {
        SigningException expectedException =
                new SigningException("Some error message", new RuntimeException());
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(issuerSignedFactory.build(namespaces)).thenThrow(expectedException);

        SigningException actualException =
                assertThrows(
                        SigningException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument));
        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory).build(namespaces);
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagatePropagateMDLException_When_CborEncoderThrows() throws Exception {
        MDLException expectedException =
                new MDLException("Some error message", new RuntimeException());
        when(namespacesFactory.build(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(issuerSignedFactory.build(namespaces)).thenReturn(issuerSigned);
        when(cborEncoder.encode(issuerSigned)).thenThrow(expectedException);

        MDLException actualException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument));

        assertEquals(expectedException, actualException);
        verify(namespacesFactory).build(mockDrivingLicenceDocument);
        verify(issuerSignedFactory).build(namespaces);
        verify(cborEncoder).encode(issuerSigned);
    }
}
