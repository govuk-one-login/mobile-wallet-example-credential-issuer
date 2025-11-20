package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEKeyTypes;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;

import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.BigIntegerToFixedBytes.bigIntegerToFixedBytes;

/**
 * Factory for creating {@link COSEKey} instance from an EC public key.
 *
 * <p>This factory encapsulates the logic for converting a Java {@link ECPublicKey} into a COSE_Key
 * (CBOR Object Signing and Encryption key) format, as required for mobile security objects.
 */
public class COSEKeyFactory {

    /**
     * Creates a {@link COSEKey} from the given EC public key.
     *
     * <p>This method validates that the provided key uses the P-256 curve, extracts the x and y
     * coordinates, and encodes them as fixed-length byte arrays to construct the {@link COSEKey}
     * object.
     *
     * @param publicKey the EC public key to convert.
     * @return The {@link COSEKey} representation of the public key.
     * @throws IllegalArgumentException If the key is not a valid P-256 (secp256r1) key.
     */
    public COSEKey fromECPublicKey(ECPublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey must not be null");
        }

        ECParameterSpec params = publicKey.getParams();
        int curveSizeBits = params.getCurve().getField().getFieldSize();
        if (curveSizeBits != 256) {
            throw new IllegalArgumentException(
                    "Invalid EC key curve: expected P-256 (secp256r1), got field size "
                            + curveSizeBits
                            + " bits");
        }

        ECPoint point = publicKey.getW();
        if (point == null || point.getAffineX() == null || point.getAffineY() == null) {
            throw new IllegalArgumentException("Invalid EC public key point: missing coordinates");
        }
        BigInteger x = point.getAffineX();
        BigInteger y = point.getAffineY();

        // Convert BigInteger to a fixed-length byte array
        byte[] xBytes = bigIntegerToFixedBytes(x, curveSizeBits);
        byte[] yBytes = bigIntegerToFixedBytes(y, curveSizeBits);

        return new COSEKey(COSEKeyTypes.EC2, COSEEllipticCurves.P256, xBytes, yBytes);
    }
}
