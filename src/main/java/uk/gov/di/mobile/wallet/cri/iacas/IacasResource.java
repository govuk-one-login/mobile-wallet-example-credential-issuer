package uk.gov.di.mobile.wallet.cri.iacas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.nimbusds.jose.jwk.JWK;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.util.ResponseUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Singleton
@Path("/iacas")
public class IacasResource {

    private static final String IACAS_FILE_NAME = "iacas.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(IacasResource.class);

    public IacasResource() {}

    @GET
    public Response getIacas() {
        try {
            String pem =
                    "-----BEGIN CERTIFICATE-----\n"
                            + "MIIB+TCCAZ6gAwIBAgIRAMnlFFLquSAw0bZeocBfYNAwCgYIKoZIzj0EAwIwQTEL\n"
                            + "MAkGA1UEBhMCVUsxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIEJV\n"
                            + "SUxEIGVudmlyb25tZW50MB4XDTI1MDQxNTEyMzAzNFoXDTM0MDQxNjEzMzAzNFow\n"
                            + "QTELMAkGA1UEBhMCVUsxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAt\n"
                            + "IEJVSUxEIGVudmlyb25tZW50MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEmesd\n"
                            + "UgEV3ug3ypAOfxpPmMpufhhyY0gU4GSYyqCNtzSesUrW4rKg1MAF+3ms9abxJi3W\n"
                            + "Ug93+9lgoyjwIlIc3qN3MHUwEgYDVR0TAQH/BAgwBgEB/wIBADAdBgNVHQ4EFgQU\n"
                            + "tVaLskxvqtFP0jzJzfGdhUXWOXkwDwYDVR0PAQH/BAUDAwcGADAvBgNVHRIEKDAm\n"
                            + "hiRodHRwczovL21vYmlsZS5idWlsZC5hY2NvdW50Lmdvdi51ay8wCgYIKoZIzj0E\n"
                            + "AwIDSQAwRgIhAIRqET364AqFM+EeVglL6ger9zYxRW8vsRhrOSKBt5ihAiEAmUBt\n"
                            + "tXh7AiJCnt2dUJzmCX8VpI5cBB4WqIp91MtyhV8=\n"
                            + "-----END CERTIFICATE-----";

            JWK jwk = JWK.parseFromPEMEncodedObjects(pem);
            System.out.println("JWK: " + jwk.toJSONObject());

            String fingerprint = getFingerprintFromPem(pem);
            System.out.println("Certificate fingerprint: " + fingerprint);

            Object iacas = loadIacas(IACAS_FILE_NAME);
            return ResponseUtil.ok(iacas);
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to get the IACAs: ", exception);
            return ResponseUtil.internalServerError();
        }
    }

    public static X509Certificate pemToX509Certificate(String pemString)
            throws CertificateException {
        // Remove PEM header and footer, and whitespace
        String pem =
                pemString
                        .replace("-----BEGIN CERTIFICATE-----", "")
                        .replace("-----END CERTIFICATE-----", "")
                        .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(pem);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(encoded));
    }

    public static String getFingerprintFromPem(String pemString)
            throws CertificateException, NoSuchAlgorithmException {
        X509Certificate cert = pemToX509Certificate(pemString);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(cert.getEncoded());
        byte[] digest = md.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    //    public static String getFingerprint(String filePath) throws Exception {
    //        CertificateFactory cf = CertificateFactory.getInstance("X.509");
    //        X509Certificate cert = (X509Certificate) cf.generateCertificate(new
    // FileInputStream(filePath));
    //        MessageDigest md = MessageDigest.getInstance("SHA-256");
    //        md.update(cert.getEncoded());
    //        byte[] digest = md.digest();
    //        StringBuilder hexString = new StringBuilder();
    //        for (byte b : digest) {
    //            hexString.append(String.format("%02x", b));
    //        }
    //        return hexString.toString();
    //    }

    public static Object loadIacas(String fileName) throws IOException, IllegalArgumentException {
        File iacasFilePath = new File(Resources.getResource(fileName).getPath());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(iacasFilePath, Object.class);
    }
}
