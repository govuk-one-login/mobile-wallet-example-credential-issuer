package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

import java.text.ParseException;

public class CredentialService {
    private final ConfigurationService configurationService;
    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;

    public CredentialService(
            ConfigurationService configurationService,
            DataStore dataStore,
            AccessTokenService accessTokenService) {
        this.configurationService = configurationService;
        this.dataStore = dataStore;
        this.accessTokenService = accessTokenService;
    }

    public String buildCredential(String authorizationHeader, Object requestBody)
            throws AccessTokenValidationException, ParseException, DataStoreException {

        SignedJWT accessToken = accessTokenService.verifyAccessToken(authorizationHeader);

        String tokenCredentialIdentifier = getCredentialIdentifier(accessToken);
        System.out.println("tokenCredentialIdentifier " + tokenCredentialIdentifier);
        CredentialOfferCacheItem credentialOffer =
                dataStore.getCredentialOffer(tokenCredentialIdentifier);

        String tokenWalletSubjectId = getWalletSubjectId(accessToken);
        System.out.println("tokenWalletSubjectId " + tokenWalletSubjectId);
        if (!credentialOffer.getWalletSubjectId().equals(tokenWalletSubjectId)) {
            throw new AccessTokenValidationException(
                    "JWT sub claim does not match credential offer walletSubjectId");
        }

        //        Credential credential = new Credential();

        return "Credential";
    }

    private static String getCredentialIdentifier(SignedJWT accessToken) throws ParseException {
        JWTClaimsSet jwtClaimsSet = accessToken.getJWTClaimsSet();
        return (String) jwtClaimsSet.getListClaim("credential_identifiers").get(0);
    }

    private static String getWalletSubjectId(SignedJWT accessToken) throws ParseException {
        JWTClaimsSet jwtClaimsSet = accessToken.getJWTClaimsSet();
        return (String) jwtClaimsSet.getClaim("sub");
    }
}
