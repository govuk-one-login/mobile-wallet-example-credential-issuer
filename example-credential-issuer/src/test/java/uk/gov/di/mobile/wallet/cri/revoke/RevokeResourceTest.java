package uk.gov.di.mobile.wallet.cri.revoke;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class RevokeResourceTest {

    private final RevokeService revokeService = mock(RevokeService.class);
    private final ResourceExtension resource =
            ResourceExtension.builder().addResource(new RevokeResource(revokeService)).build();

    @Test
    void Should_Return400_When_RequestBodyIsNull() {
        final Response response =
                resource.target("/revoke")
                        .request()
                        .post(Entity.entity(null, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(400));
        assertThat(
                response.readEntity(String.class), is("{\"error\":\"request body is required\"}"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"somethingElse\":\"12345\"}", // 'drivingLicenceNumber' is missing
                "{\"drivingLicenceNumber\":\"\"}", // 'drivingLicenceNumber' is empty string
            })
    void Should_Return400_When_DrivingLicenceNumberIsInvalid(String requestBody) {
        final Response response =
                resource.target("/revoke")
                        .request()
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(400));
        assertThat(
                response.readEntity(String.class),
                is("{\"error\":\"drivingLicenceNumber is required\"}"));
    }

    @Test
    void Should_Return404_When_RevokeServiceThrowsCredentialNotFoundException()
            throws CredentialNotFoundException, DataStoreException {
        doThrow(new CredentialNotFoundException("No credentials found"))
                .when(revokeService)
                .revokeCredential("EDWAR515163SE5RO");

        final Response response =
                resource.target("/revoke")
                        .request()
                        .post(
                                Entity.entity(
                                        "{\"drivingLicenceNumber\":\"EDWAR515163SE5RO\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(404));
    }

    @Test
    void Should_Return500_When_RevokeServiceThrowsDataStoreException()
            throws CredentialNotFoundException, DataStoreException {
        doThrow(new DataStoreException("Some database error"))
                .when(revokeService)
                .revokeCredential("EDWAR515163SE5RO");

        final Response response =
                resource.target("/revoke")
                        .request()
                        .post(
                                Entity.entity(
                                        "{\"drivingLicenceNumber\":\"EDWAR515163SE5RO\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(500));
    }

    @Test
    void Should_Return202_When_RevokeServiceExecutesSuccessfully() {
        final Response response =
                resource.target("/revoke")
                        .request()
                        .post(
                                Entity.entity(
                                        "{\"drivingLicenceNumber\":\"EDWAR515163SE5RO\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(202));
    }
}
