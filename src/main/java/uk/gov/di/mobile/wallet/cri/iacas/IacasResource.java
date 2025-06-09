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
 * <p>Typical usage:
 *
 * <ul>
 *   <li>GET /iacas — Returns the IACAs for the current environment.
 * </ul>
 *
 * *
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
     * and returns its contents.
     *
     * @return HTTP 200 response with the IACAs, or HTTP 500 on error.
     */
    @GET
    public Response getIacas() {
        try {
            String environment = configurationService.getEnvironment();
            String fileName = String.format("iacas-%s.json", environment);
            Iacas iacas = loadIacasWithFallback(fileName, environment);
            return ResponseUtil.ok(iacas);
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to get the IACAs: ", exception);
            return ResponseUtil.internalServerError();
        }
    }

    /**
     * Loads the IACAs for the given environment, with fallback logic.
     *
     * <p>If the environment-specific configuration file is not found, and the environment is
     * "staging", this method falls back to "iacas-dev.json". For other environments, the exception
     * is propagated.
     *
     * @param fileName The expected JSON filename for the current environment.
     * @param environment The current environment name.
     * @return The deserialized {@link Iacas} configuration object.
     * @throws IOException if reading the configuration file fails.
     */
    public static Iacas loadIacasWithFallback(String fileName, String environment)
            throws IOException {
        File iacasFile;
        try {
            iacasFile = new File(Resources.getResource(fileName).getPath());
        } catch (IllegalArgumentException exception) {
            if ("staging".equals(environment)) {
                String fallbackFile = "iacas-dev.json";
                LOGGER.warn(
                        "Config file '{}' not found for environment '{}'. Falling back to '{}'.",
                        fileName,
                        environment,
                        fallbackFile);
                iacasFile = new File(Resources.getResource(fallbackFile).getPath());
            } else {
                throw exception;
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(iacasFile, Iacas.class);
    }
}
