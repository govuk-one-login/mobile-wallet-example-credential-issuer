package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialOfferServiceTest {

    private CredentialOfferService credentialOfferService;
    private final PreAuthorizedCodeBuilder preAuthorizedCodeBuilder =
            mock(PreAuthorizedCodeBuilder.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private static final String TEST_CREDENTIAL_ISSUER = "https://test-credential-issuer.gov.uk";

    @BeforeEach
    void setUp() {
        credentialOfferService =
                new CredentialOfferService(configurationService, preAuthorizedCodeBuilder);
        when(configurationService.getSelfUrl()).thenReturn(TEST_CREDENTIAL_ISSUER);
    }

    @Test
    void should_Build_And_Return_CredentialOffer()
            throws SigningException, JOSEException, NoSuchAlgorithmException, ParseException {
        SignedJWT preAuthorizedCode = getTestPreAuthCode();
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        preAuthorizedCode.sign(ecSigner);
        when(preAuthorizedCodeBuilder.buildPreAuthorizedCode(any(String.class)))
                .thenReturn(preAuthorizedCode);

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(
                        "e27474f5-6aef-40a4-bed6-5e4e1ec3f885", "TestCredentialType");

        assertEquals(TEST_CREDENTIAL_ISSUER, credentialOffer.getCredentialIssuer());
        assertArrayEquals(new String[] {"TestCredentialType"}, credentialOffer.getCredentials());
        assertThat(credentialOffer, hasProperty("grants"));
        assertEquals(
                credentialOffer
                        .getGrants()
                        .get("urn:ietf:params:oauth:grant-type:pre-authorized_code")
                        .get("pre-authorized_code"),
                preAuthorizedCode.serialize());
    }

    private static SignedJWT getTestPreAuthCode() {
        return new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                new JWTClaimsSet.Builder().issueTime(Date.from(Instant.now())).build());
    }

    private ECKey getEsPrivateKey() throws ParseException {
        String privateKeyJwk =
                "{\"kty\":\"EC\",\"d\":\"aWs8vn4m77PZ_SFMqpGgDlmgBCvtccsV1sE8UCmWPm0\",\"crv\":\"P-256\",\"x\":\"QW9GkrKtsARqx2stUsf1EwBmFaORYzheMbCq28oAIsg\",\"y\":\"DM7AJ0OmO9EduJoQEzGVT0pNKuzwGr1KI1r3fuU85oQ\"}";
        return ECKey.parse(privateKeyJwk);
    }
}
