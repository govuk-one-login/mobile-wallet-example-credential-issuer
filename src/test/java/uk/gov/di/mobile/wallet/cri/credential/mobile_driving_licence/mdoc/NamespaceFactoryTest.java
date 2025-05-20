package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.Namespaces;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamespaceFactoryTest {

    private static final int EXPECTED_ISO_FIELDS = 15;
    private static final int EXPECTED_UK_FIELDS = 1;
    private static final byte[] MOCK_CBOR_BYTES = {0x01, 0x02};

    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;
    @Mock private CBOREncoder mockCborEncoder;
    @Captor private ArgumentCaptor<String> elementIdentifierCaptor;

    private NamespaceFactory namespaceFactory;
    private DrivingLicenceDocument drivingLicence;

    @BeforeEach
    void setUp() throws MDLException {
        IssuerSignedItem dummyItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(dummyItem);
        when(mockCborEncoder.encode(any())).thenReturn(MOCK_CBOR_BYTES);

        namespaceFactory = new NamespaceFactory(mockIssuerSignedItemFactory, mockCborEncoder);
        drivingLicence = createTestDrivingLicenceDocument();
    }

    @Test
    void Should_BuildISOAndUKNamespaces() throws Exception {
        Map<String, List<byte[]>> namespaces = namespaceFactory.buildAllNamespaces(drivingLicence);

        assertEquals(2, namespaces.size());
        assertTrue(namespaces.containsKey(Namespaces.ISO), "Should contain ISO namespace");
        assertTrue(namespaces.containsKey(Namespaces.UK), "Should contain UK namespace");
        assertEquals(EXPECTED_ISO_FIELDS, namespaces.get(Namespaces.ISO).size());
        assertEquals(EXPECTED_UK_FIELDS, namespaces.get(Namespaces.UK).size());
    }

    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_ISONamespace()
            throws MDLException {
        Map<String, List<byte[]>> namespaces = namespaceFactory.buildAllNamespaces(drivingLicence);
        List<byte[]> isoNamespace = namespaces.get(Namespaces.ISO);

        assertEquals(
                EXPECTED_ISO_FIELDS,
                isoNamespace.size(),
                "Should create one IssuerSignedItem per ISO namespace attribute");
        isoNamespace.forEach(bytes -> assertArrayEquals(MOCK_CBOR_BYTES, bytes));
    }

    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_UKNamespace()
            throws MDLException {
        Map<String, List<byte[]>> namespaces = namespaceFactory.buildAllNamespaces(drivingLicence);
        List<byte[]> ukNamespace = namespaces.get(Namespaces.UK);

        assertEquals(
                EXPECTED_UK_FIELDS,
                ukNamespace.size(),
                "Should create one IssuerSignedItem per UK namespace attribute");
        ukNamespace.forEach(bytes -> assertArrayEquals(MOCK_CBOR_BYTES, bytes));
    }

    @Test
    void Should_CorrectlyConvertFieldNamesToSnakeCase() throws MDLException {
        namespaceFactory.buildAllNamespaces(drivingLicence);

        verify(mockIssuerSignedItemFactory, times(EXPECTED_ISO_FIELDS + EXPECTED_UK_FIELDS))
                .build(elementIdentifierCaptor.capture(), any());
        List<String> capturedIdentifiers = elementIdentifierCaptor.getAllValues();

        List<String> expectedIdentifiers =
                List.of(
                        "family_name",
                        "given_name",
                        "portrait",
                        "birth_date",
                        "birth_place",
                        "issue_date",
                        "expiry_date",
                        "issuing_authority",
                        "issuing_country",
                        "document_number",
                        "resident_address",
                        "resident_postal_code",
                        "resident_city",
                        "driving_privileges",
                        "un_distinguishing_sign",
                        "provisional_driving_privileges");

        assertTrue(
                capturedIdentifiers.containsAll(expectedIdentifiers),
                "All expected snake_case identifiers should be present");
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
                drivingPrivileges,
                "UK",
                drivingPrivileges);
    }
}
