package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Document;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DocumentFactory;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MobileDrivingLicenceServiceTest {

    @Mock private CBOREncoder cborEncoder;
    @Mock private DocumentFactory documentFactory;

    private MobileDrivingLicenceService mobileDrivingLicenceService;

    DrivingLicenceDocument mockDrivingLicenceDocument = mock(DrivingLicenceDocument.class);
    Document mockDocument = mock(Document.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mobileDrivingLicenceService = new MobileDrivingLicenceService(cborEncoder, documentFactory);
    }

    @Test
    void Should_ReturnHexEncodedString() throws MDLException {
        byte[] encodedData = {0x01, 0x02, 0x03, 0x04};
        String expectedHex = "01020304";

        when(documentFactory.build(mockDrivingLicenceDocument)).thenReturn(mockDocument);
        when(cborEncoder.encode(mockDocument)).thenReturn(encodedData);

        String result =
                mobileDrivingLicenceService.createMobileDrivingLicence(mockDrivingLicenceDocument);

        assertEquals(expectedHex, result);
        verify(documentFactory).build(mockDrivingLicenceDocument);
        verify(cborEncoder).encode(mockDocument);
    }

    @Test
    void Should_PropagateException_When_DocumentFactoryThrowsMDLException() throws MDLException {
        MDLException expectedException =
                new MDLException("Some DocumentFactory error", new RuntimeException());

        when(documentFactory.build(mockDrivingLicenceDocument)).thenThrow(expectedException);

        MDLException thrownException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument));

        assertEquals(expectedException, thrownException);
        verify(documentFactory).build(mockDrivingLicenceDocument);
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagateException_When_CBOREncoderThrowsMDLException() throws MDLException {
        MDLException expectedException =
                new MDLException("Some CBOREncoder error", new RuntimeException());

        when(documentFactory.build(mockDrivingLicenceDocument)).thenReturn(mockDocument);
        when(cborEncoder.encode(mockDocument)).thenThrow(expectedException);

        MDLException thrownException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument));

        assertEquals(expectedException, thrownException);
        verify(documentFactory).build(mockDrivingLicenceDocument);
        verify(cborEncoder).encode(mockDocument);
    }

    @Test
    void Should_CreateMobileDrivingLicence_WithRealDependencies() throws MDLException {
        MobileDrivingLicenceService mobileDrivingLicenceService = new MobileDrivingLicenceService();
        DrivingLicenceDocument document = createTestDrivingLicenceDocument();

        String result = mobileDrivingLicenceService.createMobileDrivingLicence(document);

        assertNotNull(result);
        assertTrue(result.length() > 0);
        assertDoesNotThrow(() -> HexFormat.of().parseHex(result));
    }

    private DrivingLicenceDocument createTestDrivingLicenceDocument() {
        DrivingLicenceDocument drivingLicence = new DrivingLicenceDocument();
        drivingLicence.setFamilyName("Doe");
        drivingLicence.setGivenName("John");
        drivingLicence.setPortrait("base64EncodedPortraitString");
        drivingLicence.setBirthDate("24-05-1985");
        drivingLicence.setBirthPlace("London");
        drivingLicence.setIssueDate("10-01-2020");
        drivingLicence.setExpiryDate("09-01-2025");
        drivingLicence.setIssuingAuthority("DVLA");
        drivingLicence.setIssuingCountry("GBR");
        drivingLicence.setDocumentNumber("123456789");
        String[] address = {"123 Main St", "Apt 4B"};
        drivingLicence.setResidentAddress(address);
        drivingLicence.setResidentPostalCode("SW1A 2AA");
        drivingLicence.setResidentCity("London");
        return drivingLicence;
    }
}
