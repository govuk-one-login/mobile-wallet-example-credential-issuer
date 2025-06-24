package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.Namespaces;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentFactoryTest {

    // Test data: a single driving privilege for use in the driving licence document
    private static final DrivingPrivilege[] DRIVING_PRIVILEGES = {
        new DrivingPrivilege("B", null, null)
    };

    // Mocked dependencies for DocumentFactory
    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;
    @Mock private MobileSecurityObjectFactory mockMobileSecurityObjectFactory;
    @Mock private CBOREncoder mockCborEncoder;
    @Mock private ValueDigests mockValueDigests;

    /**
     * Test that the DocumentFactory creates both ISO and UK namespaces, and that the correct number
     * of fields are present in each.
     */
    @Test
    void Should_BuildISOAndUKNamespaces() {
        // Arrange: Set up DocumentFactory and test DrivingLicenceDocument
        DocumentFactory documentFactory =
                new DocumentFactory(
                        mockIssuerSignedItemFactory,
                        mockMobileSecurityObjectFactory,
                        mockCborEncoder);
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();

        // Arrange: Configure mocks to return a dummy IssuerSignedItem and CBOR bytes
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);
        byte[] expectedCbor = "testCbor".getBytes();
        when(mockCborEncoder.encode(any())).thenReturn(expectedCbor);

        // Act: Build the document
        Document result = documentFactory.build(drivingLicenceDocument);

        // Assert: Check that both namespaces exist and have the expected number of fields
        Map<String, List<byte[]>> namespaces = result.issuerSigned().nameSpaces();
        assertEquals(2, namespaces.size(), "Should have 2 namespaces (ISO and UK)");
        assertTrue(namespaces.containsKey(Namespaces.ISO), "Should contain ISO namespace");
        assertTrue(namespaces.containsKey(Namespaces.UK), "Should contain UK namespace");
        assertEquals(
                18, namespaces.get(Namespaces.ISO).size(), "ISO namespace should have 18 fields");
        assertEquals(3, namespaces.get(Namespaces.UK).size(), "UK namespace should have 3 fields");
    }

    /**
     * Test that the DocumentFactory builds an IssuerSignedItem for each ISO namespace field. Also
     * verifies that the correct values are passed to the IssuerSignedItemFactory for each field.
     */
    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_ISONamespace()
            throws MDLException {
        // Arrange: Set up DocumentFactory and test DrivingLicenceDocument
        DocumentFactory documentFactory =
                new DocumentFactory(
                        mockIssuerSignedItemFactory,
                        mockMobileSecurityObjectFactory,
                        mockCborEncoder);
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();

        // Arrange: Configure mocks to return a dummy IssuerSignedItem and CBOR bytes
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);
        byte[] expectedCbor = "testCbor".getBytes();
        when(mockCborEncoder.encode(any())).thenReturn(expectedCbor);

        // Act: Build the document
        Document result = documentFactory.build(drivingLicenceDocument);

        // Assert: Check that ISO namespace has correct number of fields and that each field has
        // been CBOR encoded
        List<byte[]> isoNamespace = result.issuerSigned().nameSpaces().get(Namespaces.ISO);
        assertEquals(
                18,
                isoNamespace.size(),
                "Should create one IssuerSignedItem per ISO namespace attribute");
        isoNamespace.forEach(bytes -> assertArrayEquals(expectedCbor, bytes));

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
     * Test that the DocumentFactory builds an IssuerSignedItem for each UK namespace field. Also
     * verifies that the correct values are passed to the IssuerSignedItemFactory for each field.
     */
    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_UKNamespace()
            throws MDLException {
        // Arrange: Set up DocumentFactory and test DrivingLicenceDocument
        DocumentFactory documentFactory =
                new DocumentFactory(
                        mockIssuerSignedItemFactory,
                        mockMobileSecurityObjectFactory,
                        mockCborEncoder);
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();

        // Arrange: Configure mocks to return a dummy IssuerSignedItem and CBOR bytes
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);
        byte[] expectedCbor = "testCbor".getBytes();
        when(mockCborEncoder.encode(any())).thenReturn(expectedCbor);

        // Act: Build the document
        Document result = documentFactory.build(drivingLicenceDocument);

        // Assert: Check that UK namespace has correct number of fields and that each field has been
        // CBOR encoded
        List<byte[]> ukNamespace = result.issuerSigned().nameSpaces().get(Namespaces.UK);
        assertEquals(
                3,
                ukNamespace.size(),
                "Should create one IssuerSignedItem per UK namespace attribute");
        ukNamespace.forEach(bytes -> assertArrayEquals(expectedCbor, bytes));

        // Assert: Verify that the factory was called with all expected UK fields and values
        verify(mockIssuerSignedItemFactory)
                .build("provisional_driving_privileges", Optional.ofNullable(DRIVING_PRIVILEGES));
        verify(mockIssuerSignedItemFactory).build("title", "Miss");
    }

    /**
     * Test that the DocumentFactory does not build an IssuerSignedItem for provisional driving
     * privileges if its value is null.
     */
    @Test
    void Should_NotBuildIssuerSignedItemForProvisionalDrivingPrivileges_When_ItsValueIsNull()
            throws MDLException {
        // Arrange: Set up DocumentFactory and a DrivingLicenceDocument with null provisional
        // privileges
        DocumentFactory documentFactory =
                new DocumentFactory(
                        mockIssuerSignedItemFactory,
                        mockMobileSecurityObjectFactory,
                        mockCborEncoder);
        DrivingLicenceDocument drivingLicenceWithProvisionalNull =
                createTestDrivingLicenceDocument(null);

        // Arrange: Configure mocks to return a dummy IssuerSignedItem and CBOR bytes
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(issuerSignedItem);
        byte[] expectedCbor = "testCbor".getBytes();
        when(mockCborEncoder.encode(any())).thenReturn(expectedCbor);

        // Act: Build the document
        Document result = documentFactory.build(drivingLicenceWithProvisionalNull);

        // Assert: Check that UK namespace has only 1 field (i.e. not provisional privileges)
        List<byte[]> ukNamespace = result.issuerSigned().nameSpaces().get(Namespaces.UK);
        assertEquals(
                2,
                ukNamespace.size(),
                "Should not create an IssuerSignedItem for provisional driving privileges when its value is null");
        // Assert: Verify that the factory was NOT called for provisional privileges with null
        verify(mockIssuerSignedItemFactory, never())
                .build("provisional_driving_privileges", Optional.empty());
    }

    /**
     * Test that the DocumentFactory returns a Document containing the expected docType and the
     * expected IssuerSigned, and that all mocks are called as expected.
     */
    @Test
    void Should_ReturnIssuerSignedWithExpectedNamespacesAndIssuerAuth() {
        // Arrange: Set up DocumentFactory and test DrivingLicenceDocument
        DocumentFactory documentFactory =
                new DocumentFactory(
                        mockIssuerSignedItemFactory,
                        mockMobileSecurityObjectFactory,
                        mockCborEncoder);
        DrivingLicenceDocument drivingLicenceDocument = createTestDrivingLicenceDocument();

        // Arrange: Prepare a specific IssuerSignedItem to be returned by the factory
        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(1, new byte[] {1, 2, 3}, "elementIdentifier", "elementValue");
        when(mockIssuerSignedItemFactory.build(any(), any())).thenReturn(issuerSignedItem);

        // Arrange: Prepare a specific MobileSecurityObject to be returned by the factory
        Clock clock = Clock.fixed(Instant.ofEpochSecond(1750677223), ZoneId.systemDefault());
        ValidityInfo expectedValidityInfo =
                new ValidityInfo(
                        clock.instant(),
                        clock.instant(),
                        clock.instant().plus(Duration.ofDays(365)));
        MobileSecurityObject expectedMso =
                new MobileSecurityObject(
                        "1.0",
                        "SHA-256",
                        mockValueDigests,
                        "org.iso.18013.5.1.mDL",
                        expectedValidityInfo);
        when(mockMobileSecurityObjectFactory.build(any())).thenReturn(expectedMso);

        // Arrange: Set up CBOR encoding for both the IssuerSignedItem and the MobileSecurityObject
        byte[] expectIssuerSignedItemCbor = "expectIssuerSignedItemCbor".getBytes();
        when(mockCborEncoder.encode(issuerSignedItem)).thenReturn(expectIssuerSignedItemCbor);

        byte[] expectedMsoCbor = "expectedMsoCbor".getBytes();
        when(mockCborEncoder.encode(expectedMso)).thenReturn(expectedMsoCbor);

        // Act: Build the document
        Document result = documentFactory.build(drivingLicenceDocument);

        // Assert: Check that docType is correct
        assertEquals(
                "org.iso.18013.5.1.mDL",
                result.docType(),
                "Document type should be \"org.iso.18013.5.1.mDL\"");

        // Assert: Check that both namespaces exist and contain CBOR encoded values
        Map<String, List<byte[]>> encodedNamespaces = result.issuerSigned().nameSpaces();
        assertEquals(2, encodedNamespaces.size(), "Should have 2 namespaces (ISO and UK)");
        assertTrue(encodedNamespaces.containsKey(Namespaces.UK), "Should contain UK namespace");
        assertTrue(encodedNamespaces.containsKey(Namespaces.ISO), "Should contain ISO namespace");
        encodedNamespaces
                .get(Namespaces.UK)
                .forEach(
                        item ->
                                assertArrayEquals(
                                        expectIssuerSignedItemCbor,
                                        item,
                                        "All UK namespace items should match the expected CBOR-encoded IssuerSignedItem"));
        encodedNamespaces
                .get(Namespaces.ISO)
                .forEach(
                        item ->
                                assertArrayEquals(
                                        expectIssuerSignedItemCbor,
                                        item,
                                        "All ISO namespace items should match the expected CBOR-encoded IssuerSignedItem"));

        // Assert: Check that issuerAuth contains the expected MobileSecurityObject CBOR bytes
        IssuerAuth issuerAuth = result.issuerSigned().issuerAuth();
        assertArrayEquals(
                expectedMsoCbor,
                issuerAuth.mobileSecurityObjectBytes(),
                "issuerAuth should contain the expected CBOR-encoded MobileSecurityObject");

        // Assert: Verify that mocks were called the expected number of times
        verify(mockIssuerSignedItemFactory, times(21)).build(any(), any());
        verify(mockMobileSecurityObjectFactory).build(any());
        verify(mockCborEncoder, times(21)).encode(issuerSignedItem);
        verify(mockCborEncoder).encode(expectedMso);
    }

    /**
     * Creates a DrivingLicenceDocument using the default DRIVING_PRIVILEGES for both
     * drivingPrivileges and provisionalDrivingPrivileges.
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
