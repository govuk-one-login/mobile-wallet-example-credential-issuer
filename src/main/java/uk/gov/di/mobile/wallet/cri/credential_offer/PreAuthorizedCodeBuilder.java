package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyHelper;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static uk.gov.di.mobile.wallet.cri.util.KmsSignatureUtil.toBase64UrlEncodedSignature;

public class PreAuthorizedCodeBuilder {

    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;
    private static final JOSEObjectType JWT = JOSEObjectType.JWT;
    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;

    public PreAuthorizedCodeBuilder(
            ConfigurationService configurationService, KeyProvider keyProvider) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
    }

    public SignedJWT buildPreAuthorizedCode(String credentialIdentifier)
            throws SigningException, NoSuchAlgorithmException {
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        String hashedKeyId = KeyHelper.hashKeyId(keyId);
        var encodedHeader = getEncodedHeader(hashedKeyId);
        var encodedClaims = getEncodedClaims(credentialIdentifier);
        var message = encodedHeader + "." + encodedClaims;
        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(message.getBytes()))
                        .keyId(keyId)
                        .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                        .build();

        try {
            SignResponse signResult = keyProvider.sign(signRequest);
            String signature = toBase64UrlEncodedSignature(signResult);
            return SignedJWT.parse(message + "." + signature);
        } catch (Exception exception) {
            throw new SigningException(
                    String.format("Error signing token: %s", exception.getMessage()), exception);
        }
    }

    private Base64URL getEncodedClaims(String credentialIdentifier) {
        Instant now = Instant.now();

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .audience(configurationService.getOneLoginAuthServerUrl())
                        .issuer(configurationService.getSelfUrl())
                        .issueTime(Date.from(now))
                        .expirationTime(
                                Date.from(
                                        now.plus(
                                                configurationService
                                                        .getPreAuthorizedCodeTtlInSecs(),
                                                ChronoUnit.SECONDS)))
                        .claim("clientId", configurationService.getClientId())
                        .claim("credential_identifiers", new String[] {credentialIdentifier});

        return Base64URL.encode(claimsBuilder.build().toString());
    }

    private Base64URL getEncodedHeader(String keyId) {
        var jwsHeader = new JWSHeader.Builder(SIGNING_ALGORITHM).keyID(keyId).type(JWT).build();
        return jwsHeader.toBase64URL();
    }
}
