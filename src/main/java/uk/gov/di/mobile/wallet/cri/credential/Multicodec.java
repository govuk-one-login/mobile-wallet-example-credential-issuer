package uk.gov.di.mobile.wallet.cri.credential;

public enum Multicodec {
    P256_PUB("1200", 33);

    /** The codec code value. */
    public final String code;

    /** The compressed public key length in bytes. */
    public final int expectedKeyLength;

    /** The unsigned varint encoding of the code. */
    public final String uvarintcode;

    Multicodec(String code, int expectedKeyLength) {
        this.code = code;
        this.expectedKeyLength = expectedKeyLength;
        this.uvarintcode = HexUtils.hexToVarintHex(this.code);
    }
}
