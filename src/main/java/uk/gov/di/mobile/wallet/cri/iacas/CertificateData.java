package uk.gov.di.mobile.wallet.cri.iacas;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Represents information extracted from an X.509 certificate.
 *
 * <p>This record holds the certificate's validity period and issuer details.
 *
 * @param notAfter The date and time (ISO 8601 format) when the certificate expires).
 * @param notBefore The date and time (ISO 8601 format) when the certificate becomes valid.
 * @param country The country code (ISO 3166-1 alpha-2) of the IACA's issuer country.
 * @param commonName The IACA's issuer common name (CN).
 */
public record CertificateData(
        String notAfter, String notBefore, String country, String commonName) {

    /**
     * Creates a {@code CertificateData} instance from an {@link X509Certificate}.
     *
     * @param certificate The X.509 certificate to extract data from.
     * @return A new {@code CertificateData} instance.
     * @throws IllegalArgumentException If required fields CN (Common Name) and C (Country) are
     *     missing.
     */
    public static CertificateData fromCertificate(X509Certificate certificate) {
        String isoNotBefore = getIsoDate(certificate.getNotBefore());
        String isoNotAfter = getIsoDate(certificate.getNotAfter());

        X500Name x500Name = new X500Name(certificate.getSubjectX500Principal().getName());
        String commonName = extractValue(x500Name, BCStyle.CN);
        String country = extractValue(x500Name, BCStyle.C);

        if (commonName == null) {
            throw new IllegalArgumentException("Certificate missing required CN field");
        }
        if (country == null) {
            throw new IllegalArgumentException("Certificate missing required C field");
        }

        return new CertificateData(isoNotAfter, isoNotBefore, country, commonName);
    }

    /**
     * Converts a {@link Date} to an ISO 8601 formatted string in UTC (e.g.,
     * "2025-06-18T00:00:00.000Z").
     *
     * @param date The date to format.
     * @return The ISO 8601 formatted date string.
     */
    private static String getIsoDate(Date date) {
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC);

        Instant instant = date.toInstant();
        return dateTimeFormatter.format(instant);
    }

    /**
     * Extracts the value for a given attribute (e.g., CN or C) from X.509 certificate’s Subject
     * Distinguished Name (DN).
     *
     * @param x500Name The {@code X500Name} object representing the distinguished name.
     * @param objectId The {@code ASN1ObjectIdentifier} for the attribute to extract.
     * @return The string value of the attribute, or {@code null} if not present.
     */
    private static String extractValue(X500Name x500Name, ASN1ObjectIdentifier objectId) {
        RDN[] rdns = x500Name.getRDNs(objectId);
        if (rdns != null && rdns.length > 0 && rdns[0].getFirst() != null) {
            return rdns[0].getFirst().getValue().toString();
        }
        return null;
    }
}
