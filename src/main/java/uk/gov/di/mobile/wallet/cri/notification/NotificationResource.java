package uk.gov.di.mobile.wallet.cri.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.util.ResponseUtil;

import java.util.UUID;
import java.util.stream.Stream;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/notification")
public class NotificationResource {

    private final NotificationService notificationService;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationResource.class);

    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @POST
    public Response processNotification(
            @HeaderParam("Authorization") String authorizationHeader, String payload) {
        try {
            SignedJWT accessToken = parseAuthorizationHeader(authorizationHeader);
            NotificationRequestBody notificationRequestBody = parseRequestBody(payload);

            notificationService.processNotification(accessToken, notificationRequestBody);
        } catch (Exception exception) {
            LOGGER.error(
                    "An error happened trying to process the notification request: ", exception);
            if (exception instanceof AccessTokenValidationException) {
                return ResponseUtil.unauthorized(error("invalid_token"));
            }

            if (exception instanceof InvalidNotificationIdException) {
                return ResponseUtil.badRequest(error("invalid_notification_id"));
            }

            if (exception instanceof InvalidNotificationRequestException) {
                return ResponseUtil.badRequest(error("invalid_notification_request"));
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
                    "Failed to parse authorization header", exception);
        }
    }

    private NotificationRequestBody parseRequestBody(String payload)
            throws InvalidNotificationIdException, InvalidNotificationRequestException {
        ObjectMapper mapper =
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        NotificationRequestBody requestBody;
        try {
            requestBody = mapper.readValue(payload, NotificationRequestBody.class);
        } catch (JsonProcessingException exception) {
            throw new InvalidNotificationRequestException(
                    "Failed to parse request body", exception);
        }

        if (requestBody.getNotificationId() == null) {
            throw new InvalidNotificationIdException("Missing property notification_id");
        }

        try {
            UUID.fromString(requestBody.getNotificationId());
        } catch (IllegalArgumentException exception) {
            throw new InvalidNotificationIdException("Invalid notification_id: must be UUID");
        }

        if (requestBody.getEvent() == null) {
            throw new InvalidNotificationRequestException("Missing property event");
        }

        if (Stream.of("credential_accepted", "credential_failure", "credential_deleted")
                .noneMatch(valid -> valid.equals(requestBody.getEvent()))) {

            throw new InvalidNotificationRequestException(
                    "Invalid event: must be one of 'credential_accepted', 'credential_failure', 'credential_deleted'");
        }

        if (requestBody.getEventDescription() != null
                && !requestBody.getEventDescription().matches("\\A\\p{ASCII}*\\z")) {
            throw new InvalidNotificationRequestException(
                    "Invalid event_description: must contain only ASCII characters");
        }

        return requestBody;
    }

    private String error(String error) {
        return String.format("{\"error\":\"%s\"}", error);
    }
}
