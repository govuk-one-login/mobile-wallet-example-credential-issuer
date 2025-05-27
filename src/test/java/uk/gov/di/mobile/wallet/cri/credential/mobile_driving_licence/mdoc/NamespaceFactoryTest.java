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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamespaceFactoryTest {

    private static final int EXPECTED_ISO_FIELDS = 15;
    private static final int EXPECTED_UK_FIELDS = 1;
    private static final byte[] MOCK_CBOR_BYTES = {0x01, 0x02};
    private static final DrivingPrivilege[] DRIVING_PRIVILEGES = {
        new DrivingPrivilege("B", null, null)
    };

    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;
    @Mock private CBOREncoder mockCborEncoder;
    @Captor private ArgumentCaptor<String> elementIdentifierCaptor;

    private DocumentFactory documentFactory;
    private DrivingLicenceDocument drivingLicence;

    @BeforeEach
    void setUp() throws MDLException {
        IssuerSignedItem dummyItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(dummyItem);
        when(mockCborEncoder.encode(any())).thenReturn(MOCK_CBOR_BYTES);

        documentFactory = new DocumentFactory(mockIssuerSignedItemFactory, mockCborEncoder);
        drivingLicence = createTestDrivingLicenceDocument(DRIVING_PRIVILEGES);
    }

    @Test
    void Should_BuildISOAndUKNamespaces() throws Exception {
        Map<String, List<byte[]>> namespaces = documentFactory.buildAllNamespaces(drivingLicence);

        assertEquals(2, namespaces.size());
        assertTrue(namespaces.containsKey(Namespaces.ISO), "Should contain ISO namespace");
        assertTrue(namespaces.containsKey(Namespaces.UK), "Should contain UK namespace");
        assertEquals(EXPECTED_ISO_FIELDS, namespaces.get(Namespaces.ISO).size());
        assertEquals(EXPECTED_UK_FIELDS, namespaces.get(Namespaces.UK).size());
    }

    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_ISONamespace()
            throws MDLException {
        Map<String, List<byte[]>> namespaces = documentFactory.buildAllNamespaces(drivingLicence);

        List<byte[]> isoNamespace = namespaces.get(Namespaces.ISO);
        assertEquals(
                EXPECTED_ISO_FIELDS,
                isoNamespace.size(),
                "Should create one IssuerSignedItem per ISO namespace attribute");
        isoNamespace.forEach(bytes -> assertArrayEquals(MOCK_CBOR_BYTES, bytes));
        verify(mockIssuerSignedItemFactory).build("family_name", "Doe");
        verify(mockIssuerSignedItemFactory).build("given_name", "John");
        verify(mockIssuerSignedItemFactory).build("portrait", "base64EncodedPortraitString");
        verify(mockIssuerSignedItemFactory).build("birth_date", LocalDate.parse("1985-05-24"));
        verify(mockIssuerSignedItemFactory).build("birth_place", "London");
        verify(mockIssuerSignedItemFactory).build("issue_date", LocalDate.parse("2020-01-10"));
        verify(mockIssuerSignedItemFactory).build("expiry_date", LocalDate.parse("2025-01-09"));
        verify(mockIssuerSignedItemFactory).build("issuing_authority", "DVLA");
        verify(mockIssuerSignedItemFactory).build("issuing_country", "GB");
        verify(mockIssuerSignedItemFactory).build("document_number", "HALL9655293DH5RO");
        verify(mockIssuerSignedItemFactory).build("resident_address", "123 Main St, Apt 4B");
        verify(mockIssuerSignedItemFactory).build("resident_postal_code", "SW1A 2AA");
        verify(mockIssuerSignedItemFactory).build("resident_city", "London");
        verify(mockIssuerSignedItemFactory).build("driving_privileges", DRIVING_PRIVILEGES);
        verify(mockIssuerSignedItemFactory).build("un_distinguishing_sign", "UK");
    }

    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_UKNamespace()
            throws MDLException {
        Map<String, List<byte[]>> namespaces = documentFactory.buildAllNamespaces(drivingLicence);

        List<byte[]> ukNamespace = namespaces.get(Namespaces.UK);
        assertEquals(
                EXPECTED_UK_FIELDS,
                ukNamespace.size(),
                "Should create one IssuerSignedItem per UK namespace attribute");
        ukNamespace.forEach(bytes -> assertArrayEquals(MOCK_CBOR_BYTES, bytes));
        verify(mockIssuerSignedItemFactory)
                .build("provisional_driving_privileges", Optional.ofNullable(DRIVING_PRIVILEGES));
    }

    @Test
    void Should_NotBuildIssuerSignedItemForProvisionalDrivingPrivileges_When_ItsValueIsNull()
            throws MDLException {
        DrivingPrivilege[] provisionDrivingPrivileges = null;
        DrivingLicenceDocument drivingLicenceWithProvisionalNull =
                createTestDrivingLicenceDocument(provisionDrivingPrivileges);

        Map<String, List<byte[]>> namespaces =
                documentFactory.buildAllNamespaces(drivingLicenceWithProvisionalNull);

        List<byte[]> ukNamespace = namespaces.get(Namespaces.UK);
        assertEquals(
                EXPECTED_UK_FIELDS - 1,
                ukNamespace.size(),
                "Should not create an IssuerSignedItem for provisional driving privileges when its value is null");
        verify(mockIssuerSignedItemFactory, never())
                .build("provisional_driving_privileges", Optional.empty());
    }

    @Test
    void Should_CorrectlyConvertFieldNamesToSnakeCase() throws MDLException {
        documentFactory.buildAllNamespaces(drivingLicence);

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

    private DrivingLicenceDocument createTestDrivingLicenceDocument(
            DrivingPrivilege[] provisionalDrivingPrivileges) {
        String[] address = {"123 Main St", "Apt 4B"};
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
                DRIVING_PRIVILEGES,
                "UK",
                provisionalDrivingPrivileges);
    }
}
