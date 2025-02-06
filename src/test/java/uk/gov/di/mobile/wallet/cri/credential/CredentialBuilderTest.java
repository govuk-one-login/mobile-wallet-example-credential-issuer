package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.DisabledException;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.credential.socialSecurityCredential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CredentialBuilderTest {

    private final KmsService kmsService = mock(KmsService.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);

    private CredentialBuilder<SocialSecurityCredentialSubject> credentialBuilder;
    private SocialSecurityCredentialSubject socialSecurityCredentialSubject;

    private static Instant fixedInstant;

    private static final String KEY_ID = "ff275b92-0def-4dfc-b0f6-87c96b26c6c7";
    private static final String HASHED_KEY_ID =
            "78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274";
    private static final String EXAMPLE_CREDENTIAL_ISSUER = "https://example-cri-url.gov.uk";
    private static final String DID_KEY =
            "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==";

    @BeforeAll
    static void beforeAll() {
        fixedInstant = Instant.now();
    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        Clock nowClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
        credentialBuilder = new CredentialBuilder<>(configurationService, kmsService, nowClock);
        when(configurationService.getSigningKeyAlias()).thenReturn("mock-signing-key-alias");
        when(configurationService.getSelfUrl()).thenReturn(EXAMPLE_CREDENTIAL_ISSUER);
        when(configurationService.getCredentialTtlInDays()).thenReturn(365L);
        when(kmsService.getKeyId(any(String.class))).thenReturn(KEY_ID);
        socialSecurityCredentialSubject =
                new ObjectMapper()
                        .readValue(
                                "{\"id\":\"did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==\",\"name\":[{\"nameParts\":[{\"type\":\"Title\",\"value\":\"Miss\"},{\"type\":\"GivenName\",\"value\":\"Sarah\"},{\"type\":\"GivenName\",\"value\":\"Elizabeth\"},{\"type\":\"FamilyName\",\"value\":\"Edwards\"},{\"type\":\"FamilyName\",\"value\":\"Green\"}]}],\"socialSecurityRecord\":[{\"personalNumber\":\"QQ123456C\"}]}",
                                SocialSecurityCredentialSubject.class);
    }

    @Test
    void Should_Call_KmsSign_With_Correct_Parameters()
            throws SigningException, JOSEException, NoSuchAlgorithmException {
        ArgumentCaptor<SignRequest> signRequestArgumentCaptor =
                ArgumentCaptor.forClass(SignRequest.class);
        SignResponse mockSignResponse = getMockKmsSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(mockSignResponse);

        credentialBuilder.buildCredential(
                socialSecurityCredentialSubject, "SocialSecurityCredential");

        verify(kmsService).sign(signRequestArgumentCaptor.capture());
        SignRequest capturedSignRequest = signRequestArgumentCaptor.getValue();
        assertThat(capturedSignRequest.signingAlgorithm().name(), equalTo("ECDSA_SHA_256"));
        assertThat(capturedSignRequest.messageType().name(), equalTo("DIGEST"));
        assertThat(capturedSignRequest.keyId(), equalTo(KEY_ID));
        assertThat(capturedSignRequest.message(), instanceOf(SdkBytes.class));
        assertThat(capturedSignRequest.message().asByteArray().length, equalTo(32));
    }

    @Test
    void Should_Throw_SigningException_When_Kms_Throws_An_Error() {
        when(kmsService.sign(any(SignRequest.class))).thenThrow(DisabledException.class);

        SigningException exception =
                assertThrows(
                        SigningException.class,
                        () ->
                                credentialBuilder.buildCredential(
                                        socialSecurityCredentialSubject,
                                        "SocialSecurityCredential"));

        assertThat(exception.getMessage(), containsString("Error signing token"));
    }

    @Test
    void Should_Return_Social_Security_Credential()
            throws SigningException, JOSEException, NoSuchAlgorithmException, ParseException {
        SignResponse mockSignResponse = getMockKmsSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(mockSignResponse);

        Credential credential =
                credentialBuilder.buildCredential(
                        socialSecurityCredentialSubject, "SocialSecurityCredential");

        SignedJWT token = SignedJWT.parse(credential.getCredential());

        assertThat(credential, hasProperty("credential"));
        assertThat(token.getHeader().getAlgorithm(), equalTo(JWSAlgorithm.ES256));
        assertThat(token.getHeader().getKeyID(), equalTo(HASHED_KEY_ID));
        assertThat(token.getHeader().getType(), equalTo(new JOSEObjectType("vc+jwt")));
        assertThat(token.getHeader().getContentType(), equalTo("vc"));
        assertThat(token.getJWTClaimsSet().getIssuer(), equalTo(EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(token.getJWTClaimsSet().getSubject(), equalTo(DID_KEY));
        assertThat(
                token.getJWTClaimsSet().getIssueTime().toString(),
                equalTo(Date.from(fixedInstant).toString()));
        assertThat(
                token.getJWTClaimsSet().getNotBeforeTime().toString(),
                equalTo(Date.from(fixedInstant).toString()));
        assertThat(
                token.getJWTClaimsSet().getExpirationTime().toString(),
                equalTo(Date.from(fixedInstant.plus(365, ChronoUnit.DAYS)).toString()));
        assertThat(
                token.getJWTClaimsSet().getListClaim("@context"),
                equalTo(List.of("https://www.w3.org/ns/credentials/v2")));
        assertThat(
                token.getJWTClaimsSet().getListClaim("type"),
                equalTo(List.of("VerifiableCredential", "SocialSecurityCredential")));
        assertThat(token.getJWTClaimsSet().getClaim("issuer"), equalTo(EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(token.getJWTClaimsSet().getClaim("name"), equalTo("SocialSecurityCredential"));
        assertThat(
                token.getJWTClaimsSet().getClaim("description"),
                equalTo("SocialSecurityCredential"));
        assertThat(
                token.getJWTClaimsSet().getClaim("validFrom").toString(),
                equalTo(fixedInstant.toString()));
        assertThat(
                token.getJWTClaimsSet().getClaim("credentialSubject").toString(),
                equalTo(
                        "{id=did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==, name=[{nameParts=[{type=Title, value=Miss}, {type=GivenName, value=Sarah}, {type=GivenName, value=Elizabeth}, {type=FamilyName, value=Edwards}, {type=FamilyName, value=Green}]}], socialSecurityRecord=[{personalNumber=QQ123456C}]}"));
        assertThat(token.getState(), equalTo(JWSObject.State.SIGNED));
    }

    private SignResponse getMockKmsSignResponse() throws JOSEException {
        var signingKey =
                new ECKeyGenerator(Curve.P_256)
                        .keyID(KEY_ID)
                        .algorithm(JWSAlgorithm.ES256)
                        .generate();
        var ecdsaSigner = new ECDSASigner(signingKey);
        var jwtClaimsSet = new JWTClaimsSet.Builder().build();
        var jwsHeader = new JWSHeader(JWSAlgorithm.ES256);
        var signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
        signedJWT.sign(ecdsaSigner);
        byte[] derSignature = ECDSA.transcodeSignatureToDER(signedJWT.getSignature().decode());
        return SignResponse.builder()
                .signature(SdkBytes.fromByteArray(derSignature))
                .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                .keyId(KEY_ID)
                .build();
    }
}
