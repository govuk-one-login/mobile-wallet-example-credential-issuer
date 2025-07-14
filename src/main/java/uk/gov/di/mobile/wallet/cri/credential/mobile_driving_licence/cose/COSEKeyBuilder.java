package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.LinkedHashMap;
import java.util.Map;

public class COSEKeyBuilder {
    private final Map<Integer, Object> coseKey = new LinkedHashMap<>();

    public COSEKeyBuilder keyType(int kty) {
        coseKey.put(1, kty);
        return this;
    }

    public COSEKeyBuilder curve(int crv) {
        coseKey.put(-1, crv);
        return this;
    }

    public COSEKeyBuilder xCoordinate(byte[] x) {
        coseKey.put(-2, x);
        return this;
    }

    public COSEKeyBuilder yCoordinate(byte[] y) {
        coseKey.put(-3, y);
        return this;
    }

    public COSEKey build() {
        if (!coseKey.containsKey(1)) {
            throw new IllegalStateException("Key type must be set");
        }
        if (!coseKey.containsKey(-1)) {
            throw new IllegalStateException("EC curve must be set");
        }
        if (!coseKey.containsKey(-2)) {
            throw new IllegalStateException("x-coordinate must be set");
        }
        if (!coseKey.containsKey(-3)) {
            throw new IllegalStateException("y-coordinate must be set");
        }
        return new COSEKey(new LinkedHashMap<>(coseKey));
    }
}
