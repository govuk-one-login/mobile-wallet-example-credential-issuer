package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentFactoryTest {

    private static final int EXPECTED_ISO_FIELDS = 15;
    private static final int EXPECTED_UK_FIELDS = 2;
    private static final byte[] MOCK_CBOR_BYTES = {0x01, 0x02};
    private static final DrivingPrivilege[] DRIVING_PRIVILEGES = {
        new DrivingPrivilege("B", null, null)
    };

    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;
    @Mock private MobileSecurityObjectFactory mockMobileSecurityObjectFactory;
    @Mock private CBOREncoder mockCborEncoder;

    private DrivingLicenceDocument drivingLicenceDocument;
    private DocumentFactory documentFactory;

    @BeforeEach
    void setUp() {
        IssuerSignedItem dummyItem = mock(IssuerSignedItem.class);
        when(mockIssuerSignedItemFactory.build(anyString(), any())).thenReturn(dummyItem);
        when(mockCborEncoder.encode(any())).thenReturn(MOCK_CBOR_BYTES);

        documentFactory =
                new DocumentFactory(
                        mockIssuerSignedItemFactory,
                        mockMobileSecurityObjectFactory,
                        mockCborEncoder);
        drivingLicenceDocument = createTestDrivingLicenceDocument(DRIVING_PRIVILEGES);
    }

    @Test
    void Should_BuildDocument() {
        Document result = documentFactory.build(drivingLicenceDocument);

        assertEquals("org.iso.18013.5.1.mDL", result.docType());
        assertNotNull(result.issuerSigned());
    }

    @Test
    void Should_BuildISOAndUKNamespaces() {
        Document result = documentFactory.build(drivingLicenceDocument);

        Map<String, List<byte[]>> namespaces = result.issuerSigned().nameSpaces();
        assertEquals(2, namespaces.size());
        assertTrue(namespaces.containsKey(Namespaces.ISO), "Should contain ISO namespace");
        assertTrue(namespaces.containsKey(Namespaces.UK), "Should contain UK namespace");
        assertEquals(EXPECTED_ISO_FIELDS, namespaces.get(Namespaces.ISO).size());
        assertEquals(EXPECTED_UK_FIELDS, namespaces.get(Namespaces.UK).size());
    }

    @Test
    void Should_BuildIssuerSignedItemsForEachFieldInDrivingLicence_ISONamespace()
            throws MDLException {
        Document result = documentFactory.build(drivingLicenceDocument);

        List<byte[]> isoNamespace = result.issuerSigned().nameSpaces().get(Namespaces.ISO);
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
        Document result = documentFactory.build(drivingLicenceDocument);

        List<byte[]> ukNamespace = result.issuerSigned().nameSpaces().get(Namespaces.UK);
        assertEquals(
                EXPECTED_UK_FIELDS,
                ukNamespace.size(),
                "Should create one IssuerSignedItem per UK namespace attribute");
        ukNamespace.forEach(bytes -> assertArrayEquals(MOCK_CBOR_BYTES, bytes));
        verify(mockIssuerSignedItemFactory)
                .build("provisional_driving_privileges", Optional.ofNullable(DRIVING_PRIVILEGES));
        verify(mockIssuerSignedItemFactory).build("title", "Miss");
    }

    @Test
    void Should_NotBuildIssuerSignedItemForProvisionalDrivingPrivileges_When_ItsValueIsNull()
            throws MDLException {
        DrivingPrivilege[] provisionDrivingPrivileges = null;
        DrivingLicenceDocument drivingLicenceWithProvisionalNull =
                createTestDrivingLicenceDocument(provisionDrivingPrivileges);

        Document result = documentFactory.build(drivingLicenceWithProvisionalNull);

        List<byte[]> ukNamespace = result.issuerSigned().nameSpaces().get(Namespaces.UK);
        assertEquals(
                EXPECTED_UK_FIELDS - 1,
                ukNamespace.size(),
                "Should not create an IssuerSignedItem for provisional driving privileges when its value is null");
        verify(mockIssuerSignedItemFactory, never())
                .build("provisional_driving_privileges", Optional.empty());
    }

    //    @Test
    //    void Should_ReturnIssuerSignedWithCorrectNamespacesAndIssuerAuth() throws Exception {
    //        MobileSecurityObject mockMso = mock(MobileSecurityObject.class);
    //        when(mockMobileSecurityObjectFactory.build(any(Map.class))).thenReturn(mockMso);
    //
    //        byte[] mockMsoBytes = {1,2,3};
    //        when(mockCborEncoder.encode(mockMso)).thenReturn(mockMsoBytes);
    //
    //        // Act
    //        Document result = documentFactory.build(drivingLicenceDocument);
    //
    //        assertNotNull(result);
    //        Map<String, List<byte[]>> encodedNamespaces = result.issuerSigned().nameSpaces();
    //        assertEquals(2, encodedNamespaces.size());
    //
    //        IssuerAuth issuerAuth = result.issuerSigned().issuerAuth();
    //        assertNotNull(issuerAuth);
    //        assertArrayEquals(mockMsoBytes, issuerAuth.mobileSecurityObjectBytes());
    //    }

    //    @Test
    //    void Should_BuildDocumentWithIssuerAuthInIssuerSigned() {
    //        Document document = documentFactory.build(drivingLicenceDocument);
    //
    //        IssuerSigned issuerSigned = document.issuerSigned();
    //        assertNotNull(issuerSigned.issuerAuth(), "IssuerAuth should not be null");
    //    }

    private DrivingLicenceDocument createTestDrivingLicenceDocument(
            DrivingPrivilege[] provisionalDrivingPrivileges) {
        String[] address = {"123 Main St", "Apt 4B"};
        return new DrivingLicenceDocument(
                "Doe",
                "John",
                "Miss",
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
