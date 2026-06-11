package testUtils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;
import java.util.List;

public class MockAccessTokenBuilder {

    private static final Instant FIXED_EXPIRY = Instant.parse("2099-01-01T00:00:00Z");

    private final JWSHeader.Builder headerBuilder;
    private final JWTClaimsSet.Builder claimsBuilder =
            new JWTClaimsSet.Builder()
                    .subject(
                            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i")
                    .audience("https://issuer-url.gov.uk")
                    .issuer("https://auth-url.gov.uk")
                    .jwtID("e75b7cc0-c5ef-4075-ad22-3b239b6db89c")
                    .claim(JWTClaimNames.EXPIRATION_TIME, FIXED_EXPIRY.getEpochSecond())
                    .claim("c_nonce", "134e0c41-a8b4-46d4-aec8-cd547e125589")
                    .claim("credential_configuration_ids", List.of("rg.iso.18013.5.1.mDL"));

    public MockAccessTokenBuilder(String algorithm) {
        this.headerBuilder =
                new JWSHeader.Builder(JWSAlgorithm.parse(algorithm))
                        .keyID("cb5a1a8b-809a-4f32-944d-caae1a57ed91")
                        .type(new JOSEObjectType("at+jwt"));
    }

    public MockAccessTokenBuilder withKid(String kid) {
        headerBuilder.keyID(kid);
        return this;
    }

    public MockAccessTokenBuilder withType(JOSEObjectType typ) {
        headerBuilder.type(typ);
        return this;
    }

    public MockAccessTokenBuilder withIssuer(String issuer) {
        claimsBuilder.issuer(issuer);
        return this;
    }

    public MockAccessTokenBuilder withAudience(String audience) {
        claimsBuilder.audience(audience);
        return this;
    }

    public MockAccessTokenBuilder withExpirationTime(Instant expirationTime) {
        claimsBuilder.claim(JWTClaimNames.EXPIRATION_TIME, expirationTime.getEpochSecond());
        return this;
    }

    public MockAccessTokenBuilder withClaim(String key, Object value) {
        claimsBuilder.claim(key, value);
        return this;
    }

    public SignedJWT build() {
        return new SignedJWT(headerBuilder.build(), claimsBuilder.build());
    }
}
