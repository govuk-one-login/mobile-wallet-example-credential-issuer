package uk.gov.di.mobile.wallet.cri.credential_offer;

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

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PreAuthorizedCodeBuilderTest {
    private PreAuthorizedCodeBuilder preAuthorizedCodeBuilder;
    private final KmsService kmsService = mock(KmsService.class);
    ConfigurationService configurationService;
    private static final String KEY_ID = "ff275b92-0def-4dfc-b0f6-87c96b26c6c7";

    @BeforeEach
    void setUp() {
        configurationService = new ConfigurationService();
        preAuthorizedCodeBuilder = new PreAuthorizedCodeBuilder(configurationService, kmsService);
    }

    @Test
    @DisplayName(
            "Should build the pre-authorized code with the correct claims and sign it with KMS")
    void testItReturnsSignedJwt() throws SigningException, ParseException, JOSEException {
        SignResponse signResponse = getMockedSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(signResponse);
        when(kmsService.getKeyId(any(String.class))).thenReturn(KEY_ID);

        SignedJWT preAuthorizedCode =
                preAuthorizedCodeBuilder.buildPreAuthorizedCode(
                        "e27474f5-6aef-40a4-bed6-5e4e1ec3f885");

        assertThat(
                preAuthorizedCode.getJWTClaimsSet().getAudience(),
                equalTo(singletonList("urn:fdc:gov:uk:wallet")));
        assertThat(
                preAuthorizedCode.getJWTClaimsSet().getClaim("clientId"), equalTo("EXAMPLE_CRI"));
        assertThat(
                preAuthorizedCode.getJWTClaimsSet().getIssuer(),
                equalTo("urn:fdc:gov:uk:example-credential-issuer"));
        assertThat(
                preAuthorizedCode.getJWTClaimsSet().getClaim("credential_identifiers"),
                equalTo(singletonList("e27474f5-6aef-40a4-bed6-5e4e1ec3f885")));
        assertThat(preAuthorizedCode.getJWTClaimsSet().getIssueTime(), notNullValue());
        assertThat(
                preAuthorizedCode
                        .getJWTClaimsSet()
                        .getExpirationTime()
                        .before(Date.from(Instant.now().plus(300, ChronoUnit.SECONDS))),
                equalTo(true));
        assertThat(
                preAuthorizedCode.getHeader().getKeyID(),
                equalTo("ff275b92-0def-4dfc-b0f6-87c96b26c6c7"));
        assertThat(preAuthorizedCode.getHeader().getAlgorithm(), equalTo(JWSAlgorithm.ES256));
        assertThat(preAuthorizedCode.getHeader().getType(), equalTo(JOSEObjectType.JWT));
    }

    @Test
    @DisplayName("Should throw a SigningException when KMS throws an exception")
    void testItThrowsSigningException() {
        when(kmsService.sign(any(SignRequest.class))).thenThrow(DisabledException.class);

        SigningException exception =
                assertThrows(
                        SigningException.class,
                        () ->
                                preAuthorizedCodeBuilder.buildPreAuthorizedCode(
                                        "e27474f5-6aef-40a4-bed6-5e4e1ec3f885"));
        assertThat(exception.getMessage(), containsString("Error signing token"));
    }

    private SignResponse getMockedSignResponse() throws JOSEException {
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
