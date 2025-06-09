package uk.gov.di.mobile.wallet.cri.iacas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.util.ResponseUtil;

import java.io.File;
import java.io.IOException;

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

    /**
     * Constructs the resource with the required configuration service.
     *
     * @param configurationService Service for resolving the current environment.
     */
    public IacasResource(ConfigurationService configurationService) {
        this.configurationService = configurationService;
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
            String environment = configurationService.getEnvironment();
            String fileName = String.format("iacas-%s.json", environment);
            Iacas iacas = loadIacas(fileName);
            return ResponseUtil.ok(iacas);
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to get the IACAs: ", exception);
            return ResponseUtil.internalServerError();
        }
    }

    /**
     * Loads the IACAs for the given file name.
     *
     * <p>If the file is not found or cannot be read, throws an exception.
     *
     * @param fileName The expected JSON filename for the current environment.
     * @return The deserialized {@link Iacas} object.
     * @throws IOException If reading the file fails.
     * @throws IllegalArgumentException If the resource file is not found or cannot be read.
     */
    public static Iacas loadIacas(String fileName) throws IOException {
        File iacasFile = new File(Resources.getResource(fileName).getPath());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(iacasFile, Iacas.class);
    }
}
