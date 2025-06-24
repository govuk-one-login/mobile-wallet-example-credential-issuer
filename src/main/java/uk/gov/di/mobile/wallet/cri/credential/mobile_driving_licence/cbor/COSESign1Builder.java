package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class COSESign1Builder {
    private COSEProtectedHeader protectedHeader;
    private COSEUnprotectedHeader unprotectedHeader;
    private byte[] payload;
    private byte[] signature;
    private final CBOREncoder cborEncoder;

    public COSESign1Builder(CBOREncoder cborEncoder) {
        this.cborEncoder = cborEncoder;
    }
    public COSESign1Builder protectedHeader(COSEProtectedHeader header) {
        this.protectedHeader = header;
        return this;
    }

    public COSESign1Builder unprotectedHeader(COSEUnprotectedHeader header) {
        this.unprotectedHeader = header;
        return this;
    }

    public COSESign1Builder payload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public COSESign1Builder signature(byte[] signature) {
        this.signature = signature;
        return this;
    }

    public COSESign1 build() {
        byte[] protectedHeaderBytes = protectedHeader.getCborBytes();

        Map<Integer, Object> unprotectedHeaderMap = unprotectedHeader.getHeaderMap();

        byte[] payloadBytes = payload;

        byte[] signatureBytes = signature;

        List<Object> coseSign1Array = new ArrayList<>();
        coseSign1Array.add(protectedHeaderBytes);
        coseSign1Array.add(unprotectedHeaderMap);
        coseSign1Array.add(payloadBytes);
        coseSign1Array.add(signatureBytes);

        byte[] coseSign1Bytes = cborEncoder.encode(coseSign1Array);

        return new COSESign1(coseSign1Bytes);
    }
}
