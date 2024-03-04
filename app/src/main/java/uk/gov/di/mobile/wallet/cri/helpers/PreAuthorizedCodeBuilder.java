package uk.gov.di.mobile.wallet.cri.helpers;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.KmsService;

import java.time.Instant;
import java.util.Date;


public class PreAuthorizedCodeBuilder {
    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;
    private static final JOSEObjectType JWT = JOSEObjectType.JWT;
    private final ConfigurationService configurationService;
    private final KmsService kmsService;

    public PreAuthorizedCodeBuilder(ConfigurationService configurationService, KmsService kmsService) {
        this.configurationService = configurationService;
        this.kmsService = kmsService;
    }

    public SignedJWT buildPreAuthorizedCode() {

        String walletSubjectId = "placeholderWalletSubjectId";
        Instant now = Instant.now();

        var jwsHeader = new JWSHeader.Builder(SIGNING_ALGORITHM)
                .keyID(configurationService.getSigningKid())
                .type(JWT)
                .build();
        var encodedHeader = jwsHeader.toBase64URL();

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .audience(configurationService.getAudience())
                        .issueTime(Date.from(now))
                        .issuer(configurationService.getIssuer())
                        .notBeforeTime(Date.from(now))
//                        .expirationTime(generateExpirationTime(now))
                        .claim("client_id", configurationService.getClientId())
                        .claim("credential_identifiers", walletSubjectId);


        var encodedClaims = Base64URL.encode(claimsBuilder.build().toString());
        var message = encodedHeader + "." + encodedClaims;

        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(message.getBytes()))
                        .keyId(configurationService.getSigningKeyAlias())
                        .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                        .build();


        try {
            System.out.println("Signing JWT");
            var signResult = kmsService.sign(signRequest);
            System.out.println("JWT has been signed");

            var signature =
                    Base64URL.encode(
                                    ECDSA.transcodeSignatureToConcat(
                                            signResult.signature().asByteArray(),
                                            ECDSA.getSignatureByteArrayLength(SIGNING_ALGORITHM)))
                            .toString();
            var signedJWT = SignedJWT.parse(message + "." + signature);
            return signedJWT;
        } catch (Exception e) {
            System.out.println("Error when generating SignedJWT: " + e);
            throw new RuntimeException(e);
        }

    }
}