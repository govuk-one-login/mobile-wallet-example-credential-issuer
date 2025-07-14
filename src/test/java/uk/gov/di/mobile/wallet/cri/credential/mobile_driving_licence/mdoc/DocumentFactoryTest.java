package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;

import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.constants.NamespaceTypes.GB;
import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.constants.NamespaceTypes.ISO;

@ExtendWith(MockitoExtension.class)
class DocumentFactoryTest {

    @Mock private NamespacesFactory mockNamespacesFactory;
    @Mock private IssuerSignedFactory mockIssuerSignedFactory;
    @Mock private ECPublicKey mockEcPublicKey;

    /**
     * Test that the DocumentFactory creates both ISO and UK namespaces, and that the correct number
     * of fields are present in each.
     */
    @Test
    void Should_BuildISOAndUKNamespaces() throws Exception {
        // Arrange: Set up DocumentFactory and test DrivingLicenceDocument
        DocumentFactory documentFactory =
                new DocumentFactory(mockNamespacesFactory, mockIssuerSignedFactory);
        DrivingLicenceDocument mockDrivingLicenceDocument = mock(DrivingLicenceDocument.class);

        // Arrange: Create dummy namespaces map
        Map<String, List<IssuerSignedItem>> dummyNamespacesMap = new HashMap<>();
        dummyNamespacesMap.put(ISO, createDummyIssuerSignedItemList(18));
        dummyNamespacesMap.put(GB, createDummyIssuerSignedItemList(3));
        Namespaces dummyNamespaces = new Namespaces(dummyNamespacesMap);

        // Arrange: Mock NamespacesFactory to return dummy NamespaceTypes
        when(mockNamespacesFactory.build(any(DrivingLicenceDocument.class)))
                .thenReturn(dummyNamespaces);

        // Arrange: Mock IssuerSignedFactory to return dummy IssuerSigned
        IssuerSigned mockIssuerSigned = mock(IssuerSigned.class);
        // Simulate IssuerSigned.nameSpaces() returning a map of String -> List<byte[]>
        Map<String, List<byte[]>> dummyEncodedNamespaces = new HashMap<>();
        dummyEncodedNamespaces.put(ISO, Collections.nCopies(18, "testCbor".getBytes()));
        dummyEncodedNamespaces.put(GB, Collections.nCopies(3, "testCbor".getBytes()));
        when(mockIssuerSigned.nameSpaces()).thenReturn(dummyEncodedNamespaces);
        when(mockIssuerSignedFactory.build(any(Namespaces.class), mockEcPublicKey))
                .thenReturn(mockIssuerSigned);

        // Act: Build the document
        Document result = documentFactory.build(mockDrivingLicenceDocument, mockEcPublicKey);

        // Assert: Check that both namespaces exist and have the expected number of fields
        Map<String, List<byte[]>> namespaces = result.issuerSigned().nameSpaces();
        assertEquals(2, namespaces.size(), "Should have 2 namespaces (ISO and UK)");
        assertTrue(namespaces.containsKey(ISO), "Should contain ISO namespace");
        assertTrue(namespaces.containsKey(GB), "Should contain UK namespace");
        assertEquals(18, namespaces.get(ISO).size(), "ISO namespace should have 18 fields");
        assertEquals(3, namespaces.get(GB).size(), "UK namespace should have 3 fields");
    }

    private List<IssuerSignedItem> createDummyIssuerSignedItemList(int count) {
        List<IssuerSignedItem> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(mock(IssuerSignedItem.class));
        }
        return list;
    }
}
