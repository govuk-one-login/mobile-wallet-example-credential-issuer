package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jose.JOSEException;
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
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CredentialOfferServiceTest {
    private CredentialOfferService credentialOfferService;
    private final KmsService kmsService = mock(KmsService.class);
    private final ConfigurationService configurationService = new ConfigurationService();

    @BeforeEach
    void setUp() {
        credentialOfferService = new CredentialOfferService(configurationService, kmsService);
    }

    @Test
    @DisplayName("Should build and return a credential offer")
    void testItReturnsCredentialOffer() throws SigningException, JOSEException {
        SignResponse signResponse = getMockedSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(signResponse);

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(
                        "e27474f5-6aef-40a4-bed6-5e4e1ec3f885", "TestCredentialType");

        assertEquals(
                "https://example-credential-issuer.mobile.dev.account.gov.uk",
                credentialOffer.getCredentialIssuer());
        assertArrayEquals(new String[] {"TestCredentialType"}, credentialOffer.getCredentials());
        assertThat(credentialOffer, hasProperty("grants"));
        assertThat(
                credentialOffer
                        .getGrants()
                        .get("urn:ietf:params:oauth:grant-type:pre-authorized_code")
                        .get("pre-authorized_code"),
                startsWith("eyJraWQiOiJmZjI3NWI5Mi0wZGVm"));
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
