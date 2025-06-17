package uk.gov.di.mobile.wallet.cri.iacas;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.util.ResponseUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static uk.gov.di.mobile.wallet.cri.util.ArnUtil.extractCertificateId;

/**
 * JAX-RS resource class for serving IACA (Issuing Authority Certificate Authority) certificates.
 *
 * <p>This resource exposes the "/iacas" endpoint, allowing clients to retrieve the list of IACAs
 * for the current environment.
 *
 * <p>The IACAs is loaded from a JSON file named according to the active environment (e.g., <code>
 * iacas-dev.json</code>, <code>iacas-build.json</code>). If the environment-specific file is
 * missing or cannot be read, the resource returns an HTTP 500 Internal Server Error.
 *
 * @see Iacas
 */
@Singleton
@Path("/iacas")
public class IacasResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(IacasResource.class);
    private final ConfigurationService configurationService;
    private final ObjectStore objectStore;

    /**
     * Constructs the resource with the required configuration service.
     *
     * @param configurationService Service for resolving the current environment.
     */
    public IacasResource(ConfigurationService configurationService, ObjectStore objectStore) {
        this.configurationService = configurationService;
        this.objectStore = objectStore;
    }

    /**
     * Handles HTTP GET requests for the IACAs.
     *
     * <p>Determines the current environment, attempts to load the corresponding IACAs JSON file,
     * and returns its contents. If the environment-specific file does not exist or cannot be read,
     * returns HTTP 500.
     *
     * @return HTTP 200 response with the IACAs, or HTTP 500 on error.
     */
    @GET
    public Response getIacas() {
        try {
            String bucketName = configurationService.getCertificatesBucketName();
            String certificateAuthorityArn = configurationService.getCertificateAuthorityArn();

            String rootCertificateId = extractCertificateId(certificateAuthorityArn);

            byte[] pemBytes = objectStore.getObject(bucketName, rootCertificateId);
            String pem = new String(pemBytes, StandardCharsets.UTF_8);

            System.out.println(pem);

            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream is =
                    new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
            X509Certificate cert = (X509Certificate) factory.generateCertificate(is);

          String string = "Subject: " + cert.getSubjectX500Principal() + "\n" +
                  "Issuer: " + cert.getIssuerX500Principal() + "\n" +
                  "Valid From: " + cert.getNotBefore() + "\n" +
                  "Valid To: " + cert.getNotAfter() + "\n" +
                  "Public Key: " + cert.getPublicKey() + "\n" +
                  "Signature Algorithm: " + cert.getSigAlgName() + "\n";

            return ResponseUtil.ok(string);
        } catch (ObjectStoreException exception) {
            LOGGER.error("Error fetching certificate from S3: ", exception);
            return ResponseUtil.internalServerError();
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to parse the IACAs certificate: ", exception);
            return ResponseUtil.internalServerError();
        }
    }
}
