package uk.gov.di.mobile.wallet.cri.util;

import software.amazon.awssdk.arns.Arn;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;

/** Utility class for extracting resource IDs from AWS ARNs. */
public class ArnUtil {

    @ExcludeFromGeneratedCoverageReport
    private ArnUtil() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    /**
     * Extracts the certificate authority ID (CA ID) from an ACM Private CA certificate authority
     * ARN.
     *
     * <p>Example ARN format:
     *
     * <pre>
     * arn:aws:acm-pca:region:account-id:certificate-authority/ca-id
     * </pre>
     *
     * This method returns the second segment of the resource part, which is the certificate
     * authority ID.
     *
     * @param arnString The ACM PCA certificate authority ARN.
     * @return The certificate authority ID (CA ID).
     * @throws IllegalArgumentException If the ARN is malformed or does not contain a certificate
     *     authority ID.
     */
    public static String extractCertificateAuthorityId(String arnString) {
        Arn arn = Arn.fromString(arnString);
        String resource = arn.resourceAsString();
        String[] parts = resource.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                    "Invalid ACM PCA certificate authority ARN: " + arnString);
        }
        return parts[1];
    }

    /**
     * Extracts the key ID from an AWS KMS key ARN.
     *
     * <p>Example ARN format:
     *
     * <pre>
     * arn:aws:kms:region:account-id:key/key-id
     * </pre>
     *
     * This method returns the key ID portion of the ARN.
     *
     * @param arnString The KMS key ARN.
     * @return The key ID.
     * @throws IllegalArgumentException If the ARN is malformed or does not contain a key ID.
     */
    public static String extractKeyId(String arnString) {
        Arn arn = Arn.fromString(arnString);
        String keyId = arn.resource().resource();
        if (keyId == null || keyId.isEmpty()) {
            throw new IllegalArgumentException("Invalid KMS key ARN: " + arnString);
        }
        return keyId;
    }
}
