package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.time.Clock;
import java.time.Instant;

import static uk.gov.di.mobile.wallet.cri.services.signing.SignatureHelper.toBase64UrlEncodedSignature;
import static uk.gov.di.mobile.wallet.cri.util.HashUtil.sha256Hex;

public class PreAuthorizedCodeBuilder {

    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;
    private static final JOSEObjectType JWT = JOSEObjectType.JWT;
    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;
    private final Clock clock;

    public PreAuthorizedCodeBuilder(
            ConfigurationService configurationService, KeyProvider keyProvider) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.clock = Clock.systemDefaultZone();
    }

    public PreAuthorizedCodeBuilder(
            ConfigurationService configurationService, KeyProvider keyProvider, Clock clock) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.clock = clock;
    }

    public SignedJWT buildPreAuthorizedCode(String credentialIdentifier, String credentialType)
            throws SigningException {
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        String hashedKeyId = sha256Hex(keyId);
        var encodedHeader = getEncodedHeader(hashedKeyId);
        var encodedClaims = getEncodedClaims(credentialIdentifier, credentialType);
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

    private Base64URL getEncodedClaims(String credentialIdentifier, String credentialType) {
        Instant now = clock.instant();

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .audience(configurationService.getOneLoginAuthServerUrl())
                        .issuer(configurationService.getSelfUrl().toString())
                        .claim(JWTClaimNames.ISSUED_AT, now.getEpochSecond())
                        .claim(
                                JWTClaimNames.EXPIRATION_TIME,
                                now.plusSeconds(
                                                configurationService
                                                        .getPreAuthorizedCodeTtlInSecs())
                                        .getEpochSecond())
                        .claim("clientId", configurationService.getOIDCClientId())
                        .claim("credential_identifiers", new String[] {credentialIdentifier})
                        .claim("credential_configuration_ids", new String[] {credentialType});

        return Base64URL.encode(claimsBuilder.build().toString());
    }

    private Base64URL getEncodedHeader(String keyId) {
        var jwsHeader = new JWSHeader.Builder(SIGNING_ALGORITHM).keyID(keyId).type(JWT).build();
        return jwsHeader.toBase64URL();
    }
}
