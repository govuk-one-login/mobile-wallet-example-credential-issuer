package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
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
    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;
    private final Clock clock;

    @ExcludeFromGeneratedCoverageReport
    public CredentialBuilder(ConfigurationService configurationService, KeyProvider keyProvider) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.clock = Clock.systemUTC();
    }

    // Required to mock time in unit tests
    public CredentialBuilder(
            ConfigurationService configurationService, KeyProvider keyProvider, Clock clock) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
        this.clock = clock;
    }

    public String buildCredential(
            T credentialSubject,
            CredentialType credentialType,
            String validUntil,
            long credentialTtlMinutes)
            throws SigningException, NoSuchAlgorithmException {
        // keyId is the hashed key ID. This value must be appended to the string
        // "did:web:example-credential-issuer.mobile.build.account.gov.uk#" in this ticket:
        // https://govukverify.atlassian.net/browse/DCMAW-11424
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        var encodedHeader = getEncodedHeader(keyId);
        var encodedClaims =
                getEncodedClaims(
                        credentialSubject, credentialType, validUntil, credentialTtlMinutes);
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
            return message + "." + signature;
        } catch (Exception exception) {
            throw new SigningException(
                    String.format("Error signing token: %s", exception.getMessage()), exception);
        }
    }

    private Base64URL getEncodedClaims(
            T credentialSubject,
            CredentialType credentialType,
            String validUntil,
            long credentialTtlMinutes) {
        Instant now = clock.instant();
        Date nowDate = Date.from(now);

        Date expiryEpoch = Date.from(now.plus(credentialTtlMinutes, ChronoUnit.MINUTES));

        String validFromISO =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .format(now.atZone(ZoneOffset.UTC));

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .issuer(configurationService.getSelfUrl())
                        .subject(credentialSubject.getId())
                        .issueTime(nowDate)
                        .notBeforeTime(nowDate)
                        .expirationTime(expiryEpoch)
                        .claim("@context", new String[] {"https://www.w3.org/ns/credentials/v2"})
                        .claim(
                                "type",
                                new String[] {"VerifiableCredential", credentialType.getType()})
                        .claim("issuer", configurationService.getSelfUrl())
                        .claim("name", credentialType.getName())
                        .claim("description", credentialType.getName())
                        .claim("validFrom", validFromISO)
                        .claim("credentialSubject", credentialSubject);

        if (validUntil != null) {
            // Formatting validUntil with the DateTimeFormatter is not possible as its value
            // may not be a valid date; therefore, we append "T22:59:59Z" to validUntil
            // so that we can "convert" it to ISO format. This allows mocking up an invalid date and
            // testing how the wallet handles such scenarios.
            String validUntilISO = String.format("%s%s", validUntil, "T22:59:59Z");
            claimsBuilder.claim("validUntil", validUntilISO);
        }

        return Base64URL.encode(claimsBuilder.build().toString());
    }

    private Base64URL getEncodedHeader(String keyId) throws NoSuchAlgorithmException {
        String hashedKeyId = KeyHelper.hashKeyId(keyId);
        String didKeyId = "did:web:" + configurationService.getDidController() + "#" + hashedKeyId;
        var jwsHeader =
                new JWSHeader.Builder(SIGNING_ALGORITHM)
                        .keyID(didKeyId)
                        .type(new JOSEObjectType("vc+jwt"))
                        .contentType("vc")
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
