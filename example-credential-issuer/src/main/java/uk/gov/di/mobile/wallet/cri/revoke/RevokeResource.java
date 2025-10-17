package uk.gov.di.mobile.wallet.cri.revoke;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.responses.ResponseUtil;

@Produces(MediaType.APPLICATION_JSON)
@Path("/revoke")
public class RevokeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeResource.class);
    private static final String DOCUMENT_ID_PATTERN = "^[a-zA-Z0-9]{5,25}$";

    private final RevokeService revokeService;

    public RevokeResource(RevokeService revokeService) {
        this.revokeService = revokeService;
    }

    @POST
    @Path("/{documentId}")
    public Response revokeCredential(
            @PathParam("documentId") @NotEmpty @Pattern(regexp = DOCUMENT_ID_PATTERN)
                    String documentId) {
        try {
            revokeService.revokeCredential(documentId);
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
