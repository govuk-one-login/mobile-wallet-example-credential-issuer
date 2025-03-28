package uk.gov.di.mobile.wallet.cri.util;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ResponseUtil {

    private ResponseUtil() {
        // Should never be instantiated
    }

    public static Response ok(Object entity) {
        return jsonBuilder(Response.Status.OK, entity).build();
    }

    public static Response noContent() {
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    public static Response badRequest(String entity) {
        return jsonBuilder(Response.Status.BAD_REQUEST, entity).build();
    }

    public static Response unauthorized(String entity) {
        return jsonBuilder(Response.Status.UNAUTHORIZED, entity).build();
    }

    public static Response internalServerError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    private static Response.ResponseBuilder jsonBuilder(Response.Status status, Object entity) {
        return Response.status(status)
                .entity(entity)
                .type(MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8"));
    }
}
