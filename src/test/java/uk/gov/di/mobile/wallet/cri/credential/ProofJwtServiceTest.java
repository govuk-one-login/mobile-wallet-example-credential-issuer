package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testUtils.MockProofBuilder;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProofJwtServiceTest {

    private ProofJwtService proofJwtService;
    private ECDSASigner ecSigner;
    private final ConfigurationService configurationService = mock(ConfigurationService.class);

    @BeforeEach
    void setup() throws ParseException, JOSEException {
        String proofSigningKey =
                "{\"kty\":\"EC\",\"d\":\"aWs8vn4m77PZ_SFMqpGgDlmgBCvtccsV1sE8UCmWPm0\",\"crv\":\"P-256\",\"x\":\"QW9GkrKtsARqx2stUsf1EwBmFaORYzheMbCq28oAIsg\",\"y\":\"DM7AJ0OmO9EduJoQEzGVT0pNKuzwGr1KI1r3fuU85oQ\"}";
        ecSigner = new ECDSASigner(ECKey.parse(proofSigningKey));
        proofJwtService = new ProofJwtService(configurationService);
        when(configurationService.getSelfUrl()).thenReturn("https://issuer-url.gov.uk");
    }

    @Test
    void Should_ThrowProofJwtValidationException_When_AlgorithmParamDoesNotMatchConfig() {
        SignedJWT mockProof = new MockProofBuilder("RS256").build();

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(mockProof));
        assertEquals(
                "JWT alg header claim [RS256] does not match client config alg [ES256]",
                exception.getMessage());
    }

    @Test
    void Should_ThrowProofJwtValidationException_When_KidParamIsMissing() {
        SignedJWT mockProof = new MockProofBuilder("ES256").withKid(null).build();

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(mockProof));

        assertEquals("JWT kid header claim is null", exception.getMessage());
    }

    @Test
    void Should_ThrowProofJwtValidationException_When_RequiredClaimsAreMissing() {
        SignedJWT mockProof =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.ES256)
                                .keyID(
                                        "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==")
                                .build(),
                        new JWTClaimsSet.Builder().build());

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(mockProof));

        assertEquals("JWT missing required claims: [aud, iat, iss, nonce]", exception.getMessage());
    }

    @Test
    void Should_ProofJwtValidationException_When_AudienceClaimDoesNotMatchConfig() {
        SignedJWT mockProof =
                new MockProofBuilder("ES256").withAudience("invalid-audience").build();

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(mockProof));

        assertEquals(
                "JWT aud claim has value [invalid-audience], must be [https://issuer-url.gov.uk]",
                exception.getMessage());
    }

    @Test
    void Should_ProofJwtValidationException_When_IssuerClaimDoesNotMatchConfig() {
        SignedJWT mockProof = new MockProofBuilder("ES256").withIssuer("invalid-issuer").build();

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(mockProof));

        assertEquals(
                "JWT iss claim has value invalid-issuer, must be urn:fdc:gov:uk:wallet",
                exception.getMessage());
    }

    @Test
    void Should_ThrowProofJwtValidationException_When_DidKeyIsInvalid() {
        SignedJWT mockProof =
                new MockProofBuilder("ES256").withKid("did:key:notAValidDidKey").build();

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(mockProof));

        assertThat(
                exception.getMessage(),
                containsString("Error verifying signature: did:key must be base58 encoded"));
    }

    @Test
    void Should_ThrowProofJwtValidationException_When_SignatureVerificationFails()
            throws JOSEException {
        SignedJWT mockProof =
                new MockProofBuilder("ES256")
                        .withKid("did:key:zDnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX")
                        .build();
        mockProof.sign(ecSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(mockProof));

        assertThat(
                exception.getMessage(), containsString("Proof JWT signature verification failed"));
    }

    @Test
    void Should_ReturnTokenData_When_JwtVerificationSucceeds()
            throws JOSEException, ProofJwtValidationException {
        SignedJWT mockProof = new MockProofBuilder("ES256").build();
        mockProof.sign(ecSigner);

        ProofJwtService.ProofJwtData response = proofJwtService.verifyProofJwt(mockProof);

        assertEquals("134e0c41-a8b4-46d4-aec8-cd547e125589", response.nonce());
        assertEquals(
                "did:key:zDnaeUqPxbNEqiYDMyo6EHt9XxpQcE2arUVgkZyfwA6G5Xacf", response.didKey());
    }
}
