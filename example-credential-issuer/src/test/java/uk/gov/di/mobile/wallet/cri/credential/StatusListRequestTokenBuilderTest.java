package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignResponse;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusListRequestTokenBuilderTest {

    @Mock private ConfigurationService configurationService;
    @Mock private KeyProvider keyProvider;

    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:30:00Z");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, UTC_ZONE);

    private static final String KEY_ALIAS = "keyAlias";
    private static final String CLIENT_ID = "statusListClientId";
    private static final String KEY_ID = "key123";
    private static final String KEY_ID_HASH =
            "8fefe692f690a3173176ecdff4318225afaeb97fdd6f60c866ed823d59221665";
    private static final Base64URL TEST_SIGNATURE =
            new Base64URL(
                    "DtEhU3ljbEg8L38VWAfUAqOyKAM6-Xx-F4GawxaepmXFCgfTjDxw5djxLa8ISlSApmWQxfKTUJqPP3-Kg6NU1Q");

    private StatusListRequestTokenBuilder builder;

    @BeforeEach
    void setUp() throws JOSEException {
        builder = new StatusListRequestTokenBuilder(configurationService, keyProvider, FIXED_CLOCK);
        when(configurationService.getSigningKeyAlias()).thenReturn(KEY_ALIAS);
        when(configurationService.getStatusListClientId()).thenReturn(CLIENT_ID);
        when(keyProvider.getKeyId(KEY_ALIAS)).thenReturn(KEY_ID);

        byte[] signatureToDER = ECDSA.transcodeSignatureToDER(TEST_SIGNATURE.decode());
        when(keyProvider.sign(any()))
                .thenReturn(
                        SignResponse.builder()
                                .signature(SdkBytes.fromByteArray(signatureToDER))
                                .build());
    }

    @Test
    void Should_BuildStatusListIssueToken() throws Exception {
        long credentialExpiry = 1516239022;

        String result = builder.buildIssueToken(credentialExpiry);

        SignedJWT parsedToken = SignedJWT.parse(result);
        JWTClaimsSet claimSet = parsedToken.getJWTClaimsSet();
        JWSHeader header = parsedToken.getHeader();
        Base64URL signature = parsedToken.getSignature();

        Set<String> expectedHeaders = Set.of("kid", "typ", "alg");
        assertEquals(expectedHeaders, header.getIncludedParams());
        assertEquals(KEY_ID_HASH, header.getKeyID());
        assertEquals("JWT", header.getType().toString());
        assertEquals("ES256", header.getAlgorithm().toString());

        Set<String> expectedClaims = Set.of("iss", "iat", "statusExpiry", "jti");
        assertEquals(expectedClaims, claimSet.getClaims().keySet());
        assertEquals(CLIENT_ID, claimSet.getIssuer());
        assertEquals(claimSet.getIssueTime(), Date.from(FIXED_INSTANT));
        assertEquals(credentialExpiry, claimSet.getLongClaim("statusExpiry"));
        String jwtId = claimSet.getJWTID();
        assertNotNull(jwtId);
        assertDoesNotThrow(() -> UUID.fromString(jwtId));

        assertEquals(TEST_SIGNATURE, signature);
    }

    @Test
    void Should_PropagateSigningErrors_As_SigningException() {
        RuntimeException originalException = new RuntimeException("Some signing error");
        when(keyProvider.sign(any())).thenThrow(originalException);

        SigningException exception =
                assertThrows(SigningException.class, () -> builder.buildIssueToken(1516239022L));

        assertEquals(
                "Error signing status list request token: Some signing error",
                exception.getMessage());
        assertEquals(exception.getCause(), originalException);
    }
}
