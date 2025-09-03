package uk.gov.di.mobile.wallet.cri.iacas;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.X509CertUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HexFormat;

/**
 * Represents an IACA (Issuing Authority Certificate Authority).
 *
 * <p>The IACA is an X.509 certificate that serves as the trust anchor (root or top-level CA) for a
 * specific mDoc issuer, and is used to sign Document Signer Certificates, which in turn sign Mobile
 * Security Objects (MSOs).
 *
 * @param id Unique identifier for the IACA.
 * @param active Indicates whether this IACA is currently active and valid for use.
 * @param certificatePem PEM-encoded X.509 certificate.
 * @param certificateData Metadata about the IACA.
 * @param certificateFingerprint The fingerprint (cryptographic hash) of the IACA.
 * @param publicKeyJwk The public key of the IACA in JWK format.
 */
public record Iaca(
        String id,
        boolean active,
        String certificatePem,
        CertificateData certificateData,
        String certificateFingerprint,
        PublicKeyJwk publicKeyJwk) {

    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;

    /**
     * Creates an {@code Iaca} instance from a PEM-encoded certificate.
     *
     * <p>This method parses the provided PEM certificate, extracts its metadata, computes its
     * SHA-256 fingerprint, and builds a JWK representation of the public key.
     *
     * @param id The unique identifier for the IACA.
     * @param active Whether the IACA is active.
     * @param certificatePem The PEM-encoded X.509 certificate.
     * @return A new {@code Iaca} instance.
     * @throws IllegalArgumentException If the input certificate string cannot be parsed as a {@code
     *     X509Certificate}.
     * @throws JOSEException If parsing the certificate and extracting the Elliptic Curve (EC) key
     *     from it fails.
     * @throws CertificateEncodingException If encoding fails.
     * @throws NoSuchAlgorithmException If SHA-256 is not available.
     */
    public static Iaca fromCertificate(String id, boolean active, String certificatePem)
            throws JOSEException, CertificateEncodingException, NoSuchAlgorithmException {
        X509Certificate certificate = X509CertUtils.parse(certificatePem);
        if (certificate == null) {
            throw new IllegalArgumentException(
                    "Failed to parse PEM certificate: parsing returned null");
        }

        CertificateData certificateData = CertificateData.fromCertificate(certificate);

        ECKey ecKey = ECKey.parse(certificate);
        PublicKeyJwk publicKeyJwk =
                new PublicKeyJwk(
                        ecKey.getKeyType().getValue(),
                        ecKey.getCurve().getName(),
                        ecKey.getX().toString(),
                        ecKey.getY().toString(),
                        SIGNING_ALGORITHM.toString());
        String fingerprint = getCertificateFingerprint(certificate);

        return new Iaca(id, active, certificatePem, certificateData, fingerprint, publicKeyJwk);
    }

    /**
     * Computes the SHA-256 fingerprint of the certificate.
     *
     * <p>The fingerprint is a cryptographic hash of the certificate's encoded form, represented as
     * a lowercase hexadecimal string.
     *
     * @param certificate The X.509 certificate.
     * @return The fingerprint as a lowercase hexadecimal string.
     * @throws NoSuchAlgorithmException If SHA-256 is not available.
     * @throws CertificateEncodingException If encoding fails.
     */
    private static String getCertificateFingerprint(X509Certificate certificate)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(certificate.getEncoded());
        return HexFormat.of().formatHex(messageDigest.digest());
    }
}
