package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;

public class TokenService {
    private final TokenSignatureVerificationService tokenSignatureVerificationService;

    public TokenService(TokenSignatureVerificationService tokenSignatureVerificationService) {
        this.tokenSignatureVerificationService = tokenSignatureVerificationService;
    }

    public BearerAccessToken validateAccessToken(String authorizationHeader)
            throws InvalidRequestAuthorizationHeaderException,
                    JsonProcessingException,
                    java.text.ParseException {

        BearerAccessToken accessToken;
        try {
            accessToken = BearerAccessToken.parse(authorizationHeader);
        } catch (ParseException exception) {
            throw new InvalidRequestAuthorizationHeaderException(exception);
        }

        JWTClaimsSet claimsSet;
        try {
            SignedJWT signedAccessToken = SignedJWT.parse(accessToken.getValue());
            claimsSet = signedAccessToken.getJWTClaimsSet();
        } catch (java.text.ParseException exception) {
            throw new InvalidRequestAuthorizationHeaderException(exception);
        }

        JWK isAccessTokenSignatureValid =
                tokenSignatureVerificationService.verifyAccessTokenSignature(accessToken);
        //        if (!isAccessTokenSignatureValid) {
        //            throw new RuntimeException("Unauthorized");
        //        }

        return accessToken;
    }
}
