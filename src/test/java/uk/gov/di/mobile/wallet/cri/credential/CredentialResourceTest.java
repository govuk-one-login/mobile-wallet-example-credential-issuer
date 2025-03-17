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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

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

    private static final CredentialService credentialService = mock(CredentialService.class);
    private final ResourceExtension resource =
            ResourceExtension.builder()
                    .addResource(new CredentialResource(credentialService))
                    .build();

    @BeforeEach
    void setUp() {
        Mockito.reset(credentialService);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "", // when request body is falsy
                "{\"proof\":{\"proof_type\":\"jwt\"}}", // 'jwt' param is missing
                "{\"proof_type\":\"jwt\", \"jwt\":\"testJwt\"}", // 'proof' param is missing
                "{\"invalidParam\": \"test\", \"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"testJwt\"}}", // request contains additional param 'invalidParam'"
                "{\"proof\":{\"proof_type\":\"somethingElse\", \"jwt\": \"testJwt\"}}", // "'proof_type' param is not 'jwt'"
                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"testJwt\"}}" // JWT is invalid
            })
    void should_Return_400_And_Invalid_Proof_When_ProofJwt_Is_Invalid(String arg)
            throws DataStoreException,
                    AccessTokenValidationException,
                    CredentialServiceException,
                    SigningException,
                    ProofJwtValidationException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    CredentialOfferNotFoundException {
        final Response response =
                resource.target("/credential")
                        .request()
                        .header(
                                "Authorization",
                                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                        .post(Entity.entity(arg, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(0)).getCredential(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("{\"error\":\"invalid_proof\"}"));
    }

    @Test
    @DisplayName(
            "should return 400 and invalid Credential Request when Authorization Header is missing")
    void should_Return_400_When_AuthorizationHeader_Is_Missing()
            throws JsonProcessingException,
                    DataStoreException,
                    AccessTokenValidationException,
                    SigningException,
                    ProofJwtValidationException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");

        final Response response =
                resource.target("/credential")
                        .request()
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(0)).getCredential(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(
                response.readEntity(String.class),
                is("{\"error\":\"invalid_credential_request\"}"));
    }

    @Test
    @DisplayName(
            "should return 400 and invalid Credential Request when AuthorizationHeader is not valid Bearer Access Token")
    void should_Return_400_When_AuthorizationHeader_Is_Not_Valid_Bearer_AccessToken()
            throws JsonProcessingException,
                    DataStoreException,
                    AccessTokenValidationException,
                    SigningException,
                    ProofJwtValidationException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");

        final Response response =
                resource.target("/credential")
                        .request()
                        .header(
                                "Authorization",
                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(0)).getCredential(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(
                response.readEntity(String.class),
                is("{\"error\":\"invalid_credential_request\"}"));
    }

    @Test
    @DisplayName(
            "should return 400 when Credential Service Throws a Credential Offer Not Found Exception")
    void should_Return_400_When_CredentialService_ThrowsException()
            throws DataStoreException,
                    AccessTokenValidationException,
                    SigningException,
                    ProofJwtValidationException,
                    NoSuchAlgorithmException,
                    JsonProcessingException,
                    URISyntaxException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");
        doThrow(new CredentialOfferNotFoundException("Credential offer not found"))
                .when(credentialService)
                .getCredential(any(), any());

        final Response response =
                resource.target("/credential")
                        .request()
                        .header(
                                "Authorization",
                                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(400));
        assertThat(
                response.readEntity(String.class),
                is("{\"error\":\"invalid_credential_request\"}"));
        reset(credentialService);
    }

    @Test
    @DisplayName("should return 500 when Credential Service Throws a Signing Exception")
    void should_Return_500_When_CredentialService_Throws_A_Signing_Exception()
            throws DataStoreException,
                    AccessTokenValidationException,
                    SigningException,
                    ProofJwtValidationException,
                    NoSuchAlgorithmException,
                    JsonProcessingException,
                    URISyntaxException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");
        doThrow(new SigningException("Some signing error", new Exception()))
                .when(credentialService)
                .getCredential(any(), any());

        final Response response =
                resource.target("/credential")
                        .request()
                        .header(
                                "Authorization",
                                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(500));
        reset(credentialService);
    }

    @Test
    @DisplayName("should return 500 when Credential Service Throws a DataStore Exception")
    void should_Return_500_When_CredentialService_Throws_A_DataStore_Exception()
            throws DataStoreException,
                    AccessTokenValidationException,
                    SigningException,
                    ProofJwtValidationException,
                    NoSuchAlgorithmException,
                    JsonProcessingException,
                    URISyntaxException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");
        doThrow(new DataStoreException("Some database error", new Exception()))
                .when(credentialService)
                .getCredential(any(), any());

        final Response response =
                resource.target("/credential")
                        .request()
                        .header(
                                "Authorization",
                                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(500));
        reset(credentialService);
    }

    @Test
    void should_Return_200_And_TheCredential()
            throws DataStoreException,
                    AccessTokenValidationException,
                    SigningException,
                    ProofJwtValidationException,
                    JsonProcessingException,
                    ParseException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\", \"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");

        CredentialResponse credentialResponse = getMockCredentialResponse();

        when(credentialService.getCredential(any(SignedJWT.class), any(SignedJWT.class)))
                .thenReturn(credentialResponse);

        final Response response =
                resource.target("/credential")
                        .request()
                        .header(
                                "Authorization",
                                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        verify(credentialService, Mockito.times(1)).getCredential(any(), any());
        assertThat(response.getStatus(), is(200));
        assertThat(
                response.readEntity(String.class),
                is(new ObjectMapper().writeValueAsString(credentialResponse)));
    }

    private Credential getMockCredential() throws ParseException {
        SignedJWT credential =
                SignedJWT.parse(
                        "eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJzdWIiOiJkaWQ6a2V5Ok1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRVRjbTNMTUpqelZUeEEyTGQ3VEYybHZqRm5oQXV6amZNQm1rbE01QnMxNjFWNERzQyt6N2ZjUUdCRzJvNGZ3QXRKc1ltZ0w2MTQ4Qzl1UkVZUTd5MEdRPT0iLCJuYmYiOjE3MTIyNTM1OTEsImlzcyI6InVybjpmZGM6Z292OnVrOmV4YW1wbGUtY3JlZGVudGlhbC1pc3N1ZXIiLCJjb250ZXh0IjpbImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL3YxIl0sImV4cCI6MTc0Mzc4OTU5MSwiaWF0IjoxNzEyMjUzNTkxLCJ2YyI6eyJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiU29jaWFsU2VjdXJpdHlDcmVkZW50aWFsIl0sImNyZWRlbnRpYWxTdWJqZWN0Ijp7Im5hbWUiOlt7Im5hbWVQYXJ0cyI6W3sidmFsdWUiOiJNciIsInR5cGUiOiJUaXRsZSJ9LHsidmFsdWUiOiJTYXJhaCIsInR5cGUiOiJHaXZlbk5hbWUifSx7InZhbHVlIjoiRWxpemFiZXRoIiwidHlwZSI6IkdpdmVuTmFtZSJ9LHsidmFsdWUiOiJFZHdhcmRzIiwidHlwZSI6IkZhbWlseU5hbWUifV19XSwic29jaWFsU2VjdXJpdHlSZWNvcmQiOlt7InBlcnNvbmFsTnVtYmVyIjoiUVExMjM0NTZDIn1dfX19.F49yZ0Y5cBYMMrIwroYJffUoidgOQBXJRa-MzU77Qa875G5VOIYHv9ZmtqjAb7pvHseVquBQEhLGtcULUlSfSA");
        return new Credential(credential);
    }

    private CredentialResponse getMockCredentialResponse() throws ParseException {
        return new CredentialResponse(getMockCredential(), "3fwe98js");
    }
}
