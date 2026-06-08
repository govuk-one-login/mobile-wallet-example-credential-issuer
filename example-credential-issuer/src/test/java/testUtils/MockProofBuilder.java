package testUtils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;

public class MockProofBuilder {

    private static final Instant FIXED_ISSUE_TIME = Instant.parse("2024-01-15T10:30:00Z");

    private final JWSHeader.Builder headerBuilder;
    private final JWTClaimsSet.Builder claimsBuilder =
            new JWTClaimsSet.Builder()
                    .claim(JWTClaimNames.ISSUED_AT, FIXED_ISSUE_TIME.getEpochSecond())
                    .audience("https://issuer-url.gov.uk")
                    .issuer("urn:fdc:gov:uk:wallet")
                    .claim("nonce", "134e0c41-a8b4-46d4-aec8-cd547e125589");

    public MockProofBuilder(String algorithm) {
        this.headerBuilder =
                new JWSHeader.Builder(JWSAlgorithm.parse(algorithm))
                        .keyID("did:key:zDnaeUqPxbNEqiYDMyo6EHt9XxpQcE2arUVgkZyfwA6G5Xacf")
                        .type(new JOSEObjectType("openid4vci-proof+jwt"));
    }

    public MockProofBuilder withKid(String kid) {
        headerBuilder.keyID(kid);
        return this;
    }

    public MockProofBuilder withType(JOSEObjectType typ) {
        headerBuilder.type(typ);
        return this;
    }

    public MockProofBuilder withIssuer(String issuer) {
        claimsBuilder.issuer(issuer);
        return this;
    }

    public MockProofBuilder withAudience(String audience) {
        claimsBuilder.audience(audience);
        return this;
    }

    public SignedJWT build() {
        return new SignedJWT(headerBuilder.build(), claimsBuilder.build());
    }
}
