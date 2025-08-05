package testUtils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public class MockAccessTokenBuilder {

    private final JWSHeader.Builder headerBuilder;
    private final JWTClaimsSet.Builder claimsBuilder =
            new JWTClaimsSet.Builder()
                    .issueTime(Date.from(Instant.now()))
                    .subject(
                            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i")
                    .audience("https://issuer-url.gov.uk")
                    .issuer("https://auth-url.gov.uk")
                    .claim("c_nonce", "134e0c41-a8b4-46d4-aec8-cd547e125589")
                    .claim(
                            "credential_identifiers",
                            List.of("efb52887-48d6-43b7-b14c-da7896fbf54d"));

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

    public MockAccessTokenBuilder withClaim(String key, Object value) {
        claimsBuilder.claim(key, value);
        return this;
    }

    public SignedJWT build() {
        return new SignedJWT(headerBuilder.build(), claimsBuilder.build());
    }
}
