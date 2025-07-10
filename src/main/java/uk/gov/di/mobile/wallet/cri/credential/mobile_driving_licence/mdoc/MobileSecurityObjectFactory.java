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
        int keySize = getKeySize(params);

        // Extract coordinates
        ECPoint point = publicKey.getW();
        BigInteger x = point.getAffineX();
        BigInteger y = point.getAffineY();

//        // Convert to byte arrays
//        byte[] xBytes =
//        byte[] yBytes =

        COSEKey coseKey = new COSEKeyBuilder().keyType(COSEKeyTypes.EC2).curve(COSEEllipticCurves.P256).build();

        return new MobileSecurityObject(
                MSO_VERSION,
                valueDigestsFactory.getDigestAlgorithm(),
                valueDigests,
                DOC_TYPE,
                validityInfo);
    }

    private static int getKeySize(ECParameterSpec params) {
        int fieldSize = params.getCurve().getField().getFieldSize();

        switch (fieldSize) {
            case 256:
                return 32; // 256 bits = 32 bytes
            default:
                throw new IllegalArgumentException("Unsupported curve with field size: " + fieldSize);
        }
    }
}
