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

public class CredentialBuilder {

    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;
    private static final JOSEObjectType JWT = JOSEObjectType.JWT;
    private static final String CONTEXT = "https://www.w3.org/2018/credentials/v1";

    private final ConfigurationService configurationService;
    private final KeyProvider keyProvider;

    public CredentialBuilder(ConfigurationService configurationService, KeyProvider keyProvider) {
        this.configurationService = configurationService;
        this.keyProvider = keyProvider;
    }

    public Credential buildCredential(String proofJwtDidKey, Object documentDetails)
            throws SigningException, NoSuchAlgorithmException {
        String keyId = keyProvider.getKeyId(configurationService.getSigningKeyAlias());
        var encodedHeader = getEncodedHeader(keyId);
        var encodedClaims = getEncodedClaims(proofJwtDidKey, documentDetails);
        var message = encodedHeader + "." + encodedClaims;
        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(message.getBytes()))
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

    private static String encodedSignature(SignResponse signResult) throws JOSEException {
        return Base64URL.encode(
                        ECDSA.transcodeSignatureToConcat(
                                signResult.signature().asByteArray(),
                                ECDSA.getSignatureByteArrayLength(SIGNING_ALGORITHM)))
                .toString();
    }

    private Base64URL getEncodedClaims(String proofJwtDidKey, Object documentDetails) {
        Instant now = Instant.now();

        var claimsBuilder =
                new JWTClaimsSet.Builder()
                        .issuer(configurationService.getIssuer())
                        .issueTime(Date.from(now))
                        .notBeforeTime(Date.from(now))
                        .expirationTime(
                                Date.from(
                                        now.plus(
                                                configurationService.getCredentialTtlInSecs(),
                                                ChronoUnit.DAYS)))
                        .subject(proofJwtDidKey)
                        .claim("vc", documentDetails)
                        .claim("context", new String[] {CONTEXT});
        return Base64URL.encode(claimsBuilder.build().toString());
    }

    private Base64URL getEncodedHeader(String keyId) throws NoSuchAlgorithmException {
        String hashedKeyId = KeyHelper.hashKeyId(keyId);
        var jwsHeader =
                new JWSHeader.Builder(SIGNING_ALGORITHM).keyID(hashedKeyId).type(JWT).build();
        return jwsHeader.toBase64URL();
    }
}
