package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Document;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DocumentFactory;

import java.security.interfaces.ECPublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileDrivingLicenceServiceTest {

    @Mock private CBOREncoder cborEncoder;
    @Mock private DocumentFactory documentFactory;
    @Mock private DrivingLicenceDocument mockDrivingLicenceDocument;
    @Mock private Document mockDocument;
    @Mock private ECPublicKey mockEcPublicKey;

    private MobileDrivingLicenceService mobileDrivingLicenceService;

    @BeforeEach
    void setUp() {
        mobileDrivingLicenceService = new MobileDrivingLicenceService(cborEncoder, documentFactory);
    }

    @Test
    void Should_CreateMobileDrivingLicence() throws Exception {
        // Arrange
        String expectedHex = "a10102";
        byte[] mockCborBytes = new byte[] {(byte) 0xA1, 0x01, 0x02};
        when(documentFactory.build(mockDrivingLicenceDocument, mockEcPublicKey))
                .thenReturn(mockDocument);
        when(cborEncoder.encode(mockDocument)).thenReturn(mockCborBytes);

        // Act
        String result =
                mobileDrivingLicenceService.createMobileDrivingLicence(
                        mockDrivingLicenceDocument, mockEcPublicKey);

        // Assert
        assertEquals(expectedHex, result, "The actual hex string should match the expected result");
        verify(documentFactory).build(mockDrivingLicenceDocument, mockEcPublicKey);
        verify(cborEncoder).encode(mockDocument);
    }

    @Test
    void Should_PropagateException_When_DocumentFactoryThrows() throws Exception {
        // Arrange
        MDLException expectedException =
                new MDLException("Some DocumentFactory error", new RuntimeException());
        when(documentFactory.build(mockDrivingLicenceDocument, mockEcPublicKey))
                .thenThrow(expectedException);

        // Act & Assert
        MDLException thrown =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument, mockEcPublicKey));
        assertEquals(expectedException, thrown);
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagateException_When_CBOREncoderThrows() throws Exception {
        // Arrange
        MDLException expectedException =
                new MDLException("Some CBOREncoder error", new RuntimeException());
        when(documentFactory.build(any(DrivingLicenceDocument.class), eq(mockEcPublicKey)))
                .thenReturn(mockDocument);
        when(cborEncoder.encode(mockDocument)).thenThrow(expectedException);

        // Act & Assert
        MDLException thrown =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument, mockEcPublicKey));
        assertEquals(expectedException, thrown);
        verify(cborEncoder).encode(mockDocument);
    }
}
