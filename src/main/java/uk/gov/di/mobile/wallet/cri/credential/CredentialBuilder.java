package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static uk.gov.di.mobile.wallet.cri.services.signing.SignatureHelper.toBase64UrlEncodedSignature;
import static uk.gov.di.mobile.wallet.cri.util.HashUtil.*;

public class CredentialBuilder<T extends CredentialSubject> {

    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;
    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;
    private final Clock clock;
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

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
            T credentialSubject, CredentialType credentialType, long credentialTtlMinutes)
            throws SigningException {
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        var encodedHeader = getEncodedHeader(keyId);
        var encodedClaims =
                getEncodedClaims(credentialSubject, credentialType, credentialTtlMinutes);
        var message = encodedHeader + "." + encodedClaims;

        byte[] encodedHash = sha256(message);

        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(encodedHash))
                        .messageType(MessageType.DIGEST)
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

    private Base64URL getEncodedClaims(
            T credentialSubject, CredentialType credentialType, long credentialTtlMinutes) {
        Instant now = clock.instant();
        Instant expiry = now.plus(credentialTtlMinutes, ChronoUnit.MINUTES);

        Date nowDate = Date.from(now);
        Date expiryDate = Date.from(expiry);

        String validFromISO = ISO_FORMATTER.format(now);
        String validUntilISO = ISO_FORMATTER.format(expiry);

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .issuer(configurationService.getSelfUrl())
                        .subject(credentialSubject.getId())
                        .issueTime(nowDate)
                        .notBeforeTime(nowDate)
                        .expirationTime(expiryDate)
                        .claim("@context", new String[] {"https://www.w3.org/ns/credentials/v2"})
                        .claim(
                                "type",
                                new String[] {"VerifiableCredential", credentialType.getType()})
                        .claim("issuer", configurationService.getSelfUrl())
                        .claim("name", credentialType.getName())
                        .claim("description", credentialType.getName())
                        .claim("validFrom", validFromISO)
                        .claim("validUntil", validUntilISO)
                        .claim("credentialSubject", credentialSubject);

        return Base64URL.encode(claimsBuilder.build().toString());
    }

    private Base64URL getEncodedHeader(String keyId) {
        String hashedKeyId = sha256Hex(keyId);
        String didKeyId = "did:web:" + configurationService.getDidController() + "#" + hashedKeyId;
        var jwsHeader =
                new JWSHeader.Builder(SIGNING_ALGORITHM)
                        .keyID(didKeyId)
                        .type(new JOSEObjectType("vc+jwt"))
                        .contentType("vc")
                        .build();
        return jwsHeader.toBase64URL();
    }
}
