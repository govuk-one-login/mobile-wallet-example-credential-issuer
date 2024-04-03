package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CredentialRequestTest {

    @Test
    @DisplayName("Should parse the request body as CredentialRequest")
    void testItParsesRequestBodyAsCredentialRequest() throws JsonProcessingException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\",\"jwt\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");

        CredentialRequest credentialRequest = CredentialRequest.from(requestBody);

        assertNotNull(credentialRequest.getProof().getProofType());
        assertEquals(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                credentialRequest.getProof().getJwt());
        assertEquals("jwt", credentialRequest.getProof().getProofType());
    }

    @Test
    @DisplayName("Should throw BadRequestException if proof key is missing from request body")
    void testItThrowsBadRequestExceptionWhenProofIsMissing() throws JsonProcessingException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof_type\":\"jwt\",\"jwt\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}");

        Throwable exception =
                assertThrows(BadRequestException.class, () -> CredentialRequest.from(requestBody));
        assertEquals("Missing proof", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException if proof_type key is missing from request body")
    void testItThrowsBadRequestExceptionWhenProofTypeIsMissing() throws JsonProcessingException {
        JsonNode requestBody =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"jwt\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}}");

        Throwable exception =
                assertThrows(BadRequestException.class, () -> CredentialRequest.from(requestBody));
        assertEquals("Missing proof type", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException if jwt key is missing from request body")
    void testItThrowsBadRequestExceptionWhenJwtIsMissing() throws JsonProcessingException {
        JsonNode requestBody = new ObjectMapper().readTree("{\"proof\":{\"proof_type\":\"jwt\"}}");

        Throwable exception =
                assertThrows(BadRequestException.class, () -> CredentialRequest.from(requestBody));
        assertEquals("Missing JWT", exception.getMessage());
    }
}
