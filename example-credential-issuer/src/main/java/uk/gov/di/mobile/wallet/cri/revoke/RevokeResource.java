package uk.gov.di.mobile.wallet.cri.revoke;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.responses.ResponseUtil;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/revoke")
public class RevokeResource {

    private final RevokeService revokeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeResource.class);

    public RevokeResource(RevokeService revokeService) {
        this.revokeService = revokeService;
    }

    @POST
    public Response revokeCredential(RevokeRequestBody body) throws DataStoreException {
        if (body == null) {
            return ResponseUtil.badRequest("Driving Licence Number not found");
        }
        try {
            revokeService.revokeCredential(body.getDrivingLicenceNumber());
            return ResponseUtil.accepted();
        } catch (Exception exception) {
            LOGGER.error("An Error happened getting driving licence number", exception);
            if (exception instanceof RevokeServiceException) {
                return ResponseUtil.notFound();
            }
            return ResponseUtil.internalServerError();
        }
    }
}
