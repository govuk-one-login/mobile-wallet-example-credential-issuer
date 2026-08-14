package uk.gov.di.mobile.wallet.cri.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

/**
 * A Jersey filter that adds HTTP security headers to all responses.
 *
 * <p>These headers protect against client-side attacks such as clickjacking, MIME-type sniffing,
 * and cross-site scripting.
 */
public class SecurityHeadersFilter implements ContainerResponseFilter {

    @Override
    public void filter(
            ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext
                .getHeaders()
                .putSingle("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        responseContext.getHeaders().putSingle("X-Frame-Options", "deny");
        responseContext.getHeaders().putSingle("X-Content-Type-Options", "nosniff");
        responseContext
                .getHeaders()
                .putSingle("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'");
        responseContext.getHeaders().putSingle("X-Permitted-Cross-Domain-Policies", "none");
        responseContext.getHeaders().putSingle("Referrer-Policy", "no-referrer");
    }
}
