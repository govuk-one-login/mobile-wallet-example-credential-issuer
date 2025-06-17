package uk.gov.di.mobile.wallet.cri.util;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ResponseUtil {

    private ResponseUtil() {
        // Should never be instantiated
    }

    public static Response ok(Object entity) {
        return jsonBuilder(Response.Status.OK, entity).build();
    }

    public static Response ok(Object entity, String cacheControlValue) {
        return jsonBuilder(Response.Status.OK, entity)
                .header(HttpHeaders.CACHE_CONTROL, cacheControlValue)
                .build();
    }

    public static Response noContent() {
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    public static Response badRequest(String entity) {
        return jsonBuilder(Response.Status.BAD_REQUEST, entity).build();
    }

    public static Response badRequest(String entity, String cacheControlValue) {
        return jsonBuilder(Response.Status.BAD_REQUEST, entity)
                .header(HttpHeaders.CACHE_CONTROL, cacheControlValue)
                .build();
    }

    public static Response unauthorized() {
        return unauthorized(null);
    }

    public static Response unauthorized(String error) {
        String headerValue = "Bearer" + (error == null ? "" : " error=\"" + error + "\"");
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, headerValue)
                .build();
    }

    public static Response unauthorized(String error, String cacheControlValue) {
        String headerValue = "Bearer" + (error == null ? "" : " error=\"" + error + "\"");
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, headerValue)
                .header(HttpHeaders.CACHE_CONTROL, cacheControlValue)
                .build();
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
