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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class NameSpaceFactoryTest {

    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;
    @Mock private CBOREncoder mockCborEncoder;
    private NameSpaceFactory nameSpaceFactory;

    @BeforeEach
    void setUp() {
        nameSpaceFactory = new NameSpaceFactory(mockIssuerSignedItemFactory, mockCborEncoder);
    }

    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence() throws MDLException {
        DrivingLicenceDocument drivingLicence = createTestDrivingLicenceDocument();
        List<byte[]> issuerSignedItems = nameSpaceFactory.build(drivingLicence);

        assertEquals(
                13,
                issuerSignedItems.size(),
                "Should create one IssuerSignedItem per attribute in the driving licence document");
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
