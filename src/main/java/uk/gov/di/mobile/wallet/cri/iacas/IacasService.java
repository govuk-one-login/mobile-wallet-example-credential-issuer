package uk.gov.di.mobile.wallet.cri.iacas;

import com.nimbusds.jose.jwk.ECKey;
import jakarta.xml.bind.DatatypeConverter;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static uk.gov.di.mobile.wallet.cri.util.ArnUtil.extractCertificateId;

public class IacasService {

  private final ConfigurationService configurationService;
  private final ObjectStore objectStore;

  public IacasService(ConfigurationService configurationService, ObjectStore objectStore) {
    this.configurationService = configurationService;
    this.objectStore = objectStore;
  }

  public String getCertificateDetails() throws Exception {
    String bucketName = configurationService.getCertificatesBucketName();
    String certificateAuthorityArn = configurationService.getCertificateAuthorityArn();

    String rootCertificateId = extractCertificateId(certificateAuthorityArn);

    String objectKey = rootCertificateId + "/certificate.pem";

    byte[] pemBytes = objectStore.getObject(bucketName, objectKey);
    String pem = new String(pemBytes, StandardCharsets.UTF_8);

    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    ByteArrayInputStream is = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
    X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
//
//    Iaca iaca = Iaca.fromCertificate(rootCertificateId,true, pem);
//    List<Iaca> iacaList = new ArrayList<>();
//    iacaList.add(iaca);
//
//    Iacas iacas = Iacas.fromList(iacaList);

    return "Subject: " + cert.getSubjectX500Principal() + "\n" +
            "Issuer: " + cert.getIssuerX500Principal() + "\n" +
            "Valid From: " + cert.getNotBefore() + "\n" +
            "Valid To: " + cert.getNotAfter() + "\n" +
            "Public Key: " + cert.getPublicKey() + "\n" +
            "Thumbprint: " + getThumbprint(cert) + "\n" +
            "Signature Algorithm: " + cert.getSigAlgName() + "\n";

  }


  private static String getThumbprint(X509Certificate cert)
          throws NoSuchAlgorithmException, CertificateEncodingException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    md.update(cert.getEncoded());
    return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
  }
}