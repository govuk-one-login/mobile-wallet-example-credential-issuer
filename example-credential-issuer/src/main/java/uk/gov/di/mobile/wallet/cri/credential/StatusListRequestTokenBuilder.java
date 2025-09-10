package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static uk.gov.di.mobile.wallet.cri.services.signing.SignatureHelper.toBase64UrlEncodedSignature;
import static uk.gov.di.mobile.wallet.cri.util.HashUtil.sha256Hex;

public class StatusListRequestTokenBuilder {

    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;

    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;
    private final Clock clock;

    public StatusListRequestTokenBuilder(
            ConfigurationService configurationService, KeyProvider keyProvider) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.clock = Clock.systemUTC();
    }

    public StatusListRequestTokenBuilder(
            ConfigurationService configurationService, KeyProvider keyProvider, Clock clock) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.clock = clock;
    }

    public String buildToken(JWTClaimsSet claimsSet) throws SigningException {
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        var encodedHeader = getEncodedHeader(keyId);
        var encodedClaims = getEncodedClaims(claimsSet);
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
            return message + "." + signature;
        } catch (Exception exception) {
            throw new SigningException(
                    String.format("Error signing token: %s", exception.getMessage()), exception);
        }
    }

    private Base64URL getEncodedHeader(String keyId) {
        String hashedKeyId = sha256Hex(keyId);
        var jwsHeader =
                new JWSHeader.Builder(SIGNING_ALGORITHM)
                        .keyID(hashedKeyId)
                        .type(JOSEObjectType.JWT)
                        .build();
        return jwsHeader.toBase64URL();
    }

    private Base64URL getEncodedClaims(JWTClaimsSet claimsSet) {
        return Base64URL.encode(claimsSet.toString());
    }

    public JWTClaimsSet buildIssueClaims(long credentialTtlMinutes) {
        return baseClaimsBuilder().claim("statusExpiry", credentialTtlMinutes).build();
    }

    public JWTClaimsSet buildRevokeClaims(String uri, int index) {
        return baseClaimsBuilder().claim("uri", uri).claim("idx", index).build();
    }

    private JWTClaimsSet.Builder baseClaimsBuilder() {
        Instant now = clock.instant();
        String jti = UUID.randomUUID().toString();

        return new JWTClaimsSet.Builder()
                .issuer(configurationService.getOIDCClientId())
                .issueTime(Date.from(now))
                .jwtID(jti);
    }
}
