package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamespaceFactoryTest {

    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;
    @Mock private CBOREncoder mockCborEncoder;
    @Captor private ArgumentCaptor<String> elementIdentifierCaptor;

    private NamespaceFactory namespaceFactory;

    @BeforeEach
    void setUp() {
        namespaceFactory = new NamespaceFactory(mockIssuerSignedItemFactory, mockCborEncoder);
    }

    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence() throws MDLException {
        DrivingLicenceDocument drivingLicence = createTestDrivingLicenceDocument();
        List<byte[]> issuerSignedItems = namespaceFactory.build(drivingLicence);

        assertEquals(
                14,
                issuerSignedItems.size(),
                "Should create one IssuerSignedItem per attribute in the driving licence document");
    }

    @Test
    void Should_CorrectlyConvertFieldNamesToSnakeCase() throws MDLException {
        DrivingLicenceDocument drivingLicence = createTestDrivingLicenceDocument();

        namespaceFactory.build(drivingLicence);

        // Capture all calls to mockIssuerSignedItemFactory build method
        verify(mockIssuerSignedItemFactory, times(14))
                .build(elementIdentifierCaptor.capture(), ArgumentMatchers.any());
        List<String> capturedIdentifiers = elementIdentifierCaptor.getAllValues();

        // Verify that field names were properly converted to snake_case
        assertTrue(capturedIdentifiers.contains("family_name"));
        assertTrue(capturedIdentifiers.contains("given_name"));
        assertTrue(capturedIdentifiers.contains("portrait"));
        assertTrue(capturedIdentifiers.contains("birth_date"));
        assertTrue(capturedIdentifiers.contains("birth_place"));
        assertTrue(capturedIdentifiers.contains("issue_date"));
        assertTrue(capturedIdentifiers.contains("expiry_date"));
        assertTrue(capturedIdentifiers.contains("issuing_authority"));
        assertTrue(capturedIdentifiers.contains("issuing_country"));
        assertTrue(capturedIdentifiers.contains("document_number"));
        assertTrue(capturedIdentifiers.contains("resident_address"));
        assertTrue(capturedIdentifiers.contains("resident_postal_code"));
        assertTrue(capturedIdentifiers.contains("resident_city"));
        assertTrue(capturedIdentifiers.contains("driving_privileges"));
    }

    private DrivingLicenceDocument createTestDrivingLicenceDocument() {
        String[] address = {"123 Main St", "Apt 4B"};
        DrivingPrivilege[] drivingPrivileges = {new DrivingPrivilege("B", null, null)};

        return new DrivingLicenceDocument(
                "Doe",
                "John",
                "base64EncodedPortraitString",
                "24-05-1985",
                "London",
                "10-01-2020",
                "09-01-2025",
                "DVLA",
                "GB",
                "HALL9655293DH5RO",
                address,
                "SW1A 2AA",
                "London",
                drivingPrivileges);
    }
}
