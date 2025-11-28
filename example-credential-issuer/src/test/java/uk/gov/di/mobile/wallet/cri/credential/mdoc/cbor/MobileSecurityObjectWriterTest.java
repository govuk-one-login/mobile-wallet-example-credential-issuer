package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor;

import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.DeviceKeyInfo;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.KeyAuthorizations;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.MobileSecurityObject;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.Status;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.StatusList;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.ValidityInfo;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.ValueDigests;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.constants.COSEKeyTypes;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class MobileSecurityObjectWriterTest {

    @Mock private CBORGenerator cborGenerator;

    @Test
    void Should_WriteMobileSecurityObject() throws IOException {
        MobileSecurityObject valueToWrite = getTestMobileSecurityObject();

        MobileSecurityObjectWriter.write(cborGenerator, valueToWrite);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(7);
        inOrder.verify(cborGenerator).writeStringField("version", valueToWrite.version());
        inOrder.verify(cborGenerator)
                .writeStringField("digestAlgorithm", valueToWrite.digestAlgorithm());
        inOrder.verify(cborGenerator).writeFieldName("valueDigests");
        inOrder.verify(cborGenerator).writeObject(valueToWrite.valueDigests());
        inOrder.verify(cborGenerator).writeFieldName("deviceKeyInfo");
        inOrder.verify(cborGenerator).writeObject(valueToWrite.deviceKeyInfo());
        inOrder.verify(cborGenerator).writeFieldName("docType");
        inOrder.verify(cborGenerator).writeString(valueToWrite.docType());
        inOrder.verify(cborGenerator).writeFieldName("validityInfo");
        inOrder.verify(cborGenerator).writeObject(valueToWrite.validityInfo());
        inOrder.verify(cborGenerator).writeFieldName("status");
        inOrder.verify(cborGenerator).writeObject(valueToWrite.status());
        inOrder.verify(cborGenerator).writeEndObject();
        verifyNoMoreInteractions(cborGenerator);
    }

    private static @NotNull MobileSecurityObject getTestMobileSecurityObject() {
        byte[] x = new byte[] {0x01, 0x02, 0x03};
        byte[] y = new byte[] {0x04, 0x05, 0x06};
        COSEKey coseKey = new COSEKey(COSEKeyTypes.EC2, COSEEllipticCurves.P256, x, y);
        KeyAuthorizations keyAuthorizations =
                new KeyAuthorizations(Set.of("testNamespace1", "testNamespace2"));
        DeviceKeyInfo deviceKeyInfo = new DeviceKeyInfo(coseKey, keyAuthorizations);

        Map<Integer, byte[]> digestMap = new HashMap<>();
        digestMap.put(1, new byte[] {0x01, 0x02, 0x03});
        Map<String, Map<Integer, byte[]>> valueDigestsMap = new HashMap<>();
        valueDigestsMap.put("org.iso.18013.5.1", digestMap);
        ValueDigests valueDigests = new ValueDigests(valueDigestsMap);

        ValidityInfo validityInfo =
                new ValidityInfo(
                        Instant.parse("2025-06-27T12:00:00Z"),
                        Instant.parse("2025-06-27T12:00:00Z"),
                        Instant.parse("2026-06-27T12:00:00Z"));

        StatusList statusList = new StatusList(0, "https://test-status-list.gov.uk/t/3B0F3BD087A7");
        Status status = new Status(statusList);

        return new MobileSecurityObject(
                "1.0",
                "SHA-256",
                deviceKeyInfo,
                valueDigests,
                "org.iso.18013.5.1.mDL",
                validityInfo,
                status);
    }
}
