package uk.gov.di.mobile.wallet.cri.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersFilterTest {

    @Mock private ContainerRequestContext requestContext;
    @Mock private ContainerResponseContext responseContext;

    private SecurityHeadersFilter filter;
    private MultivaluedMap<String, Object> headers;

    @BeforeEach
    void setUp() {
        filter = new SecurityHeadersFilter();
        headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
    }

    @Test
    void shouldAddStrictTransportSecurityHeader() {
        filter.filter(requestContext, responseContext);

        assertThat(
                headers.getFirst("Strict-Transport-Security"),
                is("max-age=31536000; includeSubDomains"));
    }

    @Test
    void shouldAddXContentTypeOptionsHeader() {
        filter.filter(requestContext, responseContext);

        assertThat(headers.getFirst("X-Content-Type-Options"), is("nosniff"));
    }

    @Test
    void shouldAddReferrerPolicyHeader() {
        filter.filter(requestContext, responseContext);

        assertThat(headers.getFirst("Referrer-Policy"), is("no-referrer"));
    }
}
