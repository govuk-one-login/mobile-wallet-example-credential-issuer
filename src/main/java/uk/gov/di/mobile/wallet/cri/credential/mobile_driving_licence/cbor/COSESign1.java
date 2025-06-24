package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

public class COSESign1 {
  private final byte[] cborBytes;
  public COSESign1(byte[] cborBytes) {
    this.cborBytes = cborBytes;
  }
  public byte[] getCborBytes() {
    return cborBytes;
  }
}