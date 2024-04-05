package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
public class CredentialResourceTest {

    private static final CredentialService credentialService = mock(CredentialService.class);
    private final ResourceExtension EXT =
            ResourceExtension.builder()
                    .addResource(new CredentialResource(credentialService))
                    .build();

    @BeforeEach
    void setUp() {
        Mockito.reset(credentialService);
    }

    @Test
    void shouldReturn400WhenProofJwtInRequestBodyIsInvalid()
            throws JsonProcessingException,
                    DataStoreException,
                    AccessTokenValidationException,
                    ClaimMismatchException,
                    SigningException,
                    ProofJwtValidationException {
        JsonNode requestBody = new ObjectMapper().readTree("{\"proof\":{\"proof_type\":\"jwt\"}}");

        final Response response =
                EXT.target("/credential")
                        .request()
                        .header(
                                "Authorization",
                                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(0)).run(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("Missing jwt in request body"));
    }

    @Test
    void shouldReturn400WhenAuthorizationHeaderIsNotValidBearerAccessToken()
            throws JsonProcessingException,
                    DataStoreException,
                    AccessTokenValidationException,
                    ClaimMismatchException,
                    SigningException,
                    ProofJwtValidationException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");

        final Response response =
                EXT.target("/credential")
                        .request()
                        .header(
                                "Authorization",
                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(0)).run(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("Invalid authorization header"));
    }

    @Test
    void shouldReturn500WhenCredentialServiceThrowsAnException()
            throws DataStoreException,
                    AccessTokenValidationException,
                    ClaimMismatchException,
                    SigningException,
                    ProofJwtValidationException,
                    JsonProcessingException {

        String authorizationHeader =
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");
        doThrow(new SigningException("Mock error message", new Exception()))
                .when(credentialService)
                .run(any(), any());

        final Response response =
                EXT.target("/credential")
                        .request()
                        .header("Authorization", authorizationHeader)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(1)).run(any(), any());
        assertThat(response.getStatus(), is(500));
        reset(credentialService);
    }

    @Test
    void shouldReturn200AndTheCredential()
            throws DataStoreException,
                    AccessTokenValidationException,
                    ClaimMismatchException,
                    SigningException,
                    ProofJwtValidationException,
                    JsonProcessingException,
                    ParseException {

        String authorizationHeader =
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");

        Credential credential = getMockCredential();

        when(credentialService.run(any(BearerAccessToken.class), any(CredentialRequestBody.class)))
                .thenReturn(credential);

        final Response response =
                EXT.target("/credential")
                        .request()
                        .header("Authorization", authorizationHeader)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(1)).run(any(), any());
        assertThat(response.getStatus(), is(200));
        assertThat(
                response.readEntity(String.class),
                is(new ObjectMapper().writeValueAsString(credential)));
    }

    private Credential getMockCredential() throws ParseException {
        SignedJWT credential =
                SignedJWT.parse(
                        "eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJzdWIiOiJkaWQ6a2V5Ok1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRVRjbTNMTUpqelZUeEEyTGQ3VEYybHZqRm5oQXV6amZNQm1rbE01QnMxNjFWNERzQyt6N2ZjUUdCRzJvNGZ3QXRKc1ltZ0w2MTQ4Qzl1UkVZUTd5MEdRPT0iLCJuYmYiOjE3MTIyNTM1OTEsImlzcyI6InVybjpmZGM6Z292OnVrOmV4YW1wbGUtY3JlZGVudGlhbC1pc3N1ZXIiLCJjb250ZXh0IjpbImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL3YxIl0sImV4cCI6MTc0Mzc4OTU5MSwiaWF0IjoxNzEyMjUzNTkxLCJ2YyI6eyJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiU29jaWFsU2VjdXJpdHlDcmVkZW50aWFsIl0sImNyZWRlbnRpYWxTdWJqZWN0Ijp7Im5hbWUiOlt7Im5hbWVQYXJ0cyI6W3sidmFsdWUiOiJNciIsInR5cGUiOiJUaXRsZSJ9LHsidmFsdWUiOiJTYXJhaCIsInR5cGUiOiJHaXZlbk5hbWUifSx7InZhbHVlIjoiRWxpemFiZXRoIiwidHlwZSI6IkdpdmVuTmFtZSJ9LHsidmFsdWUiOiJFZHdhcmRzIiwidHlwZSI6IkZhbWlseU5hbWUifV19XSwic29jaWFsU2VjdXJpdHlSZWNvcmQiOlt7InBlcnNvbmFsTnVtYmVyIjoiUVExMjM0NTZDIn1dfX19.F49yZ0Y5cBYMMrIwroYJffUoidgOQBXJRa-MzU77Qa875G5VOIYHv9ZmtqjAb7pvHseVquBQEhLGtcULUlSfSA");

        return new Credential(credential);
    }
}
