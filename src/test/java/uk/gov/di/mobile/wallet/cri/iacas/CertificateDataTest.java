package uk.gov.di.mobile.wallet.cri.iacas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.security.auth.x500.X500Principal;

import java.security.cert.X509Certificate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateDataTest {

    @Mock private X509Certificate mockCertificate;

    @Test
    void Should_CreateCertificateDataWithAllFields() {
        String notAfter = "2025-12-31T23:59:59.999Z";
        String notBefore = "2024-01-01T00:00:00.000Z";
        String country = "UK";
        String commonName = "Test CA";

        CertificateData certData = new CertificateData(notAfter, notBefore, country, commonName);

        assertEquals(notAfter, certData.notAfter());
        assertEquals(notBefore, certData.notBefore());
        assertEquals(country, certData.country());
        assertEquals(commonName, certData.commonName());
    }

    @Test
    void Should_CreateFromValidX509Certificate() {
        Date notBefore = new Date(1704067200000L); // 2024-01-01T00:00:00Z
        Date notAfter = new Date(1767225599000L); // 2025-12-31T23:59:59Z
        X500Principal subject = new X500Principal("CN=Test CA, C=UK");
        when(mockCertificate.getNotBefore()).thenReturn(notBefore);
        when(mockCertificate.getNotAfter()).thenReturn(notAfter);
        when(mockCertificate.getSubjectX500Principal()).thenReturn(subject);

        CertificateData certData = CertificateData.fromCertificate(mockCertificate);

        assertEquals("UK", certData.country());
        assertEquals("Test CA", certData.commonName());
        assertEquals("2024-01-01T00:00:00.000Z", certData.notBefore());
        assertEquals("2025-12-31T23:59:59.000Z", certData.notAfter());
    }

    @Test
    void Should_ThrowException_When_CommonNameIsMissing() {
        X500Principal subjectWithoutCN = new X500Principal("C=UK");
        when(mockCertificate.getSubjectX500Principal()).thenReturn(subjectWithoutCN);
        when(mockCertificate.getNotBefore()).thenReturn(new Date());
        when(mockCertificate.getNotAfter()).thenReturn(new Date());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> CertificateData.fromCertificate(mockCertificate));

        assertEquals("Certificate missing required CN field", exception.getMessage());
    }

    @Test
    void Should_ThrowException_When_CountryIsMissing() {
        X500Principal subjectWithoutCN = new X500Principal("CN=Test CA");
        when(mockCertificate.getSubjectX500Principal()).thenReturn(subjectWithoutCN);
        when(mockCertificate.getNotBefore()).thenReturn(new Date());
        when(mockCertificate.getNotAfter()).thenReturn(new Date());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> CertificateData.fromCertificate(mockCertificate));

        assertEquals("Certificate missing required C field", exception.getMessage());
    }
}
