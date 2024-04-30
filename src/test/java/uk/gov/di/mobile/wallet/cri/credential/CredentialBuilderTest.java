package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasProperty.hasProperty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CredentialBuilderTest {
    private CredentialBuilder credentialBuilder;
    private final KmsService kmsService = mock(KmsService.class);
    private final String HASHED_KEY_ID =
            "78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274";
    private JsonNode DOCUMENT_DETAILS;

    ConfigurationService configurationService;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        configurationService = new ConfigurationService();
        credentialBuilder = new CredentialBuilder(configurationService, kmsService);

        DOCUMENT_DETAILS =
                new ObjectMapper()
                        .readTree(
                                "{\"credentialSubject\":{\"name\":[{\"nameParts\":[{\"type\": \"Title\",\"value\": \"Ms\"},{\"type\":\"GivenName\",\"value\":\"Irene\"},{\"type\":\"FamilyName\",\"value\":\"Adler\"}]}],\"socialSecurityRecord\": [{ \"personalNumber\": \"QQ123456A\" }]},\"type\": [\"VerifiableCredential\", \"SocialSecurityCredential\"]}");
    }

    @Test
    @DisplayName(
            "Should build the verifiable credential with the correct claims and sign it with KMS")
    void testItReturnsCredential()
            throws SigningException, ParseException, JOSEException, NoSuchAlgorithmException {
        SignResponse signResponse = getMockedSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(signResponse);

        Credential credentialBuilderReturnValue =
                credentialBuilder.buildCredential("did:key:test-did-key", DOCUMENT_DETAILS);

        SignedJWT credential = SignedJWT.parse(credentialBuilderReturnValue.getCredential());

        assertThat(credentialBuilderReturnValue, hasProperty("credential"));
        assertThat(credential.getHeader().getAlgorithm(), equalTo(JWSAlgorithm.ES256));
        assertThat(credential.getHeader().getType(), equalTo(JOSEObjectType.JWT));
        assertThat(credential.getHeader().getKeyID(), equalTo(HASHED_KEY_ID));
        assertThat(
                credential.getJWTClaimsSet().getIssuer(),
                equalTo("urn:fdc:gov:uk:example-credential-issuer"));
        assertThat(credential.getJWTClaimsSet().getIssueTime(), notNullValue());
        assertThat(credential.getJWTClaimsSet().getNotBeforeTime(), notNullValue());
        assertThat(
                credential
                        .getJWTClaimsSet()
                        .getExpirationTime()
                        .before(Date.from(Instant.now().plus(365, ChronoUnit.DAYS))),
                equalTo(true));
        assertThat(credential.getJWTClaimsSet().getSubject(), equalTo("did:key:test-did-key"));
        assertNotNull(credential.getJWTClaimsSet().getClaim("vc"));
        assertThat(
                credential.getJWTClaimsSet().getClaim("context"),
                equalTo(singletonList("https://www.w3.org/2018/credentials/v1")));
    }

    @Test
    @DisplayName("Should throw a SigningException when KMS throws an exception")
    void testItThrowsSigningException() {
        when(kmsService.sign(any(SignRequest.class))).thenThrow(DisabledException.class);

        SigningException exception =
                assertThrows(
                        SigningException.class,
                        () ->
                                credentialBuilder.buildCredential(
                                        "did:key:test-did-key", DOCUMENT_DETAILS));
        assertThat(exception.getMessage(), containsString("Error signing token"));
    }

    private SignResponse getMockedSignResponse() throws JOSEException {
        var signingKey =
                new ECKeyGenerator(Curve.P_256)
                        .keyID(configurationService.getSigningKeyId())
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
                .keyId(configurationService.getSigningKeyId())
                .build();
    }
}
