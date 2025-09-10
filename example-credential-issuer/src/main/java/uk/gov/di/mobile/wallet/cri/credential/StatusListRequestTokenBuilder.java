package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
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
    private static final SigningAlgorithmSpec KMS_SIGNING_ALGORITHM =
            SigningAlgorithmSpec.ECDSA_SHA_256;
    private static final String CLAIM_STATUS_EXPIRY = "statusExpiry";
    private static final String CLAIM_URI = "uri";
    private static final String CLAIM_INDEX = "idx";

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

    public String buildIssueToken(long credentialExpiry) throws SigningException {
        JWTClaimsSet claims = buildIssueClaims(credentialExpiry);
        return buildToken(claims);
    }

    public String buildRevokeToken(String uri, int index) throws SigningException {
        JWTClaimsSet claims = buildRevokeClaims(uri, index);
        return buildToken(claims);
    }

    private String buildToken(JWTClaimsSet claimsSet) throws SigningException {
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        Base64URL encodedHeader = buildEncodedHeader(keyId);
        Base64URL encodedClaims = buildEncodedClaims(claimsSet);
        String message = encodedHeader + "." + encodedClaims;

        try {
            String signature = signMessage(message, keyId);
            return message + "." + signature;
        } catch (Exception exception) {
            throw new SigningException(
                    String.format(
                            "Error signing status list request token: %s", exception.getMessage()),
                    exception);
        }
    }

    private String signMessage(String message, String keyId) throws JOSEException {
        SignRequest signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(message.getBytes()))
                        .keyId(keyId)
                        .signingAlgorithm(KMS_SIGNING_ALGORITHM)
                        .build();
        SignResponse signResult = keyProvider.sign(signRequest);
        return toBase64UrlEncodedSignature(signResult);
    }

    private Base64URL buildEncodedHeader(String keyId) {
        String hashedKeyId = sha256Hex(keyId);
        JWSHeader jwsHeader =
                new JWSHeader.Builder(SIGNING_ALGORITHM)
                        .keyID(hashedKeyId)
                        .type(JOSEObjectType.JWT)
                        .build();
        return jwsHeader.toBase64URL();
    }

    private Base64URL buildEncodedClaims(JWTClaimsSet claimsSet) {
        return Base64URL.encode(claimsSet.toString());
    }

    private JWTClaimsSet buildIssueClaims(long credentialExpiry) {
        return createBaseClaimsBuilder().claim(CLAIM_STATUS_EXPIRY, credentialExpiry).build();
    }

    private JWTClaimsSet buildRevokeClaims(String uri, int index) {
        return createBaseClaimsBuilder().claim(CLAIM_URI, uri).claim(CLAIM_INDEX, index).build();
    }

    private JWTClaimsSet.Builder createBaseClaimsBuilder() {
        Instant now = clock.instant();
        String jti = UUID.randomUUID().toString();

        return new JWTClaimsSet.Builder()
                .issuer(configurationService.getStatusListClientId())
                .issueTime(Date.from(now))
                .jwtID(jti);
    }
}
