package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private static final long CREDENTIAL_EXPIRY = 1516239022L;
    private static final int STATUS_LIST_INDEX = 5;
    private static final String STATUS_LIST_URI = "https://status-list.test.com/t/12345";

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

    @Nested
    class BuildIssueTokenTests {

        @Test
        void shouldBuildValidIssueToken() throws Exception {
            String result = builder.buildIssueToken(CREDENTIAL_EXPIRY);

            SignedJWT parsedToken = SignedJWT.parse(result);

            Set<String> expectedHeaders = Set.of("kid", "typ", "alg");
            JWSHeader header = parsedToken.getHeader();
            assertEquals(expectedHeaders, header.getIncludedParams());
            assertEquals(KEY_ID_HASH, header.getKeyID());
            assertEquals("JWT", header.getType().toString());
            assertEquals("ES256", header.getAlgorithm().toString());

            Set<String> expectedClaims = Set.of("iss", "iat", "statusExpiry", "jti");
            JWTClaimsSet claimSet = parsedToken.getJWTClaimsSet();
            assertEquals(expectedClaims, claimSet.getClaims().keySet());
            assertEquals(CLIENT_ID, claimSet.getIssuer());
            assertEquals(Date.from(FIXED_INSTANT), claimSet.getIssueTime());
            String jwtId = claimSet.getJWTID();
            assertNotNull(jwtId);
            assertDoesNotThrow(() -> UUID.fromString(jwtId));
            assertEquals(CREDENTIAL_EXPIRY, claimSet.getLongClaim("statusExpiry"));

            assertEquals(TEST_SIGNATURE, parsedToken.getSignature());
        }

        @Test
        void shouldThrowSigningErrorsWhenSigningFails() {
            RuntimeException originalException = new RuntimeException("Signing error");
            when(keyProvider.sign(any())).thenThrow(originalException);

            SigningException exception =
                    assertThrows(
                            SigningException.class,
                            () -> builder.buildIssueToken(CREDENTIAL_EXPIRY));
            assertEquals(
                    "Error signing status list request token: Signing error",
                    exception.getMessage());
            assertEquals(originalException, exception.getCause());
        }

        @Test
        void shouldPropagateExceptionThrownWhenGettingKeyId() {
            Mockito.reset(keyProvider);
            when(keyProvider.getKeyId(KEY_ALIAS)).thenThrow(new RuntimeException("Key not found"));

            assertThrows(RuntimeException.class, () -> builder.buildIssueToken(CREDENTIAL_EXPIRY));
        }
    }

    @Nested
    class BuildRevokeTokenTests {

        @Test
        void shouldBuildValidRevokeToken() throws Exception {
            String result = builder.buildRevokeToken(STATUS_LIST_INDEX, STATUS_LIST_URI);

            SignedJWT parsedToken = SignedJWT.parse(result);

            Set<String> expectedHeaders = Set.of("kid", "typ", "alg");
            JWSHeader header = parsedToken.getHeader();
            assertEquals(expectedHeaders, header.getIncludedParams());
            assertEquals(KEY_ID_HASH, header.getKeyID());
            assertEquals("JWT", header.getType().toString());
            assertEquals("ES256", header.getAlgorithm().toString());

            JWTClaimsSet claimSet = parsedToken.getJWTClaimsSet();
            Set<String> expectedClaims = Set.of("iss", "iat", "uri", "idx", "jti");
            assertEquals(expectedClaims, claimSet.getClaims().keySet());
            assertEquals(CLIENT_ID, claimSet.getIssuer());
            assertEquals(Date.from(FIXED_INSTANT), claimSet.getIssueTime());
            String jwtId = claimSet.getJWTID();
            assertNotNull(jwtId);
            assertDoesNotThrow(() -> UUID.fromString(jwtId));
            assertEquals(STATUS_LIST_URI, claimSet.getStringClaim("uri"));
            assertEquals(STATUS_LIST_INDEX, claimSet.getIntegerClaim("idx"));

            assertEquals(TEST_SIGNATURE, parsedToken.getSignature());
        }

        @Test
        void shouldThrowSigningErrorsWhenSigningFails() {
            RuntimeException originalException = new RuntimeException("Signing error");
            when(keyProvider.sign(any())).thenThrow(originalException);

            SigningException exception =
                    assertThrows(
                            SigningException.class,
                            () -> builder.buildRevokeToken(STATUS_LIST_INDEX, STATUS_LIST_URI));
            assertEquals(
                    "Error signing status list request token: Signing error",
                    exception.getMessage());
            assertEquals(originalException, exception.getCause());
        }

        @Test
        void shouldPropagateExceptionThrownWhenGettingKeyId() {
            Mockito.reset(keyProvider);
            when(keyProvider.getKeyId(KEY_ALIAS)).thenThrow(new RuntimeException("Key not found"));

            assertThrows(
                    RuntimeException.class,
                    () -> builder.buildRevokeToken(STATUS_LIST_INDEX, STATUS_LIST_URI));
        }
    }
}
