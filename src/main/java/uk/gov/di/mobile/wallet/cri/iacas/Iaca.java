package uk.gov.di.mobile.wallet.cri.iacas;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.X509CertUtils;
import lombok.Getter;
import lombok.Setter;

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
 */
@Getter
@Setter
public class Iaca {
    /** Unique identifier for the IACA. */
    private String id;

    /** Indicates whether this IACA is currently active and valid for use. */
    private boolean active;

    /** PEM-encoded X.509 certificate. */
    private String certificatePem;

    /** Metadata about the IACA. */
    private CertificateData certificateData;

    /** The fingerprint (cryptographic hash) of the IACA. */
    private String certificateFingerprint;

    /** The public key of the IACA in JWK (JSON Web Key) format. */
    private PublicKeyJwk publicKeyJwk;

    public Iaca(
            String id,
            boolean active,
            String certificatePem,
            CertificateData certificateData,
            String certificateFingerprint,
            PublicKeyJwk publicKeyJwk) {
        this.id = id;
        this.active = active;
        this.certificatePem = certificatePem;
        this.certificateData = certificateData;
        this.certificateFingerprint = certificateFingerprint;
        this.publicKeyJwk = publicKeyJwk;
    }

    /**
     * Creates an {@code Iaca} instance from a PEM-encoded certificate.
     *
     * @param id The unique identifier for the IACA.
     * @param active Whether the IACA is active.
     * @param certificatePem The PEM-encoded X.509 certificate.
     * @return A new {@code Iaca} instance.
     * @throws IllegalArgumentException If the certificate cannot be parsed.
     * @throws JOSEException If parsing the certificate and extracting the Elliptic Curve (EC) key
     *     from it fails.
     * @throws CertificateEncodingException If encoding fails.
     * @throws NoSuchAlgorithmException If SHA-256 is not available.
     */
    public static Iaca fromCertificate(String id, boolean active, String certificatePem)
            throws JOSEException, CertificateEncodingException, NoSuchAlgorithmException {
        X509Certificate certificate = X509CertUtils.parse(certificatePem);
        if (certificate == null) {
            throw new IllegalArgumentException("Failed to parse PEM certificate");
        }
        CertificateData certificateData = CertificateData.fromCertificate(certificate);
        ECKey ecKey = ECKey.parse(certificate);
        PublicKeyJwk publicKeyJwk =
                new PublicKeyJwk(
                        ecKey.getKeyType().getValue(),
                        ecKey.getCurve().getName(),
                        ecKey.getX().decodeToString(),
                        ecKey.getY().decodeToString());
        String fingerprint = getCertificateFingerprint(certificate);

        return new Iaca(id, active, certificatePem, certificateData, fingerprint, publicKeyJwk);
    }

    /**
     * Computes the SHA-256 fingerprint of the certificate.
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
