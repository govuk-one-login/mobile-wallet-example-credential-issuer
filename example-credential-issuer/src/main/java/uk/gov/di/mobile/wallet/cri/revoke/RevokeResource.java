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
    public Response revokeCredential(RevokeRequestBody requestBody) {
        if (requestBody == null) {
            return ResponseUtil.badRequest("Request body is required");
        }

        if (requestBody.getDrivingLicenceNumber() == null
                || requestBody.getDrivingLicenceNumber().isBlank()) {
            return ResponseUtil.badRequest("drivingLicenceNumber is required");
        }

        try {
            revokeService.revokeCredential(requestBody.getDrivingLicenceNumber());
            return ResponseUtil.accepted();
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to revoke credential(s): ", exception);
            if (exception instanceof CredentialNotFoundException) {
                return ResponseUtil.notFound();
            }
            return ResponseUtil.internalServerError();
        }
    }
}
