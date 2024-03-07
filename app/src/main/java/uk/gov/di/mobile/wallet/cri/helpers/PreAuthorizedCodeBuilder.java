package uk.gov.di.mobile.wallet.cri.helpers;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.KmsService;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class PreAuthorizedCodeBuilder {
    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;
    private static final JOSEObjectType JWT = JOSEObjectType.JWT;
    private final ConfigurationService configurationService;
    private final KmsService kmsService;

    public PreAuthorizedCodeBuilder(
            ConfigurationService configurationService, KmsService kmsService) {
        this.configurationService = configurationService;
        this.kmsService = kmsService;
    }

    public SignedJWT buildPreAuthorizedCode(String credentialIdentifier)
            throws ParseException, JOSEException {
        var encodedHeader = getEncodedHeader();

        var encodedClaims = getEncodedClaims(credentialIdentifier);

        var message = encodedHeader + "." + encodedClaims;

        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(message.getBytes()))
                        .keyId(configurationService.getSigningKeyAlias())
                        .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                        .build();

        SignResponse signResult;
        try {
            System.out.println("Signing JWT");
            signResult = kmsService.sign(signRequest);
            System.out.println("JWT has been signed");
        } catch (Exception e) {
            System.out.println("Error when signing SignedJWT: " + e);
            throw new RuntimeException(e);
        }

        String signature = encodedSignature(signResult);

        var signedJWT = SignedJWT.parse(message + "." + signature);
        System.out.println("Returning JWT" + signedJWT.serialize());
        return signedJWT;
    }

    private static String encodedSignature(SignResponse signResult) throws JOSEException {
        return Base64URL.encode(
                        ECDSA.transcodeSignatureToConcat(
                                signResult.signature().asByteArray(),
                                ECDSA.getSignatureByteArrayLength(SIGNING_ALGORITHM)))
                .toString();
    }

    private Base64URL getEncodedClaims(String walletSubjectId) {
        Instant now = Instant.now();

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .audience(configurationService.getAudience())
                        .issuer(configurationService.getIssuer())
                        .issueTime(Date.from(now))
                        .expirationTime(
                                Date.from(
                                        now.plus(
                                                configurationService.getPreAuthorizedCodeTtl(),
                                                ChronoUnit.SECONDS)))
                        .claim("clientId", configurationService.getClientId())
                        .claim("credential_identifiers", new String[] {walletSubjectId});

        var encodedClaims = Base64URL.encode(claimsBuilder.build().toString());
        return encodedClaims;
    }

    private Base64URL getEncodedHeader() {
        var jwsHeader =
                new JWSHeader.Builder(SIGNING_ALGORITHM)
                        .keyID(configurationService.getSigningKeyAlias())
                        .type(JWT)
                        .build();
        var encodedHeader = jwsHeader.toBase64URL();
        return encodedHeader;
    }
}
