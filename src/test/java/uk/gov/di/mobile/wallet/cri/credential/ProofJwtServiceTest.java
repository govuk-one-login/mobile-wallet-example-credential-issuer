// package uk.gov.di.mobile.wallet.cri.credential;
//
// import com.nimbusds.jose.JOSEException;
// import com.nimbusds.jose.JWSAlgorithm;
// import com.nimbusds.jose.JWSHeader;
// import com.nimbusds.jose.crypto.ECDSASigner;
// import com.nimbusds.jose.crypto.RSASSASigner;
// import com.nimbusds.jose.jwk.ECKey;
// import com.nimbusds.jwt.JWTClaimsSet;
// import com.nimbusds.jwt.SignedJWT;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
//
// import java.security.KeyFactory;
// import java.security.NoSuchAlgorithmException;
// import java.security.interfaces.RSAPrivateKey;
// import java.security.spec.InvalidKeySpecException;
// import java.security.spec.PKCS8EncodedKeySpec;
// import java.text.ParseException;
// import java.time.Instant;
// import java.util.Base64;
// import java.util.Date;
//
// import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.Matchers.*;
// import static org.junit.jupiter.api.Assertions.*;
//
// class ProofJwtServiceTest {
//    private ProofJwtService proofJwtService;
//
//    @BeforeEach
//    void setup() {
//        proofJwtService = new ProofJwtService();
//    }
//
//    @Test
//    void shouldThrowProofJwtValidationExceptionWhenJwtStringCannotBeParsedAsSignedJwt() {
//        ProofJwtValidationException exception =
//                assertThrows(
//                        ProofJwtValidationException.class,
//                        () -> proofJwtService.verifyProofJwt("invalid.jwt.string"));
//        assertThat(exception.getMessage(), containsString("Error parsing proof JWT"));
//    }
//
//    @Test
//    void shouldThrowProofJwtValidationExceptionWhenJwtHeaderAlgDoesNotMatchConfig()
//            throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {
//        SignedJWT signedJWT =
//                new SignedJWT(
//                        new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
//                        new JWTClaimsSet.Builder().build());
//
//        RSASSASigner rsaSigner = new RSASSASigner(getRsaPrivateKey());
//        signedJWT.sign(rsaSigner);
//
//        ProofJwtValidationException exception =
//                assertThrows(
//                        ProofJwtValidationException.class,
//                        () -> proofJwtService.verifyProofJwt(signedJWT.serialize()));
//        assertEquals(
//                "JWT alg header claim [RS256] does not match client config alg [ES256]",
//                exception.getMessage());
//    }
//
//    @Test
//    void shouldThrowProofJwtValidationExceptionWhenJwtHeaderKidIsNull()
//            throws JOSEException, ParseException {
//        SignedJWT signedJWT =
//                new SignedJWT(
//                        new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
//                        new JWTClaimsSet.Builder().build());
//
//        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
//        signedJWT.sign(ecSigner);
//
//        ProofJwtValidationException exception =
//                assertThrows(
//                        ProofJwtValidationException.class,
//                        () -> proofJwtService.verifyProofJwt(signedJWT.serialize()));
//        assertEquals("JWT kid header claim is null", exception.getMessage());
//    }
//
//    @Test
//    void shouldThrowProofJwtValidationExceptionWhenRequiredClaimAreNull()
//            throws JOSEException, ParseException {
//        SignedJWT signedJWT =
//                new SignedJWT(
//                        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID("test-kid-123").build(),
//                        new JWTClaimsSet.Builder().build());
//
//        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
//        signedJWT.sign(ecSigner);
//
//        ProofJwtValidationException exception =
//                assertThrows(
//                        ProofJwtValidationException.class,
//                        () -> proofJwtService.verifyProofJwt(signedJWT.serialize()));
//        assertEquals("JWT missing required claims: [aud, iat, iss, nonce]",
// exception.getMessage());
//    }
//
//    @Test
//    void shouldThrowProofJwtValidationExceptionWhenAudienceClaimDoesNotMatchConfig()
//            throws JOSEException, ParseException {
//        SignedJWT signedJWT =
//                new SignedJWT(
//                        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID("test-kid-123").build(),
//                        new JWTClaimsSet.Builder()
//                                .issueTime(Date.from(Instant.now()))
//                                .issuer("urn:fdc:gov:uk:wallet")
//                                .audience("invalid-audience")
//                                .claim("nonce", "test-nonce")
//                                .build());
//
//        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
//        signedJWT.sign(ecSigner);
//
//        ProofJwtValidationException exception =
//                assertThrows(
//                        ProofJwtValidationException.class,
//                        () -> proofJwtService.verifyProofJwt(signedJWT.serialize()));
//        assertEquals(
//                "JWT aud claim has value [invalid-audience], must be
// [urn:fdc:gov:uk:example-credential-issuer]",
//                exception.getMessage());
//    }
//
//    @Test
//    void shouldThrowProofJwtValidationExceptionWhenAIssuerClaimDoesNotMatchConfig()
//            throws JOSEException, ParseException {
//        SignedJWT signedJWT =
//                new SignedJWT(
//                        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID("test-kid-123").build(),
//                        new JWTClaimsSet.Builder()
//                                .issueTime(Date.from(Instant.now()))
//                                .issuer("invalid-issuer")
//                                .audience("urn:fdc:gov:uk:example-credential-issuer")
//                                .claim("nonce", "test-nonce")
//                                .build());
//
//        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
//        signedJWT.sign(ecSigner);
//
//        ProofJwtValidationException exception =
//                assertThrows(
//                        ProofJwtValidationException.class,
//                        () -> proofJwtService.verifyProofJwt(signedJWT.serialize()));
//        assertEquals(
//                "JWT iss claim has value invalid-issuer, must be urn:fdc:gov:uk:wallet",
//                exception.getMessage());
//    }
//
//    @Test
//    void shouldThrowProofJwtValidationExceptionWhenDidKeyIsInvalid()
//            throws JOSEException, ParseException {
//        SignedJWT signedJWT =
//                new SignedJWT(
//                        new JWSHeader.Builder(JWSAlgorithm.ES256)
//                                .keyID("did:key:notAValidKey")
//                                .build(),
//                        new JWTClaimsSet.Builder()
//                                .issueTime(Date.from(Instant.now()))
//                                .issuer("urn:fdc:gov:uk:wallet")
//                                .audience("urn:fdc:gov:uk:example-credential-issuer")
//                                .claim("nonce", "test-nonce")
//                                .build());
//
//        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
//        signedJWT.sign(ecSigner);
//
//        ProofJwtValidationException exception =
//                assertThrows(
//                        ProofJwtValidationException.class,
//                        () -> proofJwtService.verifyProofJwt(signedJWT.serialize()));
//
//        assertThat(exception.getMessage(), containsString("Error verifying signature"));
//    }
//
//    @Test
//    void shouldThrowProofJwtValidationExceptionWhenSignatureVerificationFailed()
//            throws JOSEException, ParseException {
//        SignedJWT signedJWT =
//                new SignedJWT(
//                        new JWSHeader.Builder(JWSAlgorithm.ES256)
//                                .keyID(
//
// "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==")
//                                .build(),
//                        new JWTClaimsSet.Builder()
//                                .issueTime(Date.from(Instant.now()))
//                                .issuer("urn:fdc:gov:uk:wallet")
//                                .audience("urn:fdc:gov:uk:example-credential-issuer")
//                                .claim("nonce", "test-nonce")
//                                .build());
//
//        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
//        signedJWT.sign(ecSigner);
//
//        ProofJwtValidationException exception =
//                assertThrows(
//                        ProofJwtValidationException.class,
//                        () -> proofJwtService.verifyProofJwt(signedJWT.serialize()));
//
//        assertThat(
//                exception.getMessage(), containsString("Proof JWT signature verification
// failed"));
//    }
//
//    @Test
//    void shouldReturnProofJwtParsedAsSignedJwtWhenVerificationSucceeds()
//            throws JOSEException, ParseException, ProofJwtValidationException {
//        SignedJWT signedJWT =
//                new SignedJWT(
//                        new JWSHeader.Builder(JWSAlgorithm.ES256)
//                                .keyID(
//
// "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEJZJxO7obR8Isv585Esig0bP0AG/oSz08R1+uUpbb/IGbrRDaQvQtuEdKW1wpisujSzXndejH+ZPm9FTODHwyQQ==")
//                                .build(),
//                        new JWTClaimsSet.Builder()
//                                .issueTime(Date.from(Instant.now()))
//                                .issuer("urn:fdc:gov:uk:wallet")
//                                .audience("urn:fdc:gov:uk:example-credential-issuer")
//                                .claim("nonce", "test-nonce")
//                                .build());
//
//        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
//        signedJWT.sign(ecSigner);
//
//        assertEquals(
//                signedJWT.serialize(),
//                proofJwtService.verifyProofJwt(signedJWT.serialize()).serialize());
//    }
//
//    private RSAPrivateKey getRsaPrivateKey()
//            throws InvalidKeySpecException, NoSuchAlgorithmException {
//        String privateKey =
//
// "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC3ZD/jwRxlA/vv43rx9T6ovlczQnsntAjgTbDRTDcsw7TYM1Q3wwD3OPYt5qaKg5qTKaOB90at2jBPih8QH/jDXfSjlU7RlSe8p7fYxaj73ds4ULGAOWaQqmbu+BppcUUPzKII1QBF8Mk2qn/GiVVfqCwDx7uOOT+jPr3H/Wysi+lI8JFYn60wj4oanmbr6iuIVKGWP41STlLu7aLhVM4JxAA2T85Ddot9nPCMftXLBW+QDZUvrbv4CSbtLjuxuqkKdN6FNpSjvx/oSZDDw6jyWydLI68CUQ09Nbm63oY/KpY5V/o+m81Uv2J0Ov+oHCF/0SU8/Nf7kWTK9uTXRW03AgMBAAECggEAE5rV8aUNQgdBAY4R8JfFEQj4DXTH8aCfaksj4dwB8fkh9hLWp/divQsL1jBYEWqsNZs37YbfuWofzAD5/SFN2KTMqEgn2uPVEafkUXof7Hz1GHoX35tDSafNxTIksKz5Mw0vLT6H/vIUsJFdg33e8JDr06Ogez3Hfc4RP7XpzjAgdUoVcEhF4VKFo6r7ImETUWQ4E5mCmWnqKPiqrGrVcwP0xVtz09yeE45eAVX89Sgn4Brxv48TDnKtKYxU2NhEKRJyL2EU0QLjW80tyGl/zzEzhn/QpjJqfKPB5REP41oPBfER/HVHoXDinelcNXYUkBUqbtcKexNI8wPHA5/TcQKBgQDb9UsmJiDHvlaTZS/Ge7+6wcn2zDT/o0S3ZRLzJeWcNLBonohiOt0FBvj/NvttqDGK53yjZZFRfWqJaG2pFJqhY/PLiX4hlt6nAQkzUDxpI08sU0Q4IgEomJUvXmicEJNHR1L4YYyPyv5nDB3fmGsyUTdbeWOFJRIxr25lOS1MNQKBgQDVcRPjsXJSDuRNOo/GnAGZiYlfAvxeXfkIsB45vbfEp+9HcvA7BZZP3b2JbMDIj2JcpYo5+W/Z3uU5nBVU0WjtXZ79uSvV/14oH95z5uAIm6JtIWgVGC31fGyPl8RLgn00fYa6MGXWUo82WnLYDBmC3wa7Xfar/dKVUQBZWGsJOwKBgC3+dOBdSK17146qsfrHFahvrVO2D78E3PGcaQH/AqxPODQoMkyYEm9ird5wGNMtQG7TSPTB4Ekx+H0TIRsh+9OTmv8MmRtc+OHjDZF1TayOfZe/MZyrP6LFhSyKiUVZEfLtryPRAhtvTxMtLXH75S54XSL7lxvYTJ2nGWaBNj+hAoGAf67ujAZp8ibQcla3DcPjvRqm3/ykRjuHL6hT3IzeszkXDjH2/gfgnJR0vxIc3Z3Q5MVuxDGwtK//hpAVvrCrSVv5MaUlURY8GFrAM6uIl/2qlAgpAH1/eNxfASN0HQvJpK32+8jaEvU+kPBYxV+vnzeWCl4yoz7rS8GyKMCY/2MCgYEAtDBPMVHPbjauWjzIjXlaJTMlAoWOmnTB36rkNC9U0SRCxvP6hcLTgvDv1TR2+mKM5tQS2/C1XC3zajUFhIqR0funEEWqUoKfZEWqagOILxpIaeida2MwWXNYLQIAimoMQ6CbqdHsPOrQ/ZtxqlwDUnPYumRawSyxsGIThDnN4oc=";
//        return (RSAPrivateKey)
//                KeyFactory.getInstance("RSA")
//                        .generatePrivate(
//                                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
//    }
//
//    private ECKey getEsPrivateKey() throws ParseException {
//        String privateKeyJwkBase64 =
//
// "eyJrdHkiOiJFQyIsImQiOiI4SGJYN0xib1E1OEpJOGo3eHdfQXp0SlRVSDVpZTFtNktIQlVmX3JnakVrIiwidXNlIjoic2lnIiwiY3J2IjoiUC0yNTYiLCJraWQiOiJmMDYxMWY3Zi04YTI5LTQ3ZTEtYmVhYy1mNWVlNWJhNzQ3MmUiLCJ4IjoiSlpKeE83b2JSOElzdjU4NUVzaWcwYlAwQUdfb1N6MDhSMS11VXBiYl9JRSIsInkiOiJtNjBRMmtMMExiaEhTbHRjS1lyTG8wczE1M1hveF9tVDV2UlV6Z3g4TWtFIiwiaWF0IjoxNzEyMTQ2MTc5fQ==";
//        return ECKey.parse(new String(Base64.getDecoder().decode(privateKeyJwkBase64)));
//    }
// }
