package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.Code;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.constants.NamespaceTypes;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NamespacesFactoryTest {

    private static final List<Code> CODES = Collections.singletonList(new Code("01"));
    private static final List<DrivingPrivilege> DRIVING_PRIVILEGES =
            List.of(
                    new DrivingPrivilege("A", "12-02-2020", "11-02-2030", CODES),
                    new DrivingPrivilege("B", null, null, null));
    private static final List<Map<String, Object>> DRIVING_PRIVILEGES_SNAKE_CASE =
            List.of(
                    Map.of(
                            "vehicle_category_code",
                            "A",
                            "issue_date",
                            LocalDate.parse("2020-02-12"),
                            "expiry_date",
                            LocalDate.parse("2030-02-11"),
                            "codes",
                            CODES),
                    Map.of("vehicle_category_code", "B"));

    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;

    private NamespacesFactory namespacesFactory;

    @BeforeEach
    void setUp() {
        namespacesFactory = new NamespacesFactory(mockIssuerSignedItemFactory);
    }

    /**
     * Test that the NamespacesFactory creates both ISO and UK nameSpaces, and that the correct
     * number of fields are present in each.
     */
    @Test
    void Should_BuildISOAndUKNamespaces() {
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);

        Namespaces result = namespacesFactory.build(drivingLicenceDocument);

        Map<String, List<IssuerSignedItem>> namespaces = result.namespaces();
        assertEquals(2, namespaces.size(), "Should have 2 nameSpaces (ISO and UK)");
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
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);

        Namespaces result = namespacesFactory.build(drivingLicenceDocument);

        List<IssuerSignedItem> isoNamespace = result.namespaces().get(NamespaceTypes.ISO);
        assertEquals(
                18,
                isoNamespace.size(),
                "Should create one IssuerSignedItem per ISO namespace attribute");
        // Verify that the factory was called with the expected ISO fields and values
        verify(mockIssuerSignedItemFactory).build("family_name", "Doe");
        verify(mockIssuerSignedItemFactory).build("given_name", "John");
        verify(mockIssuerSignedItemFactory)
                .build("portrait", Base64.getDecoder().decode("base64EncodedPortraitString"));
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
        verify(mockIssuerSignedItemFactory)
                .build("driving_privileges", DRIVING_PRIVILEGES_SNAKE_CASE);
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
        DrivingLicenceDocument drivingLicenceDocument =
                createTestDrivingLicenceDocument(DRIVING_PRIVILEGES);
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);

        Namespaces result = namespacesFactory.build(drivingLicenceDocument);

        List<IssuerSignedItem> ukNamespace = result.namespaces().get(NamespaceTypes.GB);
        assertEquals(
                3,
                ukNamespace.size(),
                "Should create one IssuerSignedItem per UK namespace attribute");
        // Verify that the factory was called with all expected UK fields and values
        verify(mockIssuerSignedItemFactory)
                .build("provisional_driving_privileges", DRIVING_PRIVILEGES_SNAKE_CASE);
        verify(mockIssuerSignedItemFactory).build("title", "Miss");
    }

    /**
     * Test that the NamespacesFactory does not build an IssuerSignedItem for provisional driving
     * privileges if its value is null.
     */
    @Test
    void Should_NotBuildIssuerSignedItemForProvisionalDrivingPrivileges_WhenItsValueIsNull()
            throws MDLException {
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument(null);
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);

        Namespaces result = namespacesFactory.build(drivingLicenceDocument);

        List<IssuerSignedItem> ukNamespace = result.namespaces().get(NamespaceTypes.GB);
        assertEquals(
                2,
                ukNamespace.size(),
                "Should not create an IssuerSignedItem for provisional driving privileges when null");

        verify(mockIssuerSignedItemFactory, never())
                .build(eq("provisional_driving_privileges"), any());
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
            List<DrivingPrivilege> provisionalDrivingPrivileges) {
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
