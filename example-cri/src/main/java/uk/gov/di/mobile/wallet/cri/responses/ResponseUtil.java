package uk.gov.di.mobile.wallet.cri.responses;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ResponseUtil {

    public static final String NO_STORE = "no-store";

    private ResponseUtil() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static Response ok(Object entity) {
        return ok(entity, false);
    }

    public static Response ok(Object entity, boolean cacheable) {
        Response.ResponseBuilder builder = jsonBuilder(Response.Status.OK, entity);
        if (!cacheable) {
            builder.header(HttpHeaders.CACHE_CONTROL, NO_STORE);
        }
        return builder.build();
    }

    public static Response noContent() {
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    public static Response badRequest(String errorMessage) {
        return jsonBuilder(Response.Status.BAD_REQUEST, new ErrorResponse(errorMessage))
                .header(HttpHeaders.CACHE_CONTROL, NO_STORE)
                .build();
    }

    public static Response unauthorized() {
        return unauthorized(null);
    }

    public static Response unauthorized(String error) {
        String headerValue = "Bearer" + (error == null ? "" : " error=\"" + error + "\"");
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, headerValue)
                .header(HttpHeaders.CACHE_CONTROL, NO_STORE)
                .build();
    }

    public static Response internalServerError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CACHE_CONTROL, NO_STORE)
                .build();
    }

    private static Response.ResponseBuilder jsonBuilder(Response.Status status, Object entity) {
        return Response.status(status)
                .entity(entity)
                .type(MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8"));
    }
}
