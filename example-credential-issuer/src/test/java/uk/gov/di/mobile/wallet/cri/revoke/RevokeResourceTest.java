package uk.gov.di.mobile.wallet.cri.revoke;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class RevokeResourceTest {

    private static final String DOCUMENT_ID = "ABCdef012345";

    private final RevokeService revokeService = mock(RevokeService.class);
    private final ResourceExtension resource =
            ResourceExtension.builder().addResource(new RevokeResource(revokeService)).build();

    @Test
    void shouldReturn404WhenDocumentIdIsMissingFromPath() {
        final Response response = resource.target("/revoke").request().post(Entity.json(null));

        assertThat(response.getStatus(), is(404));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "shrt", // 4 characters but minimum is 5
                "documentIdIsTooLong1234567", // 26 characters but maximum is 25
                "spaces are not ok",
                "invalidChars@!",
            })
    void shouldReturn400WhenDocumentIdIsInvalid(String documentId) {
        final Response response =
                resource.target("/revoke").path(documentId).request().post(Entity.json(null));

        assertThat(response.getStatus(), is(400));
        assertThat(
                response.readEntity(String.class),
                is(
                        "{\"errors\":[\"path param documentId must match \\\"^[a-zA-Z0-9]{5,25}$\\\"\"]}"));
    }

    @Test
    void shouldReturn404WhenRevokeServiceThrowsCredentialNotFoundException()
            throws CredentialNotFoundException, RevokeServiceException {
        doThrow(new CredentialNotFoundException("No credentials found"))
                .when(revokeService)
                .revokeCredential(DOCUMENT_ID);

        final Response response =
                resource.target("/revoke").path(DOCUMENT_ID).request().post(Entity.json(null));

        assertThat(response.getStatus(), is(404));
    }

    @Test
    void shouldReturn500WhenRevokeServiceThrowsRevocationException()
            throws CredentialNotFoundException, RevokeServiceException {
        doThrow(new RevokeServiceException("Revocation error"))
                .when(revokeService)
                .revokeCredential(DOCUMENT_ID);

        final Response response =
                resource.target("/revoke").path(DOCUMENT_ID).request().post(Entity.json(null));

        assertThat(response.getStatus(), is(500));
    }

    @Test
    void shouldReturn202WhenRevokeServiceExecutesSuccessfully() {
        final Response response =
                resource.target("/revoke").path(DOCUMENT_ID).request().post(Entity.json(null));

        assertThat(response.getStatus(), is(202));
    }
}
