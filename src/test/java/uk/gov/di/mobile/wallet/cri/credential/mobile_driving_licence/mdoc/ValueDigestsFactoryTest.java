package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueDigestsFactoryTest {

    @Mock private MessageDigest mockMessageDigest;

    @Mock private CBORMapper mockCborMapper;

    /** Verifies that the digest algorithm is returned as expected. */
    @Test
    void Should_ReturnDigestAlgorithm() {
        when(mockMessageDigest.getAlgorithm()).thenReturn("SHA-256-TEST");

        String algorithm = mockMessageDigest.getAlgorithm();

        assertEquals("SHA-256-TEST", algorithm);
    }

    /** Verifies correct digest generation for a single namespace with a single item. */
    @Test
    void Should_AddSingleNameSpaceWithSingleDigest() throws JsonProcessingException {
        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(1, new byte[] {1, 2, 3}, "elementIdentifier", "elementValue");
        byte[] expectedCbor = "testCbor1".getBytes();
        when(mockCborMapper.writeValueAsBytes(issuerSignedItem)).thenReturn(expectedCbor);
        byte[] expectedDigest = "testDigest1".getBytes();
        when(mockMessageDigest.digest(expectedCbor)).thenReturn(expectedDigest);

        Map<String, List<IssuerSignedItem>> namespaces =
                Map.of("namespace", List.of(issuerSignedItem));
        ValueDigests result =
                new ValueDigestsFactory(mockCborMapper, mockMessageDigest)
                        .createFromNamespaces(namespaces);

        Map<String, Map<Integer, byte[]>> expectedDigests =
                Map.of("namespace", Map.of(1, expectedDigest));
        assertEquals(expectedDigests, result.valueDigests());
        verify(mockCborMapper).writeValueAsBytes(issuerSignedItem);
        verify(mockMessageDigest).digest(expectedCbor);
    }

    /** Verifies correct digest generation for multiple namespaces, each with a single item. */
    @Test
    void Should_AddMultipleNameSpacesWithSingleDigest() throws JsonProcessingException {
        IssuerSignedItem issuerSignedItem1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2, 3}, "elementIdentifier1", "elementValue1");
        byte[] expectedCbor1 = "testCbor1".getBytes();
        when(mockCborMapper.writeValueAsBytes(issuerSignedItem1)).thenReturn(expectedCbor1);
        IssuerSignedItem issuerSignedItem2 =
                new IssuerSignedItem(
                        2, new byte[] {1, 2, 3, 4}, "elementIdentifier2", "elementValue2");
        byte[] expectedCbor2 = "testCbor2".getBytes();
        when(mockCborMapper.writeValueAsBytes(issuerSignedItem2)).thenReturn(expectedCbor2);
        byte[] expectedDigest1 = "testDigest1".getBytes();
        when(mockMessageDigest.digest(expectedCbor1)).thenReturn(expectedDigest1);
        byte[] expectedDigest2 = "testDigest2".getBytes();
        when(mockMessageDigest.digest(expectedCbor2)).thenReturn(expectedDigest2);

        Map<String, List<IssuerSignedItem>> namespaces =
                Map.of(
                        "namespace1",
                        List.of(issuerSignedItem1),
                        "namespace2",
                        List.of(issuerSignedItem2));
        ValueDigests result =
                new ValueDigestsFactory(mockCborMapper, mockMessageDigest)
                        .createFromNamespaces(namespaces);

        Map<String, Map<Integer, byte[]>> expectedDigests =
                Map.of(
                        "namespace1", Map.of(issuerSignedItem1.digestId(), expectedDigest1),
                        "namespace2", Map.of(issuerSignedItem2.digestId(), expectedDigest2));
        assertEquals(expectedDigests, result.valueDigests());
        verify(mockCborMapper).writeValueAsBytes(issuerSignedItem1);
        verify(mockCborMapper).writeValueAsBytes(issuerSignedItem2);
        verify(mockMessageDigest).digest(expectedCbor1);
        verify(mockMessageDigest).digest(expectedCbor2);
    }

    /** Verifies correct digest generation for multiple namespaces with multiple items. */
    @Test
    void Should_AddMultipleNameSpacesWithMultipleDigests() throws JsonProcessingException {
        IssuerSignedItem issuerSignedItem1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2, 3}, "elementIdentifier1", "elementValue1");
        byte[] expectedCbor1 = "testCbor1".getBytes();
        when(mockCborMapper.writeValueAsBytes(issuerSignedItem1)).thenReturn(expectedCbor1);
        IssuerSignedItem issuerSignedItem2 =
                new IssuerSignedItem(
                        2, new byte[] {1, 2, 3, 4}, "elementIdentifier2", "elementValue2");
        byte[] expectedCbor2 = "testCbor2".getBytes();
        when(mockCborMapper.writeValueAsBytes(issuerSignedItem2)).thenReturn(expectedCbor2);
        IssuerSignedItem issuerSignedItem3 =
                new IssuerSignedItem(
                        3, new byte[] {1, 2, 3, 4, 5}, "elementIdentifier3", "elementValue3");
        byte[] expectedCbor3 = "testCbor3".getBytes();
        when(mockCborMapper.writeValueAsBytes(issuerSignedItem3)).thenReturn(expectedCbor3);
        byte[] expectedDigest1 = "testDigest1".getBytes();
        when(mockMessageDigest.digest(expectedCbor1)).thenReturn(expectedDigest1);
        byte[] expectedDigest2 = "testDigest2".getBytes();
        when(mockMessageDigest.digest(expectedCbor2)).thenReturn(expectedDigest2);
        byte[] expectedDigest3 = "testDigest3".getBytes();
        when(mockMessageDigest.digest(expectedCbor3)).thenReturn(expectedDigest3);

        Map<String, List<IssuerSignedItem>> namespaces =
                Map.of(
                        "namespace1",
                        List.of(issuerSignedItem1),
                        "namespace2",
                        List.of(issuerSignedItem2, issuerSignedItem3));
        ValueDigests result =
                new ValueDigestsFactory(mockCborMapper, mockMessageDigest)
                        .createFromNamespaces(namespaces);

        Map<String, Map<Integer, byte[]>> expectedDigests =
                Map.of(
                        "namespace1",
                        Map.of(issuerSignedItem1.digestId(), expectedDigest1),
                        "namespace2",
                        Map.of(
                                issuerSignedItem2.digestId(),
                                expectedDigest2,
                                issuerSignedItem3.digestId(),
                                expectedDigest3));
        assertEquals(expectedDigests, result.valueDigests());
        verify(mockCborMapper).writeValueAsBytes(issuerSignedItem1);
        verify(mockCborMapper).writeValueAsBytes(issuerSignedItem2);
        verify(mockCborMapper).writeValueAsBytes(issuerSignedItem3);
        verify(mockMessageDigest).digest(expectedCbor1);
        verify(mockMessageDigest).digest(expectedCbor2);
        verify(mockMessageDigest).digest(expectedCbor3);
    }

    /** Verifies that an MDLException is thrown when CBOR serialization fails. */
    @Test
    void Should_ThrowMDLExceptionOnJsonProcessingException() throws JsonProcessingException {
        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(1, new byte[] {1, 2, 3}, "elementIdentifier", "elementValue");
        when(mockCborMapper.writeValueAsBytes(any()))
                .thenThrow(new JsonProcessingException("Test error") {});
        Map<String, List<IssuerSignedItem>> namespaces =
                Map.of("namespace", List.of(issuerSignedItem));

        ValueDigestsFactory valueDigestsFactory =
                new ValueDigestsFactory(mockCborMapper, mockMessageDigest);

        assertThrows(
                MDLException.class, () -> valueDigestsFactory.createFromNamespaces(namespaces));
    }
}
