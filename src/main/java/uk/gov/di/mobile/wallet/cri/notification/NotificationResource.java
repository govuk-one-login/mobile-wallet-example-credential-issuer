package uk.gov.di.mobile.wallet.cri.notification;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.*;
import uk.gov.di.mobile.wallet.cri.util.ResponseUtil;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/notification")
public class NotificationResource {

    private final NotificationService notificationService;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationResource.class);

    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @POST
    public Response getCredential(
            @HeaderParam("Authorization") String authorizationHeader, String payload) {

        try {
            SignedJWT accessToken = parseAuthorizationHeader(authorizationHeader);



        } catch (Exception exception) {
            LOGGER.error("An error happened trying to process the notification request: ", exception);
            if (exception instanceof AccessTokenValidationException) {
                return ResponseUtil.unauthorized();
            }

            return ResponseUtil.internalServerError();
        }

        return ResponseUtil.noContent();
    }

    private SignedJWT parseAuthorizationHeader(String authorizationHeader)
            throws AccessTokenValidationException {
        try {
            BearerAccessToken bearerAccessToken = BearerAccessToken.parse(authorizationHeader);
            return SignedJWT.parse(bearerAccessToken.getValue());
        } catch (ParseException | java.text.ParseException exception) {
            throw new AccessTokenValidationException(
                    "Failed to parse authorization header as Signed JWT: ", exception);
        }
    }


}
