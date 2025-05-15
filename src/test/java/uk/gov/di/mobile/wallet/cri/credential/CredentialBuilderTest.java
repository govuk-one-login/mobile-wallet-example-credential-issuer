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
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
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

    private CredentialBuilder<SocialSecurityCredentialSubject> credentialBuilderSocialSecurity;
    private CredentialBuilder<BasicCheckCredentialSubject> credentialBuilderBasicCheck;
    private CredentialBuilder<VeteranCardCredentialSubject> credentialBuilderVeteranCard;

    private SocialSecurityCredentialSubject socialSecurityCredentialSubject;
    private BasicCheckCredentialSubject basicCheckCredentialSubject;
    private VeteranCardCredentialSubject veteranCardCredentialSubject;

    private static Instant fixedInstant;
    private static ObjectMapper objectMapper;

    private static final String KMS_KEY_ID = "ff275b92-0def-4dfc-b0f6-87c96b26c6c7";
    private static final String DID_KEY_ID =
            "did:web:example-credential-issuer.gov.uk#78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274";
    private static final String EXAMPLE_CREDENTIAL_ISSUER = "https://example-cri-url.gov.uk";
    private static final String DID_KEY =
            "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==";

    @BeforeAll
    static void beforeAll() {
        fixedInstant = Instant.now();
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        Clock nowClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
        credentialBuilderSocialSecurity =
                new CredentialBuilder<>(configurationService, kmsService, nowClock);
        credentialBuilderBasicCheck =
                new CredentialBuilder<>(configurationService, kmsService, nowClock);
        credentialBuilderVeteranCard =
                new CredentialBuilder<>(configurationService, kmsService, nowClock);
        when(configurationService.getSigningKeyAlias()).thenReturn("mock-signing-key-alias");
        when(configurationService.getSelfUrl()).thenReturn(EXAMPLE_CREDENTIAL_ISSUER);
        when(configurationService.getCredentialTtlInDays()).thenReturn(365L);
        when(kmsService.getKeyId(any(String.class))).thenReturn(KMS_KEY_ID);
        when(configurationService.getDidController())
                .thenReturn("example-credential-issuer.gov.uk");
        socialSecurityCredentialSubject =
                objectMapper.readValue(
                        "{\"id\":\"did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==\",\"name\":[{\"nameParts\":[{\"type\":\"Title\",\"value\":\"Miss\"},{\"type\":\"GivenName\",\"value\":\"Sarah\"},{\"type\":\"GivenName\",\"value\":\"Elizabeth\"},{\"type\":\"FamilyName\",\"value\":\"Edwards\"},{\"type\":\"FamilyName\",\"value\":\"Green\"}]}],\"socialSecurityRecord\":[{\"personalNumber\":\"QQ123456C\"}]}",
                        SocialSecurityCredentialSubject.class);
        basicCheckCredentialSubject =
                objectMapper.readValue(
                        "{\"id\":\"did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==\",\"issuanceDate\":\"2024-07-11\",\"expirationDate\":\"2025-07-11\",\"name\":[{\"nameParts\":[{\"type\":\"GivenName\",\"value\":\"Bonnie\"},{\"type\":\"FamilyName\",\"value\":\"Blue\"}]}],\"birthDate\":[{\"value\":\"1970-12-05\"}],\"address\":[{\"subBuildingName\":\"Flat 11\",\"buildingName\":\"Blashford\",\"streetName\":\"Adelaide Road\",\"addressLocality\":\"London\",\"postalCode\":\"NW3 3RX\",\"addressCountry\":\"GB\"}],\"basicCheckRecord\":[{\"certificateNumber\":\"009878863\",\"applicationNumber\":\"E0023455534\",\"certificateType\":\"basic\",\"outcome\":\"Result clear\",\"policeRecordsCheck\":\"Clear\"}]}",
                        BasicCheckCredentialSubject.class);
        veteranCardCredentialSubject =
                objectMapper.readValue(
                        "{\"id\":\"did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==\",\"name\":[{\"nameParts\":[{\"type\":\"GivenName\",\"value\":\"Bonnie\"},{\"type\":\"FamilyName\",\"value\":\"Blue\"}]}],\"birthDate\":[{\"value\":\"1970-12-05\"}],\"veteranCard\":[{\"expiryDate\":\"2000-07-11\",\"serviceNumber\":\"25057386\",\"serviceBranch\":\"HM Naval Service\",\"photo\":null}]}",
                        VeteranCardCredentialSubject.class);
    }

    @Test
    void Should_Call_KmsSign_With_Correct_Parameters()
            throws SigningException, JOSEException, NoSuchAlgorithmException {
        ArgumentCaptor<SignRequest> signRequestArgumentCaptor =
                ArgumentCaptor.forClass(SignRequest.class);
        SignResponse mockSignResponse = getMockKmsSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(mockSignResponse);

        credentialBuilderSocialSecurity.buildCredential(
                socialSecurityCredentialSubject,
                CredentialType.SOCIAL_SECURITY_CREDENTIAL,
                null,
                1);

        verify(kmsService).sign(signRequestArgumentCaptor.capture());
        SignRequest capturedSignRequest = signRequestArgumentCaptor.getValue();
        assertThat(capturedSignRequest.signingAlgorithm().name(), equalTo("ECDSA_SHA_256"));
        assertThat(capturedSignRequest.messageType().name(), equalTo("DIGEST"));
        assertThat(capturedSignRequest.keyId(), equalTo(KMS_KEY_ID));
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
                                credentialBuilderSocialSecurity.buildCredential(
                                        socialSecurityCredentialSubject,
                                        CredentialType.SOCIAL_SECURITY_CREDENTIAL,
                                        null,
                                        1));

        assertThat(exception.getMessage(), containsString("Error signing token"));
    }

    @Test
    void Should_Return_Social_Security_Credential()
            throws SigningException, JOSEException, NoSuchAlgorithmException, ParseException {
        SignResponse mockSignResponse = getMockKmsSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(mockSignResponse);

        SignedJWT credential =
                SignedJWT.parse(
                        credentialBuilderSocialSecurity.buildCredential(
                                socialSecurityCredentialSubject,
                                CredentialType.SOCIAL_SECURITY_CREDENTIAL,
                                null,
                                525600));

        assertThat(credential.getHeader().getAlgorithm(), equalTo(JWSAlgorithm.ES256));
        assertThat(credential.getHeader().getKeyID(), equalTo(DID_KEY_ID));
        assertThat(credential.getHeader().getType(), equalTo(new JOSEObjectType("vc+jwt")));
        assertThat(credential.getHeader().getContentType(), equalTo("vc"));
        assertThat(credential.getJWTClaimsSet().getIssuer(), equalTo(EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(credential.getJWTClaimsSet().getSubject(), equalTo(DID_KEY));
        assertThat(
                credential.getJWTClaimsSet().getIssueTime().toString(),
                equalTo(Date.from(fixedInstant).toString()));
        assertThat(
                credential.getJWTClaimsSet().getNotBeforeTime().toString(),
                equalTo(Date.from(fixedInstant).toString()));
        assertThat(
                credential.getJWTClaimsSet().getExpirationTime().toString(),
                equalTo(Date.from(fixedInstant.plus(365, ChronoUnit.DAYS)).toString()));
        assertThat(
                credential.getJWTClaimsSet().getListClaim("@context"),
                equalTo(List.of("https://www.w3.org/ns/credentials/v2")));
        assertThat(
                credential.getJWTClaimsSet().getListClaim("type"),
                equalTo(List.of("VerifiableCredential", "SocialSecurityCredential")));
        assertThat(
                credential.getJWTClaimsSet().getClaim("issuer"),
                equalTo(EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(
                credential.getJWTClaimsSet().getClaim("name"),
                equalTo("National Insurance number"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("description"),
                equalTo("National Insurance number"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("validFrom").toString(),
                equalTo(fixedInstant.truncatedTo(ChronoUnit.SECONDS).toString()));
        assertThat(
                credential.getJWTClaimsSet().getClaim("credentialSubject").toString(),
                equalTo(
                        "{id=did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==, name=[{nameParts=[{type=Title, value=Miss}, {type=GivenName, value=Sarah}, {type=GivenName, value=Elizabeth}, {type=FamilyName, value=Edwards}, {type=FamilyName, value=Green}]}], socialSecurityRecord=[{personalNumber=QQ123456C}]}"));
        assertThat(credential.getState(), equalTo(JWSObject.State.SIGNED));
    }

    @Test
    void Should_Return_Basic_Check_Credential()
            throws SigningException, JOSEException, NoSuchAlgorithmException, ParseException {
        SignResponse mockSignResponse = getMockKmsSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(mockSignResponse);

        SignedJWT credential =
                SignedJWT.parse(
                        credentialBuilderBasicCheck.buildCredential(
                                basicCheckCredentialSubject,
                                CredentialType.BASIC_CHECK_CREDENTIAL,
                                "2025-07-11",
                                525600));

        assertThat(credential.getHeader().getAlgorithm(), equalTo(JWSAlgorithm.ES256));
        assertThat(credential.getHeader().getKeyID(), equalTo(DID_KEY_ID));
        assertThat(credential.getHeader().getType(), equalTo(new JOSEObjectType("vc+jwt")));
        assertThat(credential.getHeader().getContentType(), equalTo("vc"));
        assertThat(credential.getJWTClaimsSet().getIssuer(), equalTo(EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(credential.getJWTClaimsSet().getSubject(), equalTo(DID_KEY));
        assertThat(
                credential.getJWTClaimsSet().getIssueTime().toString(),
                equalTo(Date.from(fixedInstant).toString()));
        assertThat(
                credential.getJWTClaimsSet().getNotBeforeTime().toString(),
                equalTo(Date.from(fixedInstant).toString()));
        assertThat(
                credential.getJWTClaimsSet().getExpirationTime().toString(),
                equalTo(Date.from(fixedInstant.plus(365, ChronoUnit.DAYS)).toString()));
        assertThat(
                credential.getJWTClaimsSet().getListClaim("@context"),
                equalTo(List.of("https://www.w3.org/ns/credentials/v2")));
        assertThat(
                credential.getJWTClaimsSet().getListClaim("type"),
                equalTo(List.of("VerifiableCredential", "BasicCheckCredential")));
        assertThat(
                credential.getJWTClaimsSet().getClaim("issuer"),
                equalTo(EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(
                credential.getJWTClaimsSet().getClaim("name"), equalTo("Basic DBS check result"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("description"),
                equalTo("Basic DBS check result"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("validFrom").toString(),
                equalTo(fixedInstant.truncatedTo(ChronoUnit.SECONDS).toString()));
        assertThat(
                credential.getJWTClaimsSet().getClaim("validUntil"),
                equalTo("2025-07-11T22:59:59Z"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("credentialSubject").toString(),
                equalTo(
                        "{id=did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==, issuanceDate=2024-07-11, expirationDate=2025-07-11, name=[{nameParts=[{type=GivenName, value=Bonnie}, {type=FamilyName, value=Blue}]}], birthDate=[{value=1970-12-05}], address=[{subBuildingName=Flat 11, buildingName=Blashford, streetName=Adelaide Road, addressLocality=London, postalCode=NW3 3RX, addressCountry=GB}], basicCheckRecord=[{certificateNumber=009878863, applicationNumber=E0023455534, certificateType=basic, outcome=Result clear, policeRecordsCheck=Clear}]}"));

        assertThat(credential.getState(), equalTo(JWSObject.State.SIGNED));
    }

    @Test
    void Should_Return_Digital_Veteran_Card()
            throws SigningException, JOSEException, NoSuchAlgorithmException, ParseException {
        SignResponse mockSignResponse = getMockKmsSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(mockSignResponse);

        SignedJWT credential =
                SignedJWT.parse(
                        credentialBuilderVeteranCard.buildCredential(
                                veteranCardCredentialSubject,
                                CredentialType.DIGITAL_VETERAN_CARD,
                                "2000-07-11",
                                525600));

        assertThat(credential.getHeader().getAlgorithm(), equalTo(JWSAlgorithm.ES256));
        assertThat(credential.getHeader().getKeyID(), equalTo(DID_KEY_ID));
        assertThat(credential.getHeader().getType(), equalTo(new JOSEObjectType("vc+jwt")));
        assertThat(credential.getHeader().getContentType(), equalTo("vc"));
        assertThat(credential.getJWTClaimsSet().getIssuer(), equalTo(EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(credential.getJWTClaimsSet().getSubject(), equalTo(DID_KEY));
        assertThat(
                credential.getJWTClaimsSet().getIssueTime().toString(),
                equalTo(Date.from(fixedInstant).toString()));
        assertThat(
                credential.getJWTClaimsSet().getNotBeforeTime().toString(),
                equalTo(Date.from(fixedInstant).toString()));
        assertThat(
                credential.getJWTClaimsSet().getExpirationTime().toString(),
                equalTo(Date.from(fixedInstant.plus(365, ChronoUnit.DAYS)).toString()));
        assertThat(
                credential.getJWTClaimsSet().getListClaim("@context"),
                equalTo(List.of("https://www.w3.org/ns/credentials/v2")));
        assertThat(
                credential.getJWTClaimsSet().getListClaim("type"),
                equalTo(List.of("VerifiableCredential", "digitalVeteranCard")));
        assertThat(
                credential.getJWTClaimsSet().getClaim("issuer"),
                equalTo(EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(
                credential.getJWTClaimsSet().getClaim("name"),
                equalTo("HM Armed Forces Veteran Card"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("description"),
                equalTo("HM Armed Forces Veteran Card"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("validFrom").toString(),
                equalTo(fixedInstant.truncatedTo(ChronoUnit.SECONDS).toString()));
        assertThat(
                credential.getJWTClaimsSet().getClaim("validUntil"),
                equalTo("2000-07-11T22:59:59Z"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("credentialSubject").toString(),
                equalTo(
                        "{id=did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==, name=[{nameParts=[{type=GivenName, value=Bonnie}, {type=FamilyName, value=Blue}]}], birthDate=[{value=1970-12-05}], veteranCard=[{expiryDate=2000-07-11, serviceNumber=25057386, serviceBranch=HM Naval Service, photo=null}]}"));

        assertThat(credential.getState(), equalTo(JWSObject.State.SIGNED));
    }

    private SignResponse getMockKmsSignResponse() throws JOSEException {
        var signingKey =
                new ECKeyGenerator(Curve.P_256)
                        .keyID(KMS_KEY_ID)
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
                .keyId(KMS_KEY_ID)
                .build();
    }
}
