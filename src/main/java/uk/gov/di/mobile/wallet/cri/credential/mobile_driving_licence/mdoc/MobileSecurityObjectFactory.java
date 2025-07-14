package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKeyBuilder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEKeyTypes;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.constants.DocumentTypes;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/** Factory class for creating MobileSecurityObject instances. */
public class MobileSecurityObjectFactory {

    /** The version for the {@link MobileSecurityObject}. */
    private static final String MSO_VERSION = "1.0";

    /**
     * The document type identifier for a mobile driver's license (mDL), as specified by ISO
     * 18013-5.
     */
    private static final String DOC_TYPE = DocumentTypes.MDL;

    /** The factory responsible for creating {@link ValueDigests} instances. */
    private final ValueDigestsFactory valueDigestsFactory;

    /** The source of current time for validity information. */
    private final Clock clock;

    /**
     * Constructs a new {@link MobileSecurityObjectFactory} with the provided {@link
     * ValueDigestsFactory}.
     *
     * @param valueDigestsFactory The factory used to create value digests for the {@link
     *     MobileSecurityObject}.
     */
    public MobileSecurityObjectFactory(ValueDigestsFactory valueDigestsFactory) {
        this(valueDigestsFactory, Clock.systemDefaultZone());
    }

    /**
     * Constructs a new {@link MobileSecurityObjectFactory} with the provided {@link
     * ValueDigestsFactory} and {@link Clock}.
     *
     * @param valueDigestsFactory The factory used to create value digests for the {@link
     *     MobileSecurityObject}.
     * @param clock The source of current time for validity information.
     */
    public MobileSecurityObjectFactory(ValueDigestsFactory valueDigestsFactory, Clock clock) {
        this.valueDigestsFactory = valueDigestsFactory;
        this.clock = clock;
    }

    /**
     * Builds a {@link MobileSecurityObject} instance from the provided namespaces.
     *
     * <p>This method generates the value digests for each namespace and constructs the {@link
     * MobileSecurityObject}.
     *
     * @param nameSpaces A map where the key is the namespace string and the value is a list of
     *     {@link IssuerSignedItem} objects belonging to that namespace. This map provides the data
     *     used to generate the value digests for the {@link MobileSecurityObject}.
     * @return The constructed {@link MobileSecurityObject} instance.
     * @throws MDLException If an error occurs during the creation of the {@link ValueDigests}.
     */
    public MobileSecurityObject build(Namespaces nameSpaces, ECPublicKey publicKey)
            throws MDLException {
        ValueDigests valueDigests = valueDigestsFactory.createFromNamespaces(nameSpaces);

        Instant currentTimestamp = clock.instant();
        Instant validUntil = currentTimestamp.plus(Duration.ofDays(365));
        var validityInfo = new ValidityInfo(currentTimestamp, currentTimestamp, validUntil);

        // Determine the curve is P-256
        ECParameterSpec params = publicKey.getParams();
        int curveSizeBits = params.getCurve().getField().getFieldSize();
        if (curveSizeBits != 256) {
            throw new IllegalArgumentException("Invalid key curve - expected P-256");
        }
        // Extract coordinates
        ECPoint point = publicKey.getW();
        BigInteger x = point.getAffineX();
        BigInteger y = point.getAffineY();

        // Convert to bytes
        byte[] xBytes = bigIntegerToFixedBytes(x, curveSizeBits);
        byte[] yBytes = bigIntegerToFixedBytes(y, curveSizeBits);

        COSEKey coseKey =
                new COSEKeyBuilder()
                        .keyType(COSEKeyTypes.EC2)
                        .curve(COSEEllipticCurves.P256)
                        .xCoordinate(xBytes)
                        .yCoordinate(yBytes)
                        .build();

        Set<String> keys = nameSpaces.asMap().keySet();
        AuthorizedNameSpaces authorizedNameSpaces = new AuthorizedNameSpaces(keys);
        KeyAuthorizations keyAuthorizations = new KeyAuthorizations(authorizedNameSpaces);

        return new MobileSecurityObject(
                MSO_VERSION,
                valueDigestsFactory.getDigestAlgorithm(),
                new DeviceKeyInfo(coseKey, keyAuthorizations),
                valueDigests,
                DOC_TYPE,
                validityInfo);
    }

    /**
     * Converts a BigInteger to a fixed-length byte array using truncation/padding approach.
     *
     * <p>This method preserves all leading zeros in the BigInteger representation and uses
     * truncation for oversized values (keeping the least significant bytes). This approach is
     * required because exact byte lengths are required in COSE keys.
     *
     * <p><strong>Handling scenarios:</strong>
     *
     * <ul>
     *   <li><strong>Exact match:</strong> Returns the array as-is
     *   <li><strong>Too long:</strong> Takes the rightmost bytes (truncates from left)
     *   <li><strong>Too short:</strong> Pads with leading zeros (big-endian format)
     * </ul>
     *
     * <p><strong>Example:</strong>
     *
     * <pre>{@code
     * // P-256 curve coordinate (32 bytes needed)
     * BigInteger coord = new BigInteger("AB12", 16);
     * byte[] result = bigIntegerToFixedBytes(coord, 256);
     * // Result: [0x00, 0x00, ..., 0xAB, 0x12] (32 bytes total)
     * }</pre>
     *
     * @param value The BigInteger to convert to a byte array
     * @param curveSizeBits The curve size in bits (will be converted to bytes using (bits + 7) / 8)
     * @return A byte array of exactly the calculated byte length representing the BigInteger value
     */
    private static byte[] bigIntegerToFixedBytes(BigInteger value, int curveSizeBits) {
        // Calculate the number of bytes needed for this curve size
        // The (curveSizeBits + 7) / 8 formula rounds up to the next byte boundary
        int targetBytes = (curveSizeBits + 7) / 8;

        // Create the result array of the exact size needed
        byte[] result = new byte[targetBytes];

        // Get the raw byte representation from BigInteger
        byte[] sourceBytes = value.toByteArray();

        if (result.length == sourceBytes.length) {
            // Match - return the BigInteger bytes
            return sourceBytes;
        } else if (sourceBytes.length > result.length) {
            // Source is too long - truncate by taking the rightmost bytes
            // This preserves the least significant bytes (big-endian format)
            System.arraycopy(
                    sourceBytes, sourceBytes.length - result.length, result, 0, result.length);
        } else {
            // Source is too short - pad with leading zeros
            // Copy source bytes to the right side of the result array
            System.arraycopy(
                    sourceBytes, 0, result, result.length - sourceBytes.length, sourceBytes.length);
            // The left side is automatically filled with zeros
        }
        return result;
    }
}
