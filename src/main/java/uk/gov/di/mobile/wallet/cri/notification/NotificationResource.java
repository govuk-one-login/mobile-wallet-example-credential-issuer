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
import uk.gov.di.mobile.wallet.cri.credential.*;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.util.ResponseUtil;

import java.util.UUID;
import java.util.stream.Stream;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/notification")
public class NotificationResource {

    private final ConfigurationService configurationService;
    private final NotificationService notificationService;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationResource.class);

    public NotificationResource(
            NotificationService notificationService, ConfigurationService configurationService) {
        this.notificationService = notificationService;
        this.configurationService = configurationService;
    }

    @POST
    public Response processNotification(
            @HeaderParam("Authorization") String authorizationHeader, String payload) {
        try {
            SignedJWT accessToken = parseAuthorizationHeader(authorizationHeader);
            NotificationRequestBody notificationRequestBody = parseRequestBody(payload);

        } catch (Exception exception) {
            LOGGER.error(
                    "An error happened trying to process the notification request: ", exception);
            if (exception instanceof AccessTokenValidationException) {
                return ResponseUtil.unauthorized();
            }

            if (exception instanceof RequestBodyValidationException) {
                return ResponseUtil.badRequest(error(exception.getMessage()));
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

    private NotificationRequestBody parseRequestBody(String payload)
            throws RequestBodyValidationException {
        ObjectMapper mapper =
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        NotificationRequestBody requestBody;
        try {
            requestBody = mapper.readValue(payload, NotificationRequestBody.class);
        } catch (JsonProcessingException exception) {
            throw new RequestBodyValidationException("invalid_notification_request", exception);
        }

        if (requestBody.getNotificationId() == null) {
            LOGGER.error("notification_id not in request body");
            throw new RequestBodyValidationException("invalid_notification_id");
        }

        try {
            UUID.fromString(requestBody.getNotificationId());
        } catch (IllegalArgumentException exception) {
            LOGGER.error("notification_id is not a valid UUID");
            throw new RequestBodyValidationException("invalid_notification_id");
        }

        if (requestBody.getEvent() == null) {
            LOGGER.error("event not in request body");
            throw new RequestBodyValidationException("invalid_notification_request");
        }

        if (Stream.of("credential_accepted", "credential_failure", "credential_deleted")
                .noneMatch(valid -> valid.equals(requestBody.getEvent()))) {
            LOGGER.error(
                    "event is not valid: must be one of 'credential_accepted', 'credential_failure', 'credential_deleted'");
            throw new RequestBodyValidationException("invalid_notification_request");
        }

        if (requestBody.getEventDescription() != null
                && !requestBody.getEventDescription().matches("\\A\\p{ASCII}*\\z")) {
            LOGGER.error("event_description is invalid: must contain only ASCII characters");
            throw new RequestBodyValidationException("invalid_notification_request");
        }

        return requestBody;
    }

    private String error(String error) {
        return String.format("{\"error\":\"%s\"}", error);
    }
}
