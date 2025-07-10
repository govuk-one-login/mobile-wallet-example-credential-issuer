package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.LinkedHashMap;
import java.util.Map;

public class COSEKeyBuilder {
  private final Map<Integer, Object> coseKey = new LinkedHashMap<>();

  public COSEKeyBuilder keyType(int kty) {
    coseKey.put(1, kty);
    return this;
  }

  public COSEKeyBuilder curve(int kid) {
    coseKey.put(-1, kid);
    return this;
  }

  public COSEKeyBuilder xCoordinate(int alg) {
    coseKey.put(-2, alg);
    return this;
  }

  public COSEKeyBuilder yCoordinate(int alg) {
    coseKey.put(-3, alg);
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