package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.constants.NamespaceTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NamespacesFactoryTest {

    // Test data: a single driving privilege for use in the driving licence document
    private static final DrivingPrivilege[] DRIVING_PRIVILEGES = {
        new DrivingPrivilege("B", null, null)
    };

    // Mocked dependencies for NamespacesFactory
    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;

    private NamespacesFactory namespacesFactory;

    @BeforeEach
    void setUp() {
        namespacesFactory = new NamespacesFactory(mockIssuerSignedItemFactory);
    }

    /**
     * Test that the NamespacesFactory creates both ISO and UK namespaces, and that the correct
     * number of fields are present in each.
     */
    @Test
    void Should_BuildISOAndUKNamespaces() {
        // Arrange: Set up test DrivingLicenceDocument
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();
        // Arrange: Configure mock to return a dummy IssuerSignedItem
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);

        // Act: Build the namespaces
        Namespaces result = namespacesFactory.build(drivingLicenceDocument);

        // Assert: Check that both namespaces exist and have the expected number of fields
        Map<String, List<IssuerSignedItem>> namespaces = result.asMap();
        assertEquals(2, namespaces.size(), "Should have 2 namespaces (ISO and UK)");
        assertTrue(namespaces.containsKey(NamespaceTypes.ISO), "Should contain ISO namespace");
        assertTrue(namespaces.containsKey(NamespaceTypes.GB), "Should contain UK namespace");
        assertEquals(
                18,
                namespaces.get(NamespaceTypes.ISO).size(),
                "ISO namespace should have 18 fields");
        assertEquals(
                3, namespaces.get(NamespaceTypes.GB).size(), "UK namespace should have 3 fields");
    }

    /**
     * Test that the NamespacesFactory builds an IssuerSignedItem for each ISO namespace field. Also
     * verifies that the correct values are passed to the IssuerSignedItemFactory for each field.
     */
    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_ISONamespace()
            throws MDLException {
        // Arrange: Set up test DrivingLicenceDocument
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();
        // Arrange: Configure mock to return a dummy IssuerSignedItem
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);

        // Act: Build the namespaces
        Namespaces result = namespacesFactory.build(drivingLicenceDocument);

        // Assert: Check that ISO namespace has correct number of fields
        List<IssuerSignedItem> isoNamespace = result.asMap().get(NamespaceTypes.ISO);
        assertEquals(
                18,
                isoNamespace.size(),
                "Should create one IssuerSignedItem per ISO namespace attribute");
        // Assert: Verify that the factory was called with the expected ISO fields and values
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
        verify(mockIssuerSignedItemFactory).build("welsh_licence", false);
    }

    /**
     * Test that the NamespacesFactory builds an IssuerSignedItem for each UK namespace field. Also
     * verifies that the correct values are passed to the IssuerSignedItemFactory for each field.
     */
    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_UKNamespace()
            throws MDLException {
        // Arrange: Set up test DrivingLicenceDocument
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();
        // Arrange: Configure mock to return a dummy IssuerSignedItem
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);

        // Act: Build the namespaces
        Namespaces result = namespacesFactory.build(drivingLicenceDocument);

        // Assert: Check that UK namespace has correct number of fields
        List<IssuerSignedItem> ukNamespace = result.asMap().get(NamespaceTypes.GB);
        assertEquals(
                3,
                ukNamespace.size(),
                "Should create one IssuerSignedItem per UK namespace attribute");
        // Assert: Verify that the factory was called with all expected UK fields and values
        verify(mockIssuerSignedItemFactory)
                .build("provisional_driving_privileges", Optional.ofNullable(DRIVING_PRIVILEGES));
        verify(mockIssuerSignedItemFactory).build("title", "Miss");
    }

    /**
     * Test that the NamespacesFactory does not build an IssuerSignedItem for provisional driving
     * privileges if its value is null.
     */
    @Test
    void Should_NotBuildIssuerSignedItemForProvisionalDrivingPrivileges_When_ItsValueIsNull()
            throws MDLException {
        // Arrange: Set up test DrivingLicenceDocument with null provisional privileges
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument(null);
        // Arrange: Configure mock to return a dummy IssuerSignedItem
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);

        // Act: Build the namespaces
        Namespaces result = namespacesFactory.build(drivingLicenceDocument);

        // Assert: Check that UK namespace has only 1 field (i.e. not provisional privileges)
        List<IssuerSignedItem> ukNamespace = result.asMap().get(NamespaceTypes.GB);
        assertEquals(
                2,
                ukNamespace.size(),
                "Should not create an IssuerSignedItem for provisional driving privileges when its value is null");
        // Assert: Verify that the factory was NOT called for provisional privileges with null
        verify(mockIssuerSignedItemFactory, never())
                .build("provisional_driving_privileges", Optional.empty());
    }

    /**
     * Creates a DrivingLicenceDocument using the default DRIVING_PRIVILEGES for both // *
     * drivingPrivileges and provisionalDrivingPrivileges. //
     */
    private DrivingLicenceDocument createTestDrivingLicenceDocument() {
        return createTestDrivingLicenceDocument(DRIVING_PRIVILEGES);
    }

    /**
     * Creates a DrivingLicenceDocument with the given provisionalDrivingPrivileges. The main
     * drivingPrivileges field always uses the default DRIVING_PRIVILEGES.
     */
    private DrivingLicenceDocument createTestDrivingLicenceDocument(
            DrivingPrivilege[] provisionalDrivingPrivileges) {
        String[] address = {"123 Main St", "Apt 4B"};
        return new DrivingLicenceDocument(
                "Doe",
                "John",
                "Miss",
                false,
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
