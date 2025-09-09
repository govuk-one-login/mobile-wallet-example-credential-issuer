package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class CredentialResourceTest {

    private static JsonNode requestBody;

    private static final String ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String BEARER_ACCESS_TOKEN = "Bearer " + ACCESS_TOKEN;
    private static final String EXPECTED_CREDENTIAL_JWT =
            "eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJzdWIiOiJkaWQ6a2V5Ok1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRVRjbTNMTUpqelZUeEEyTGQ3VEYybHZqRm5oQXV6amZNQm1rbE01QnMxNjFWNERzQyt6N2ZjUUdCRzJvNGZ3QXRKc1ltZ0w2MTQ4Qzl1UkVZUTd5MEdRPT0iLCJuYmYiOjE3MTIyNTM1OTEsImlzcyI6InVybjpmZGM6Z292OnVrOmV4YW1wbGUtY3JlZGVudGlhbC1pc3N1ZXIiLCJjb250ZXh0IjpbImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL3YxIl0sImV4cCI6MTc0Mzc4OTU5MSwiaWF0IjoxNzEyMjUzNTkxLCJ2YyI6eyJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiU29jaWFsU2VjdXJpdHlDcmVkZW50aWFsIl0sImNyZWRlbnRpYWxTdWJqZWN0Ijp7Im5hbWUiOlt7Im5hbWVQYXJ0cyI6W3sidmFsdWUiOiJNciIsInR5cGUiOiJUaXRsZSJ9LHsidmFsdWUiOiJTYXJhaCIsInR5cGUiOiJHaXZlbk5hbWUifSx7InZhbHVlIjoiRWxpemFiZXRoIiwidHlwZSI6IkdpdmVuTmFtZSJ9LHsidmFsdWUiOiJFZHdhcmRzIiwidHlwZSI6IkZhbWlseU5hbWUifV19XSwic29jaWFsU2VjdXJpdHlSZWNvcmQiOlt7InBlcnNvbmFsTnVtYmVyIjoiUVExMjM0NTZDIn1dfX19.F49yZ0Y5cBYMMrIwroYJffUoidgOQBXJRa-MzU77Qa875G5VOIYHv9ZmtqjAb7pvHseVquBQEhLGtcULUlSfSA";
    private static final String EXPECTED_NOTIFICATION_ID = "1143910d-b9d0-4cdb-a2d2-046e2bf8f55b";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final CredentialService MOCK_CREDENTIAL_SERVICE = mock(CredentialService.class);

    private final ResourceExtension resource =
            ResourceExtension.builder()
                    .addResource(new CredentialResource(MOCK_CREDENTIAL_SERVICE))
                    .build();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        requestBody =
                OBJECT_MAPPER.readTree(
                        "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");
        Mockito.reset(MOCK_CREDENTIAL_SERVICE);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "", // request body is an empty string
                "{\"proof\":{\"proof_type\":\"jwt\"}}", // 'jwt' param is missing
                "{\"proof_type\":\"jwt\", \"jwt\":\"testJwt\"}", // 'proof' param is missing
                "{\"invalidParam\": \"test\", \"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"testJwt\"}}", // request contains additional param 'invalidParam'"
                "{\"proof\":{\"proof_type\":\"somethingElse\", \"jwt\": \"testJwt\"}}", // "'proof_type' param value is not 'jwt'"
                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"notAValidJwt\"}}" // JWT is invalid
            })
    void Should_Return400_When_RequestBodyIsInvalid(String arg)
            throws AccessTokenValidationException,
                    CredentialServiceException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    CredentialOfferException {
        final Response response =
                resource.target("/credential")
                        .request()
                        .header("Authorization", BEARER_ACCESS_TOKEN)
                        .post(Entity.entity(arg, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(0)).getCredential(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(response.getHeaderString("Cache-Control"), is("no-store"));
        assertThat(response.readEntity(String.class), is("{\"error\":\"invalid_proof\"}"));
    }

    @Test
    void Should_Return401_When_AuthorizationHeaderIsMissing()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    CredentialServiceException,
                    CredentialOfferException {
        final Response response =
                resource.target("/credential")
                        .request()
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(0)).getCredential(any(), any());
        assertThat(response.getStatus(), is(401));
        assertThat(response.getHeaderString("WWW-Authenticate"), is("Bearer"));
    }

    @Test
    void Should_Return401_When_AuthorizationHeaderIsInvalidBearerAccessToken()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    CredentialServiceException,
                    CredentialOfferException {
        final Response response =
                resource.target("/credential")
                        .request()
                        .header("Authorization", "NotBearer " + ACCESS_TOKEN)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(0)).getCredential(any(), any());
        assertThat(response.getStatus(), is(401));
        assertThat(response.getHeaderString("Cache-Control"), is("no-store"));
        assertThat(
                response.getHeaderString("WWW-Authenticate"), is("Bearer error=\"invalid_token\""));
    }

    @Test
    void Should_Return401_When_CredentialServiceThrowsCredentialOfferException()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    CredentialServiceException,
                    CredentialOfferException {
        doThrow(new CredentialOfferException("Some credential offer validation error"))
                .when(MOCK_CREDENTIAL_SERVICE)
                .getCredential(any(), any());

        final Response response =
                resource.target("/credential")
                        .request()
                        .header("Authorization", BEARER_ACCESS_TOKEN)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(401));
        assertThat(response.getHeaderString("Cache-Control"), is("no-store"));
        assertThat(
                response.getHeaderString("WWW-Authenticate"), is("Bearer error=\"invalid_token\""));
        reset(MOCK_CREDENTIAL_SERVICE);
    }

    @Test
    void Should_Return401_When_CredentialServiceThrowsAccessTokenValidationException()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    CredentialServiceException,
                    CredentialOfferException {
        doThrow(new AccessTokenValidationException("Some access token validation error"))
                .when(MOCK_CREDENTIAL_SERVICE)
                .getCredential(any(), any());

        final Response response =
                resource.target("/credential")
                        .request()
                        .header("Authorization", BEARER_ACCESS_TOKEN)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(401));
        assertThat(response.getHeaderString("Cache-Control"), is("no-store"));
        assertThat(
                response.getHeaderString("WWW-Authenticate"), is("Bearer error=\"invalid_token\""));
        reset(MOCK_CREDENTIAL_SERVICE);
    }

    @Test
    void Should_Return400_When_CredentialServiceThrowsProofJwtValidationException()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    CredentialServiceException,
                    CredentialOfferException {
        doThrow(new ProofJwtValidationException("Some proof validation error"))
                .when(MOCK_CREDENTIAL_SERVICE)
                .getCredential(any(), any());

        final Response response =
                resource.target("/credential")
                        .request()
                        .header("Authorization", BEARER_ACCESS_TOKEN)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(response.getHeaderString("Cache-Control"), is("no-store"));
        assertThat(response.readEntity(String.class), is("{\"error\":\"invalid_proof\"}"));
        reset(MOCK_CREDENTIAL_SERVICE);
    }

    @Test
    void Should_Return400_When_CredentialServiceThrowsNonceValidationException()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    CredentialServiceException,
                    CredentialOfferException {
        doThrow(new NonceValidationException("Some nonce validation error"))
                .when(MOCK_CREDENTIAL_SERVICE)
                .getCredential(any(), any());

        final Response response =
                resource.target("/credential")
                        .request()
                        .header("Authorization", BEARER_ACCESS_TOKEN)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(response.getHeaderString("Cache-Control"), is("no-store"));
        assertThat(response.readEntity(String.class), is("{\"error\":\"invalid_nonce\"}"));
        reset(MOCK_CREDENTIAL_SERVICE);
    }

    @Test
    void Should_Return500_When_CredentialServiceThrowsCredentialServiceException()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    CredentialServiceException,
                    CredentialOfferException {
        doThrow(new CredentialServiceException("Some other error", new Exception()))
                .when(MOCK_CREDENTIAL_SERVICE)
                .getCredential(any(), any());

        final Response response =
                resource.target("/credential")
                        .request()
                        .header("Authorization", BEARER_ACCESS_TOKEN)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(500));
        reset(MOCK_CREDENTIAL_SERVICE);
    }

    @Test
    void Should_Return200AndCredential()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    JsonProcessingException,
                    CredentialServiceException,
                    CredentialOfferException {
        CredentialResponse mockCredentialResponse =
                new CredentialResponse(EXPECTED_CREDENTIAL_JWT, EXPECTED_NOTIFICATION_ID);
        when(MOCK_CREDENTIAL_SERVICE.getCredential(any(SignedJWT.class), any(SignedJWT.class)))
                .thenReturn(mockCredentialResponse);

        final Response response =
                resource.target("/credential")
                        .request()
                        .header("Authorization", BEARER_ACCESS_TOKEN)
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(MOCK_CREDENTIAL_SERVICE, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(200));
        assertThat(response.getHeaderString("Cache-Control"), is("no-store"));
        CredentialResponse credentialResponse = response.readEntity(CredentialResponse.class);
        assertThat(
                OBJECT_MAPPER.writeValueAsString(credentialResponse),
                is(OBJECT_MAPPER.writeValueAsString(mockCredentialResponse)));
        assertThat(
                credentialResponse.getCredentials().get(0).getCredentialObj(),
                is(EXPECTED_CREDENTIAL_JWT));
        assertThat(credentialResponse.getCredential(), is(EXPECTED_CREDENTIAL_JWT));
        assertThat(credentialResponse.getNotificationId(), is(EXPECTED_NOTIFICATION_ID));
    }
}
