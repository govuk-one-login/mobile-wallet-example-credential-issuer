package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialOfferServiceTest {

    @Mock private PreAuthorizedCodeBuilder preAuthorizedCodeBuilder;

    @Mock private ConfigurationService configurationService;

    @InjectMocks private CredentialOfferService credentialOfferService;

    private static final String CREDENTIAL_ISSUER = "https://test-credential-issuer.gov.uk";
    private static final String CREDENTIAL_IDENTIFIER = "e27474f5-6aef-40a4-bed6-5e4e1ec3f885";
    private static final String CREDENTIAL_TYPE = "TestCredentialType";
    private static final String PRE_AUTH_GRANT_TYPE =
            "urn:ietf:params:oauth:grant-type:pre-authorized_code";
    private static final String PRE_AUTH_CODE_PARAM = "pre-authorized_code";

    @Test
    void Should_BuildAndReturnCredentialOffer() throws Exception {
        SignedJWT preAuthorizedCode = createMockPreAuthorizedCode();
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER))
                .thenReturn(preAuthorizedCode);
        when(configurationService.getSelfUrl()).thenReturn(CREDENTIAL_ISSUER);

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE);

        assertEquals(
                CREDENTIAL_ISSUER,
                credentialOffer.getCredentialIssuer(),
                "Credential issuer should match the configured self URL");
        assertArrayEquals(
                new String[] {CREDENTIAL_TYPE},
                credentialOffer.getCredentialConfigurationIds(),
                "Credential configuration IDs should contain the specified credential type");
        assertNotNull(credentialOffer.getGrants(), "Grants should not be null");
        assertTrue(
                credentialOffer.getGrants().containsKey(PRE_AUTH_GRANT_TYPE),
                "Grants should contain the pre-authorized code grant type");
        assertEquals(
                preAuthorizedCode.serialize(),
                credentialOffer.getGrants().get(PRE_AUTH_GRANT_TYPE).get(PRE_AUTH_CODE_PARAM),
                "Pre-authorized code parameter should contain the JWT");
        verify(preAuthorizedCodeBuilder).buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER);
        verify(configurationService).getSelfUrl();
    }

    @Test
    void Should_PropagateSigningException() throws Exception {
        SigningException expectedError =
                new SigningException("Some signing error", new Exception());
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER))
                .thenThrow(expectedError);

        Exception exception =
                assertThrows(
                        SigningException.class,
                        () ->
                                credentialOfferService.buildCredentialOffer(
                                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE),
                        "Should throw SigningException when token signing fails");
        assertEquals(
                "Some signing error",
                exception.getMessage(),
                "Exception message should match the expected signing error message");
        verify(preAuthorizedCodeBuilder).buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER);
        verifyNoInteractions(configurationService);
    }

    @Test
    void Should_PropagateNoSuchAlgorithmException() throws Exception {
        NoSuchAlgorithmException expectedError =
                new NoSuchAlgorithmException("ES256 not available");
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER))
                .thenThrow(expectedError);

        Exception exception =
                assertThrows(
                        NoSuchAlgorithmException.class,
                        () ->
                                credentialOfferService.buildCredentialOffer(
                                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE),
                        "Should throw NoSuchAlgorithmException when algorithm is unavailable");
        assertEquals(
                "ES256 not available",
                exception.getMessage(),
                "Exception message should match the expected error message");
        verify(preAuthorizedCodeBuilder).buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER);
        verifyNoInteractions(configurationService);
    }

    private SignedJWT createMockPreAuthorizedCode() throws JOSEException, ParseException {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).build();
        JWTClaimsSet claims =
                new JWTClaimsSet.Builder().issueTime(Date.from(Instant.now())).build();

        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new ECDSASigner(createTestPrivateKey()));
        return jwt;
    }

    private ECKey createTestPrivateKey() throws ParseException {
        String privateKeyJwk =
                """
                {
                    "kty": "EC",
                    "d": "aWs8vn4m77PZ_SFMqpGgDlmgBCvtccsV1sE8UCmWPm0",
                    "crv": "P-256",
                    "x": "QW9GkrKtsARqx2stUsf1EwBmFaORYzheMbCq28oAIsg",
                    "y": "DM7AJ0OmO9EduJoQEzGVT0pNKuzwGr1KI1r3fuU85oQ"
                }
                """;
        return ECKey.parse(privateKeyJwk);
    }
}
