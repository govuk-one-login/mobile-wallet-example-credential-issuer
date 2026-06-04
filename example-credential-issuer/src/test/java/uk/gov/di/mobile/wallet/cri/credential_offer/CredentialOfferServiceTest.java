package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.CredentialOfferException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URI;
import java.text.ParseException;
import java.time.Instant;

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
    private static final String CREDENTIAL_TYPE_MDL = "org.iso.18013.5.1.mDL";
    private static final String CREDENTIAL_TYPE_NINO = "SocialSecurityCredential";
    private static final String CREDENTIAL_TYPE_DBS = "BasicDisclosureCredential";
    private static final String CREDENTIAL_TYPE_VC = "DigitalVeteranCard";
    private static final String CREDENTIAL_TYPE_SIMPLE_MDOC =
            "uk.gov.account.mobile.example-credential-issuer.simplemdoc.1";
    private static final String PRE_AUTH_GRANT_TYPE =
            "urn:ietf:params:oauth:grant-type:pre-authorized_code";
    private static final String PRE_AUTH_CODE_PARAM = "pre-authorized_code";

    @Test
    void Should_BuildAndReturnCredentialOffer_ForMDL() throws Exception {
        SignedJWT preAuthorizedCode = createMockPreAuthorizedCode();
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_MDL))
                .thenReturn(preAuthorizedCode);
        when(configurationService.getSelfUrl()).thenReturn(URI.create(CREDENTIAL_ISSUER));

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_MDL);

        assertEquals(
                CREDENTIAL_ISSUER,
                credentialOffer.getCredentialIssuer(),
                "Credential issuer should match the configured self URL");
        assertArrayEquals(
                new String[] {CREDENTIAL_TYPE_MDL},
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
        verify(preAuthorizedCodeBuilder)
                .buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_MDL);
        verify(configurationService).getSelfUrl();
    }

    @Test
    void Should_BuildAndReturnCredentialOffer_ForNINO() throws Exception {
        SignedJWT preAuthorizedCode = createMockPreAuthorizedCode();
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_NINO))
                .thenReturn(preAuthorizedCode);
        when(configurationService.getSelfUrl()).thenReturn(URI.create(CREDENTIAL_ISSUER));

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_NINO);

        assertEquals(
                CREDENTIAL_ISSUER,
                credentialOffer.getCredentialIssuer(),
                "Credential issuer should match the configured self URL");
        assertArrayEquals(
                new String[] {CREDENTIAL_TYPE_NINO},
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
        verify(preAuthorizedCodeBuilder)
                .buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_NINO);
        verify(configurationService).getSelfUrl();
    }

    @Test
    void Should_BuildAndReturnCredentialOffer_ForDBS() throws Exception {
        SignedJWT preAuthorizedCode = createMockPreAuthorizedCode();
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_DBS))
                .thenReturn(preAuthorizedCode);
        when(configurationService.getSelfUrl()).thenReturn(URI.create(CREDENTIAL_ISSUER));

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_DBS);

        assertEquals(
                CREDENTIAL_ISSUER,
                credentialOffer.getCredentialIssuer(),
                "Credential issuer should match the configured self URL");
        assertArrayEquals(
                new String[] {CREDENTIAL_TYPE_DBS},
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
        verify(preAuthorizedCodeBuilder)
                .buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_DBS);
        verify(configurationService).getSelfUrl();
    }

    @Test
    void Should_BuildAndReturnCredentialOffer_ForVC() throws Exception {
        SignedJWT preAuthorizedCode = createMockPreAuthorizedCode();
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_VC))
                .thenReturn(preAuthorizedCode);
        when(configurationService.getSelfUrl()).thenReturn(URI.create(CREDENTIAL_ISSUER));

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_VC);

        assertEquals(
                CREDENTIAL_ISSUER,
                credentialOffer.getCredentialIssuer(),
                "Credential issuer should match the configured self URL");
        assertArrayEquals(
                new String[] {CREDENTIAL_TYPE_VC},
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
        verify(preAuthorizedCodeBuilder)
                .buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_VC);
        verify(configurationService).getSelfUrl();
    }

    @Test
    void Should_BuildAndReturnCredentialOffer_ForSimpleMdoc() throws Exception {
        SignedJWT preAuthorizedCode = createMockPreAuthorizedCode();
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_SIMPLE_MDOC))
                .thenReturn(preAuthorizedCode);
        when(configurationService.getSelfUrl()).thenReturn(URI.create(CREDENTIAL_ISSUER));

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_SIMPLE_MDOC);

        assertEquals(
                CREDENTIAL_ISSUER,
                credentialOffer.getCredentialIssuer(),
                "Credential issuer should match the configured self URL");
        assertArrayEquals(
                new String[] {CREDENTIAL_TYPE_SIMPLE_MDOC},
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
        verify(preAuthorizedCodeBuilder)
                .buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_SIMPLE_MDOC);
        verify(configurationService).getSelfUrl();
    }

    @Test
    void Should_ThrowCredentialOfferException_When_CredentialTypeIsInvalid() {
        assertThrows(
                CredentialOfferException.class,
                () ->
                        credentialOfferService.buildCredentialOffer(
                                CREDENTIAL_IDENTIFIER, "InvalidCredentialType"));
    }

    @Test
    void Should_PropagateSigningException() throws Exception {
        SigningException expectedError =
                new SigningException("Some signing error", new Exception());
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(
                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_MDL))
                .thenThrow(expectedError);

        Exception exception =
                assertThrows(
                        SigningException.class,
                        () ->
                                credentialOfferService.buildCredentialOffer(
                                        CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_MDL),
                        "Should throw SigningException when token signing fails");
        assertEquals(
                "Some signing error",
                exception.getMessage(),
                "Exception message should match the expected signing error message");
        verify(preAuthorizedCodeBuilder)
                .buildPreAuthorizedCode(CREDENTIAL_IDENTIFIER, CREDENTIAL_TYPE_MDL);
        verifyNoInteractions(configurationService);
    }

    private SignedJWT createMockPreAuthorizedCode() throws JOSEException, ParseException {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).build();
        JWTClaimsSet claims =
                new JWTClaimsSet.Builder()
                        .claim(
                                JWTClaimNames.ISSUED_AT,
                                Instant.parse("2024-01-15T10:30:00Z").getEpochSecond())
                        .build();

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
