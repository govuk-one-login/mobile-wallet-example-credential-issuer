package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DocumentFactoryTest {

    @Mock private IssuerSignedItemFactory mockIssuerSignedItemFactory;
    @Mock private CBOREncoder mockCborEncoder;

    private DocumentFactory documentFactory;
    Map<String, List<byte[]>> testNameSpaces = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        documentFactory = new DocumentFactory();
    }

    @Test
    void Should_BuildDocumentWithCorrectDocumentType() throws MDLException {
        Document document = documentFactory.build(testNameSpaces);

        assertEquals(
                "org.iso.18013.5.1.mDL",
                document.docType(),
                "Document should have the correct mobile driving licence document type");
    }

    @Test
    void Should_BuildWithCorrectNamespaceInIssuerSigned() throws MDLException {
        Document document = documentFactory.build(testNameSpaces);

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
        Document document = documentFactory.build(testNameSpaces);

        IssuerSigned issuerSigned = document.issuerSigned();
        List<byte[]> items = issuerSigned.nameSpaces().get("org.iso.18013.5.1");
        assertEquals(13, items.size(), "Should create one IssuerSignedItem per field");
    }

    @Test
    void Should_IncludeIssuerAuth() throws MDLException {
        Document document = documentFactory.build(testNameSpaces);

        IssuerSigned issuerSigned = document.issuerSigned();
        assertNotNull(issuerSigned.issuerAuth(), "IssuerAuth should not be null");
    }
}
