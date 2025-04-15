package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DocumentFactoryTest {

    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;
    @Mock private CBOREncoder mockCborEncoder;

    private DocumentFactory documentFactory;

    @BeforeEach
    void setUp() {
        documentFactory = new DocumentFactory(mockIssuerSignedItemFactory, mockCborEncoder);
    }

    @Test
    void Should_BuildDocumentWithCorrectDocumentType() throws MDLException {
        DrivingLicenceDocument drivingLicence = createTestDrivingLicenceDocument();

        Document document = documentFactory.build(drivingLicence);

        assertEquals(
                "org.iso.18013.5.1.mDL",
                document.docType(),
                "Document should have the correct mobile driving licence document type");
    }

    @Test
    void Should_BuildWithCorrectNamespaceInIssuerSigned() throws MDLException {
        DrivingLicenceDocument drivingLicence = createTestDrivingLicenceDocument();

        Document document = documentFactory.build(drivingLicence);

        IssuerSigned issuerSigned = document.issuerSigned();
        assertNotNull(issuerSigned, "IssuerSigned should not be null");
        Map<String, List<byte[]>> namespaces = issuerSigned.nameSpaces();
        assertNotNull(namespaces, "Namespaces should not be null");
        assertTrue(
                namespaces.containsKey("org.iso.18013.5.1"),
                "Namespaces should have the mobile driving licence namespace");
    }

    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence() throws MDLException {
        DrivingLicenceDocument drivingLicence = createTestDrivingLicenceDocument();

        Document document = documentFactory.build(drivingLicence);

        IssuerSigned issuerSigned = document.issuerSigned();
        List<byte[]> items = issuerSigned.nameSpaces().get("org.iso.18013.5.1");
        assertEquals(13, items.size(), "Should create one IssuerSignedItem per field");
    }

    @Test
    void Should_IncludeIssuerAuth() throws MDLException {
        DrivingLicenceDocument drivingLicence = createTestDrivingLicenceDocument();

        Document document = documentFactory.build(drivingLicence);

        IssuerSigned issuerSigned = document.issuerSigned();
        assertNotNull(issuerSigned.issuerAuth(), "IssuerAuth should not be null");
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
