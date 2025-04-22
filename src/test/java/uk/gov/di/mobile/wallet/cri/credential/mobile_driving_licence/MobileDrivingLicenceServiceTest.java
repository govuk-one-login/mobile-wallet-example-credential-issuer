package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Document;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DocumentFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.NamespaceFactory;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileDrivingLicenceServiceTest {

    private static final String MOBILE_DRIVING_LICENCE_NAMESPACE = "org.iso.18013.5.1";

    @Mock private CBOREncoder cborEncoder;

    @Mock private DocumentFactory documentFactory;

    @Mock private NamespaceFactory namespaceFactory;

    @Mock private DrivingLicenceDocument mockDrivingLicenceDocument;

    @Mock private Document mockDocument;

    private MobileDrivingLicenceService mobileDrivingLicenceService;

    @BeforeEach
    void setUp() {
        mobileDrivingLicenceService =
                new MobileDrivingLicenceService(cborEncoder, documentFactory, namespaceFactory);
    }

    @Test
    void Should_CreateMobileDrivingLicence() throws MDLException {
        List<byte[]> mockIssuerSignedItems = List.of(new byte[] {1, 2, 3}, new byte[] {4, 5, 6});
        byte[] mockCborEncoding = new byte[] {10, 20, 30, 40, 50};
        String expectedHexString = HexFormat.of().formatHex(mockCborEncoding);
        when(namespaceFactory.build(mockDrivingLicenceDocument)).thenReturn(mockIssuerSignedItems);
        when(documentFactory.build(any())).thenReturn(mockDocument);
        when(cborEncoder.encode(mockDocument)).thenReturn(mockCborEncoding);

        String actualHexString =
                mobileDrivingLicenceService.createMobileDrivingLicence(mockDrivingLicenceDocument);

        assertEquals(
                expectedHexString,
                actualHexString,
                "The actual hex string should match the expected result");

        // Verify the namespace map was correctly constructed
        ArgumentCaptor<Map<String, List<byte[]>>> namespaceCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(documentFactory).build(namespaceCaptor.capture());
        Map<String, List<byte[]>> capturedNamespaces = namespaceCaptor.getValue();
        assertEquals(1, capturedNamespaces.size(), "Should contain exactly one namespace");
        assertTrue(
                capturedNamespaces.containsKey(MOBILE_DRIVING_LICENCE_NAMESPACE),
                "Should contain the MDL namespace");
        assertEquals(
                mockIssuerSignedItems,
                capturedNamespaces.get(MOBILE_DRIVING_LICENCE_NAMESPACE),
                "Items in namespace should match the mock items");
    }

    @Test
    void Should_PropagateException_When_NameSpaceFactoryThrowsMDLException() throws MDLException {
        MDLException expectedException =
                new MDLException("Some NamespaceFactory error", new RuntimeException());
        when(namespaceFactory.build(mockDrivingLicenceDocument)).thenThrow(expectedException);

        MDLException thrownException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument));
        assertEquals(expectedException, thrownException);
        verify(documentFactory, never()).build(any());
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagateException_When_CBOREncoderThrowsMDLException() throws MDLException {
        List<byte[]> mockIssuerSignedItems = new ArrayList<>();
        MDLException expectedException =
                new MDLException("Some CBOREncoder error", new RuntimeException());
        when(namespaceFactory.build(mockDrivingLicenceDocument)).thenReturn(mockIssuerSignedItems);
        when(documentFactory.build(any())).thenReturn(mockDocument);
        when(cborEncoder.encode(mockDocument)).thenThrow(expectedException);

        MDLException thrownException =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument));
        assertEquals(expectedException, thrownException);
    }
}
