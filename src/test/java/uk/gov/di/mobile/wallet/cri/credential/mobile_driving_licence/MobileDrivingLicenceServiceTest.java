package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Document;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DocumentFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileDrivingLicenceServiceTest {

    private static final String MOCK_NAMESPACE_1 = "mockNamespace1";
    private static final String MOCK_NAMESPACE_2 = "mockNamespace2";
    private static final String EXPECTED_HEX = "a10102";

    @Mock private CBOREncoder cborEncoder;
    @Mock private DocumentFactory documentFactory;
    @Mock private DocumentFactory documentFactory;
    @Mock private DrivingLicenceDocument mockDrivingLicenceDocument;
    @Mock private Document mockMdoc;

    private MobileDrivingLicenceService mobileDrivingLicenceService;

    @BeforeEach
    void setUp() {
        mobileDrivingLicenceService =
                new MobileDrivingLicenceService(cborEncoder, documentFactory, documentFactory);
    }

    @Test
    void Should_CreateMobileDrivingLicence() throws MDLException {
        Map<String, List<byte[]>> mockNamespaces =
                Map.of(
                        MOCK_NAMESPACE_1, List.of(new byte[] {0x01, 0x02}),
                        MOCK_NAMESPACE_2, List.of(new byte[] {0x01, 0x02}));
        byte[] mockCborBytes = new byte[] {(byte) 0xA1, 0x01, 0x02};

        when(documentFactory.build(mockNamespaces)).thenReturn(mockMdoc);
        when(documentFactory.buildAllNamespaces(mockDrivingLicenceDocument))
                .thenReturn(mockNamespaces);
        when(cborEncoder.encode(mockMdoc)).thenReturn(mockCborBytes);

        String result =
                mobileDrivingLicenceService.createMobileDrivingLicence(mockDrivingLicenceDocument);

        assertEquals(
                EXPECTED_HEX, result, "The actual hex string should match the expected result");
        verify(documentFactory).buildAllNamespaces(mockDrivingLicenceDocument);
        verify(documentFactory).build(mockNamespaces);
        verify(cborEncoder).encode(mockMdoc);
    }

    @Test
    void Should_PropagateException_When_NameSpaceFactoryThrowsMDLException() throws MDLException {
        MDLException expectedException =
                new MDLException("Some DocumentFactory error", new RuntimeException());
        when(documentFactory.buildAllNamespaces(mockDrivingLicenceDocument))
                .thenThrow(expectedException);

        MDLException thrown =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument));
        assertEquals(expectedException, thrown);
        verify(documentFactory, never()).build(any());
        verify(cborEncoder, never()).encode(any());
    }

    @Test
    void Should_PropagateException_When_CBOREncoderThrowsMDLException() throws MDLException {
        Map<String, List<byte[]>> namespaces = Map.of();
        MDLException expectedException =
                new MDLException("Some CBOREncoder error", new RuntimeException());
        when(documentFactory.buildAllNamespaces(mockDrivingLicenceDocument)).thenReturn(namespaces);
        when(documentFactory.build(any())).thenReturn(mockMdoc);
        when(cborEncoder.encode(mockMdoc)).thenThrow(expectedException);

        MDLException thrown =
                assertThrows(
                        MDLException.class,
                        () ->
                                mobileDrivingLicenceService.createMobileDrivingLicence(
                                        mockDrivingLicenceDocument));
        assertEquals(expectedException, thrown);
        verify(cborEncoder).encode(mockMdoc);
    }
}
