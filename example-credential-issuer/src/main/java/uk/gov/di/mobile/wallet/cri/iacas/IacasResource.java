package uk.gov.di.mobile.wallet.cri.iacas;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.responses.ResponseUtil;

/**
 * JAX-RS resource class for serving IACA (Issuing Authority Certificate Authority) certificates.
 *
 * <p>This resource exposes the "/iacas" endpoint, allowing clients to retrieve the list of IACAs.
 *
 * @see Iacas
 */
@Singleton
@Path("/iacas")
public class IacasResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(IacasResource.class);
    private final IacasService iacasService;

    public IacasResource(IacasService iacasService) {
        this.iacasService = iacasService;
    }

    @GET
    public Response getIacas() {
        try {
            Iacas iacas = iacasService.getIacas();
            return ResponseUtil.ok(iacas);
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to generate the IACAs: ", exception);
            return ResponseUtil.internalServerError();
        }
    }
}
