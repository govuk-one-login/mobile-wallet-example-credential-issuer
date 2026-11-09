package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.CBOREncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueDigestsFactoryTest {

    private static final String DIGEST_ALGORITHM = "SHA-256";

    @Mock private CBOREncoder cborEncoder;

    private static byte[] sha256(byte[] input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(DIGEST_ALGORITHM).digest(input);
    }

    /** Verifies that the digest algorithm is returned as expected. */
    @Test
    void Should_ReturnDigestAlgorithm() {
        String algorithm =
                new ValueDigestsFactory(cborEncoder, DIGEST_ALGORITHM).getDigestAlgorithm();

        assertEquals(DIGEST_ALGORITHM, algorithm);
    }

    /** Verifies correct digest generation for a single namespace with a single item. */
    @Test
    void Should_AddSingleNameSpaceWithSingleDigest() throws NoSuchAlgorithmException {
        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(1, new byte[] {1, 2, 3}, "elementIdentifier", "elementValue");
        byte[] expectedCbor = "testCbor1".getBytes();
        when(cborEncoder.encode(issuerSignedItem)).thenReturn(expectedCbor);
        byte[] expectedDigest = sha256(expectedCbor);
        Namespaces namespaces = new Namespaces(Map.of("namespace", List.of(issuerSignedItem)));
        ValueDigests result =
                new ValueDigestsFactory(cborEncoder, DIGEST_ALGORITHM)
                        .createFromNamespaces(namespaces);

        assertArrayEquals(expectedDigest, result.valueDigests().get("namespace").get(1));
        verify(cborEncoder).encode(issuerSignedItem);
    }

    /** Verifies correct digest generation for multiple nameSpaces, each with a single item. */
    @Test
    void Should_AddMultipleNameSpacesWithSingleDigest() throws NoSuchAlgorithmException {
        IssuerSignedItem issuerSignedItem1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2, 3}, "elementIdentifier1", "elementValue1");
        byte[] expectedCbor1 = "testCbor1".getBytes();
        when(cborEncoder.encode(issuerSignedItem1)).thenReturn(expectedCbor1);
        IssuerSignedItem issuerSignedItem2 =
                new IssuerSignedItem(
                        2, new byte[] {1, 2, 3, 4}, "elementIdentifier2", "elementValue2");
        byte[] expectedCbor2 = "testCbor2".getBytes();
        when(cborEncoder.encode(issuerSignedItem2)).thenReturn(expectedCbor2);
        byte[] expectedDigest1 = sha256(expectedCbor1);
        byte[] expectedDigest2 = sha256(expectedCbor2);

        Namespaces namespaces =
                new Namespaces(
                        Map.of(
                                "namespace1",
                                List.of(issuerSignedItem1),
                                "namespace2",
                                List.of(issuerSignedItem2)));
        ValueDigests result =
                new ValueDigestsFactory(cborEncoder, DIGEST_ALGORITHM)
                        .createFromNamespaces(namespaces);

        assertArrayEquals(
                expectedDigest1,
                result.valueDigests().get("namespace1").get(issuerSignedItem1.digestId()));
        assertArrayEquals(
                expectedDigest2,
                result.valueDigests().get("namespace2").get(issuerSignedItem2.digestId()));
        verify(cborEncoder).encode(issuerSignedItem1);
        verify(cborEncoder).encode(issuerSignedItem2);
    }

    /** Verifies correct digest generation for multiple nameSpaces with multiple items. */
    @Test
    void Should_AddMultipleNameSpacesWithMultipleDigests() throws NoSuchAlgorithmException {
        IssuerSignedItem issuerSignedItem1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2, 3}, "elementIdentifier1", "elementValue1");
        byte[] expectedCbor1 = "testCbor1".getBytes();
        when(cborEncoder.encode(issuerSignedItem1)).thenReturn(expectedCbor1);
        IssuerSignedItem issuerSignedItem2 =
                new IssuerSignedItem(
                        2, new byte[] {1, 2, 3, 4}, "elementIdentifier2", "elementValue2");
        byte[] expectedCbor2 = "testCbor2".getBytes();
        when(cborEncoder.encode(issuerSignedItem2)).thenReturn(expectedCbor2);
        IssuerSignedItem issuerSignedItem3 =
                new IssuerSignedItem(
                        3, new byte[] {1, 2, 3, 4, 5}, "elementIdentifier3", "elementValue3");
        byte[] expectedCbor3 = "testCbor3".getBytes();
        when(cborEncoder.encode(issuerSignedItem3)).thenReturn(expectedCbor3);
        byte[] expectedDigest1 = sha256(expectedCbor1);
        byte[] expectedDigest2 = sha256(expectedCbor2);
        byte[] expectedDigest3 = sha256(expectedCbor3);

        Namespaces namespaces =
                new Namespaces(
                        Map.of(
                                "namespace1",
                                List.of(issuerSignedItem1),
                                "namespace2",
                                List.of(issuerSignedItem2, issuerSignedItem3)));
        ValueDigests result =
                new ValueDigestsFactory(cborEncoder, DIGEST_ALGORITHM)
                        .createFromNamespaces(namespaces);

        assertArrayEquals(
                expectedDigest1,
                result.valueDigests().get("namespace1").get(issuerSignedItem1.digestId()));
        assertArrayEquals(
                expectedDigest2,
                result.valueDigests().get("namespace2").get(issuerSignedItem2.digestId()));
        assertArrayEquals(
                expectedDigest3,
                result.valueDigests().get("namespace2").get(issuerSignedItem3.digestId()));
        verify(cborEncoder).encode(issuerSignedItem1);
        verify(cborEncoder).encode(issuerSignedItem2);
        verify(cborEncoder).encode(issuerSignedItem3);
    }
}
