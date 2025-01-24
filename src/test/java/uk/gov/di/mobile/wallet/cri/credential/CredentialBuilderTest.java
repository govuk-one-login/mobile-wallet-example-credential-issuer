package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.DisabledException;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.beans.HasProperty.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CredentialBuilderTest {

    private CredentialBuilder credentialBuilder;
    private final KmsService kmsService = mock(KmsService.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private static final String TEST_KEY_ID = "ff275b92-0def-4dfc-b0f6-87c96b26c6c7";
    private static final String TEST_HASHED_KEY_ID =
            "78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274";
    private static final String TEST_EXAMPLE_CREDENTIAL_ISSUER = "https://example-cri-url.gov.uk";
    private static final String TEST_DID_KEY = "did:key:test-did-key";
    private JsonNode documentDetails;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        credentialBuilder = new CredentialBuilder(configurationService, kmsService);
        when(configurationService.getSigningKeyAlias()).thenReturn("test-signing-key-alias");
        when(configurationService.getSelfUrl()).thenReturn(TEST_EXAMPLE_CREDENTIAL_ISSUER);
        when(configurationService.getCredentialTtlInDays()).thenReturn(300L);
        documentDetails =
                new ObjectMapper()
                        .readTree(
                                "{\"credentialSubject\":{\"name\":[{\"nameParts\":[{\"type\": \"Title\",\"value\": \"Ms\"},{\"type\":\"GivenName\",\"value\":\"Irene\"},{\"type\":\"FamilyName\",\"value\":\"Adler\"}]}],\"socialSecurityRecord\": [{ \"personalNumber\": \"QQ123456A\" }]},\"type\": [\"VerifiableCredential\", \"SocialSecurityCredential\"]}");
    }

    @Test
    void shouldCallKmsSignWithCorrectParameters()
            throws SigningException, JOSEException, NoSuchAlgorithmException {
        // arrange
        ArgumentCaptor<SignRequest> signRequestArgumentCaptor =
                ArgumentCaptor.forClass(SignRequest.class);
        SignResponse mockSignResponse = mockKmsSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(mockSignResponse);
        when(kmsService.getKeyId(any(String.class))).thenReturn(TEST_KEY_ID);

        // act
        credentialBuilder.buildCredential(TEST_DID_KEY, documentDetails);

        // assert
        verify(kmsService).sign(signRequestArgumentCaptor.capture());
        SignRequest capturedSignRequest = signRequestArgumentCaptor.getValue();
        assertThat(capturedSignRequest.signingAlgorithm().name(), equalTo("ECDSA_SHA_256"));
        assertThat(capturedSignRequest.messageType().name(), equalTo("DIGEST"));
        assertThat(capturedSignRequest.keyId(), equalTo(TEST_KEY_ID));
        assertThat(capturedSignRequest.message(), instanceOf(SdkBytes.class));
        assertThat(capturedSignRequest.message().asByteArray().length, equalTo(32));
    }

    @Test
    void shouldReturnValidCredential()
            throws SigningException, ParseException, JOSEException, NoSuchAlgorithmException {
        // arrange
        SignResponse mockSignResponse = mockKmsSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(mockSignResponse);
        when(kmsService.getKeyId(any(String.class))).thenReturn(TEST_KEY_ID);

        // act
        Credential credentialBuilderReturnValue =
                credentialBuilder.buildCredential(TEST_DID_KEY, documentDetails);

        // assert
        SignedJWT credential = SignedJWT.parse(credentialBuilderReturnValue.getCredential());
        assertThat(credentialBuilderReturnValue, hasProperty("credential"));
        assertThat(credential.getHeader().getAlgorithm(), equalTo(JWSAlgorithm.ES256));
        assertThat(credential.getHeader().getType(), equalTo(JOSEObjectType.JWT));
        assertThat(credential.getHeader().getKeyID(), equalTo(TEST_HASHED_KEY_ID));
        assertThat(
                credential.getJWTClaimsSet().getIssuer(), equalTo(TEST_EXAMPLE_CREDENTIAL_ISSUER));
        assertThat(credential.getJWTClaimsSet().getIssueTime(), notNullValue());
        assertThat(credential.getJWTClaimsSet().getNotBeforeTime(), notNullValue());
        assertThat(
                credential
                        .getJWTClaimsSet()
                        .getExpirationTime()
                        .before(Date.from(Instant.now().plus(365, ChronoUnit.DAYS))),
                equalTo(true));
        assertThat(credential.getJWTClaimsSet().getSubject(), equalTo(TEST_DID_KEY));
        assertThat(credential.getJWTClaimsSet().getClaim("vc"), notNullValue());
        assertThat(
                credential.getJWTClaimsSet().getClaim("context"),
                equalTo(singletonList("https://www.w3.org/2018/credentials/v1")));
        assertThat(credential.getState(), equalTo(JWSObject.State.SIGNED));
    }

    @Test
    void shouldThrowSigningExceptionWhenKmsThrowsException() {
        // arrange
        when(kmsService.getKeyId(any(String.class))).thenReturn(TEST_KEY_ID);
        when(kmsService.sign(any(SignRequest.class))).thenThrow(DisabledException.class);

        // act
        SigningException exception =
                assertThrows(
                        SigningException.class,
                        () -> credentialBuilder.buildCredential(TEST_DID_KEY, documentDetails));

        // assert
        assertThat(exception.getMessage(), containsString("Error signing token"));
    }

    private SignResponse mockKmsSignResponse() throws JOSEException {
        var signingKey =
                new ECKeyGenerator(Curve.P_256)
                        .keyID(TEST_KEY_ID)
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
                .keyId(TEST_KEY_ID)
                .build();
    }
}
