package uk.gov.di.mobile.wallet.cri.iacas;

import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;

import java.security.cert.X509Certificate;
import java.time.Instant;

/** Contains metadata about the IACA. */
@Getter
@Setter
public class CertificateData {
    /** The date and time when the certificate expires. */
    private Instant notAfter;

    /** The date and time when the certificate becomes valid. */
    private Instant notBefore;

    /** The country code (ISO 3166-1 alpha-2) of the IACA’s issuer country. */
    private String country;

    /** The IACA's common name. */
    private String commonName;

    public CertificateData(Instant notAfter, Instant notBefore, String country, String commonName) {
        this.notAfter = notAfter;
        this.notBefore = notBefore;
        this.country = country;
        this.commonName = commonName;
    }

    /**
     * Creates a {@code CertificateData} instance from an {@link X509Certificate}.
     *
     * @param certificate The X509 certificate.
     * @return A new {@code CertificateData} instance.
     * @throws IllegalArgumentException If required fields are missing.
     */
    public static CertificateData fromCertificate(X509Certificate certificate)
            throws IllegalArgumentException {
        Instant notAfter = certificate.getNotAfter().toInstant();
        Instant notBefore = certificate.getNotBefore().toInstant();

        X500Name x500Name = new X500Name(certificate.getSubjectX500Principal().getName());
        String commonName = extractFirstValue(x500Name, BCStyle.CN);
        String country = extractFirstValue(x500Name, BCStyle.C);
        if (commonName == null) {
            throw new IllegalArgumentException("Certificate missing required CN field");
        }
        if (country == null) {
            throw new IllegalArgumentException("Certificate missing required C field");
        }

        return new CertificateData(notAfter, notBefore, country, commonName);
    }

    private static String extractFirstValue(X500Name x500Name, ASN1ObjectIdentifier objectId) {
        RDN[] rdns = x500Name.getRDNs(objectId);
        if (rdns != null && rdns.length > 0 && rdns[0].getFirst() != null) {
            return rdns[0].getFirst().getValue().toString();
        }
        return null;
    }
}
