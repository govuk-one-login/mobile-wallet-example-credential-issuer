package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyHelper;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class CredentialBuilder<T extends CredentialSubject> {

    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;
    private static final String CONTEXT_V1 = "https://www.w3.org/2018/credentials/v1";
    private static final String CONTEXT_V2 = "https://www.w3.org/ns/credentials/v2";
    private final Clock clock;

    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;

    @ExcludeFromGeneratedCoverageReport
    public CredentialBuilder(ConfigurationService configurationService, KeyProvider keyProvider) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.clock = Clock.systemUTC();
    }

    public CredentialBuilder(
            ConfigurationService configurationService, KeyProvider keyProvider, Clock clock) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.clock = clock;
    }

    // VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
    public Credential buildCredential(String proofJwtDidKey, VCClaim vcClaim)
            throws SigningException, NoSuchAlgorithmException {
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        var encodedHeader = getEncodedHeader(keyId);

        var encodedClaims = getEncodedClaims(proofJwtDidKey, vcClaim);
        var message = encodedHeader + "." + encodedClaims;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(message.getBytes());

        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(encodedHash))
                        .messageType(MessageType.DIGEST)
                        .keyId(keyId)
                        .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                        .build();

        try {
            SignResponse signResult = keyProvider.sign(signRequest);
            String signature = encodedSignature(signResult);
            SignedJWT signedJWT = SignedJWT.parse(message + "." + signature);
            return new Credential(signedJWT);
        } catch (Exception exception) {
            throw new SigningException(
                    String.format("Error signing token: %s", exception.getMessage()), exception);
        }
    }

    // VC MD v2.0
    public Credential buildCredential(
            T credentialSubject, CredentialTypeAndName credentialTypeAndName, String validUntil)
            throws SigningException, NoSuchAlgorithmException {
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        var encodedHeader = getEncodedHeader(keyId, "vc+jwt", "vc");
        var encodedClaims = getEncodedClaims(credentialSubject, credentialTypeAndName, validUntil);
        var message = encodedHeader + "." + encodedClaims;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(message.getBytes());

        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(encodedHash))
                        .messageType(MessageType.DIGEST)
                        .keyId(keyId)
                        .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                        .build();

        try {
            SignResponse signResult = keyProvider.sign(signRequest);
            String signature = encodedSignature(signResult);
            SignedJWT signedJWT = SignedJWT.parse(message + "." + signature);
            return new Credential(signedJWT);
        } catch (Exception exception) {
            throw new SigningException(
                    String.format("Error signing token: %s", exception.getMessage()), exception);
        }
    }

    // VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
    private Base64URL getEncodedClaims(String proofJwtDidKey, VCClaim vcClaim) {
        Instant now = clock.instant();
        Date nowDate = Date.from(now);

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .issuer(configurationService.getSelfUrl())
                        .issueTime(nowDate)
                        .notBeforeTime(nowDate)
                        .expirationTime(
                                Date.from(
                                        now.plus(
                                                configurationService.getCredentialTtlInDays(),
                                                ChronoUnit.DAYS)))
                        .subject(proofJwtDidKey)
                        .claim("vc", vcClaim)
                        .claim("context", new String[] {CONTEXT_V1});
        return Base64URL.encode(claimsBuilder.build().toString());
    }

    private Base64URL getEncodedClaims(
            T credentialSubject, CredentialTypeAndName credentialTypeAndName, String validUntil) {
        Instant now = clock.instant();
        Date nowDate = Date.from(now);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String validFromIso = formatter.format(now.atZone(ZoneOffset.UTC));

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .issuer(configurationService.getSelfUrl())
                        .subject(credentialSubject.getId())
                        .issueTime(nowDate)
                        .notBeforeTime(nowDate)
                        .expirationTime(
                                Date.from(
                                        now.plus(
                                                configurationService.getCredentialTtlInDays(),
                                                ChronoUnit.DAYS)))
                        .claim("@context", new String[] {CONTEXT_V2})
                        .claim(
                                "type",
                                new String[] {
                                    "VerifiableCredential", credentialTypeAndName.getType()
                                })
                        .claim("issuer", configurationService.getSelfUrl())
                        .claim("name", credentialTypeAndName.getName())
                        .claim("description", credentialTypeAndName.getName())
                        .claim("validFrom", validFromIso)
                        .claim("credentialSubject", credentialSubject);

        if (validUntil != null) {
            String validUntilISO = String.format("%s%s", validUntil, "T22:59:59Z");
            claimsBuilder.claim("validUntil", validUntilISO);
        }

        return Base64URL.encode(claimsBuilder.build().toString());
    }

    // VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
    private Base64URL getEncodedHeader(String keyId) throws NoSuchAlgorithmException {
        String hashedKeyId = KeyHelper.hashKeyId(keyId);
        var jwsHeader =
                new JWSHeader.Builder(SIGNING_ALGORITHM)
                        .keyID(hashedKeyId)
                        .type(new JOSEObjectType("JWT"))
                        .build();
        return jwsHeader.toBase64URL();
    }

    // VC MD v2.0
    private Base64URL getEncodedHeader(String keyId, String type, String contentType)
            throws NoSuchAlgorithmException {
        String hashedKeyId = KeyHelper.hashKeyId(keyId);
        var jwsHeader =
                new JWSHeader.Builder(SIGNING_ALGORITHM)
                        .keyID(hashedKeyId)
                        .type(new JOSEObjectType(type))
                        .contentType(contentType)
                        .build();
        return jwsHeader.toBase64URL();
    }

    private static String encodedSignature(SignResponse signResult) throws JOSEException {
        return Base64URL.encode(
                        ECDSA.transcodeSignatureToConcat(
                                signResult.signature().asByteArray(),
                                ECDSA.getSignatureByteArrayLength(SIGNING_ALGORITHM)))
                .toString();
    }
}
