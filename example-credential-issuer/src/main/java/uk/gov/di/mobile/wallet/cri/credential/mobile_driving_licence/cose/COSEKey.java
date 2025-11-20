package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

/**
 * Represents a COSE_Key structure as defined in RFC 8152, used for encoding public keys in Mobile
 * Driving License (mDL).
 *
 * <p>See: <a href="https://www.rfc-editor.org/rfc/rfc8152.html#section-7">RFC 8152, Section 7</a>
 */
public record COSEKey(int keyType, int curve, byte[] x, byte[] y) {}
