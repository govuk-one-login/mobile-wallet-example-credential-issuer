package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DocumentFactoryTest {

    private DocumentFactory documentFactory;
    Map<String, List<byte[]>> testNameSpaces = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        documentFactory = new DocumentFactory();
    }

    @Test
    void Should_BuildDocumentWithCorrectDocumentType() {
        Document document = documentFactory.build(testNameSpaces);

        assertEquals(
                "org.iso.18013.5.1.mDL",
                document.docType(),
                "Document should have the correct mobile driving licence document type");
    }

    @Test
    void Should_BuildDocumentWithOneNamespaceInIssuerSigned() {
        List<byte[]> testIssuerSignedItems1 = List.of(new byte[] {1, 2, 3}, new byte[] {4, 5, 6});
        testNameSpaces.put("nameSpace1", testIssuerSignedItems1);

        Document document = documentFactory.build(testNameSpaces);

        IssuerSigned issuerSigned = document.issuerSigned();
        Map<String, List<byte[]>> namespaces = issuerSigned.nameSpaces();
        assertEquals(1, namespaces.size(), "There should be one namespace object in namespaces");
    }

    @Test
    void Should_BuildDocumentWithTwoNamespacesInIssuerSigned() {
        List<byte[]> testIssuerSignedItems1 = List.of(new byte[] {1, 2, 3}, new byte[] {4, 5, 6});
        List<byte[]> testIssuerSignedItems2 = List.of(new byte[] {1, 2, 3}, new byte[] {4, 5, 6});
        testNameSpaces.put("nameSpace1", testIssuerSignedItems1);
        testNameSpaces.put("nameSpace2", testIssuerSignedItems2);

        Document document = documentFactory.build(testNameSpaces);

        IssuerSigned issuerSigned = document.issuerSigned();
        Map<String, List<byte[]>> namespaces = issuerSigned.nameSpaces();
        assertEquals(2, namespaces.size(), "There should be two namespace objects in namespaces");
    }

    @Test
    void Should_BuildDocumentWithIssuerAuthInIssuerSigned() {
        Document document = documentFactory.build(testNameSpaces);

        IssuerSigned issuerSigned = document.issuerSigned();
        assertNotNull(issuerSigned.issuerAuth(), "IssuerAuth should not be null");
    }
}
