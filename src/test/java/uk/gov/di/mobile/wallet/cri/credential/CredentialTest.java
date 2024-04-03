package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialTest {

    private static final String CREDENTIAL =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    @Test
    @DisplayName("Should create the credential with the right property name (credential)")
    void testItCreatesCredential() throws ParseException, JsonProcessingException {
        SignedJWT credential = SignedJWT.parse(CREDENTIAL);
        Credential credentialObject = new Credential(credential);

        String credentialOfferUriObjectAsString =
                new ObjectMapper().writeValueAsString(credentialObject);

        assertThat(credentialOfferUriObjectAsString, containsString("credential"));
        assertEquals(CREDENTIAL, credentialObject.getCredential());
    }
}
