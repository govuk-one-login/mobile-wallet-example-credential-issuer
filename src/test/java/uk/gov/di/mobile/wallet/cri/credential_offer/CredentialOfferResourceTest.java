package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
public class CredentialOfferResourceTest {

    private static final String WALLET_SUBJECT_ID = "mock-wallet-subject-id";
    private static final String DOCUMENT_ID = "mock-document-id";
    private static final String CREDENTIAL_TYPE = "TestCredentialType";
    private static final KmsService kmsService = mock(KmsService.class);
    private static final ConfigurationService configurationService = new ConfigurationService();
    private static final CredentialOfferService credentialOfferService =
            mock(CredentialOfferService.class);
    private final DynamoDbService mockDataStore = mock(DynamoDbService.class);
    private final ResourceExtension EXT =
            ResourceExtension.builder()
                    .addResource(
                            new CredentialOfferResource(
                                    credentialOfferService, configurationService, mockDataStore))
                    .build();

    @Test
    @DisplayName("Should return 200 and the URL encoded credential offer")
    void testItReturns200AndUrlEncodedCredentialOffer()
            throws JOSEException, DataStoreException, SigningException {
        SignResponse signResponse = getMockedSignResponse();
        CredentialOffer mockCredentialOffer = getMockCredentialOffer();

        // when(kmsService.signPreAuthorizedCode(any(SignRequest.class))).thenReturn(signResponse);
        // when(credentialOfferService.buildCredentialOffer(anyString(), anyString()))
        //         .thenReturn(mockCredentialOffer);

// =======
        when(kmsService.sign(any(SignRequest.class))).thenReturn(signResponse);
// >>>>>>> 794304b (test(DCMAW-8412): test CredentialBuilder class)
        doThrow(new RuntimeException("Mock error message"))
                .when(mockDataStore)
                .saveCredentialOffer(new CredentialOfferCacheItem());

        final Response response =
                EXT.target("/credential_offer")
                        .queryParam("walletSubjectId", WALLET_SUBJECT_ID)
                        .queryParam("documentId", DOCUMENT_ID)
                        .queryParam("credentialType", CREDENTIAL_TYPE)
                        .request()
                        .get();

        String expectedCredentialOfferString =
                "{\"credential_offer_uri\":\"https://mobile.account.gov.uk/wallet/add?credential_offer=%7B%22credentials%22%3A%5B%22TestCredentialType%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6ImFiYzEyMyIsImlzcyI6InVybjpmZGM6Z292OnVrOjxITVJDPiIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiOWVlNzQxNjctYzYxZC00ZWE3LWFiZTEtZTI3OGYxMThlYTU1Il0sImV4cCI6MTcxMDIzNjM0NSwiaWF0IjoxNzEwMjM2MDQ1fQ.X89-rmLzo9UhzPe1t857N-0YBLRwQLu2jNYnxjSgAcU87d8wyWbbzML2wM_-rrdG5PyOWcup4-mpuFEI4VsSVA%22%7D%7D%2C%22credentialIssuer%22%3A%22https%3A%2F%2Fcredential-issuer.example.com%22%7D\"}";

        Mockito.verify(mockDataStore, Mockito.times(1)).saveCredentialOffer(any());
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(expectedCredentialOfferString));
    }

    @Test
    @DisplayName("Should return 500 when a DataStoreException is caught")
    void testItReturns500() throws JOSEException, DataStoreException {
        SignResponse signResponse = getMockedSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(signResponse);
        doThrow(new DataStoreException("Mock error message", new Exception()))
                .when(mockDataStore)
                .saveCredentialOffer(any());

        final Response response =
                EXT.target("/credential_offer")
                        .queryParam("walletSubjectId", WALLET_SUBJECT_ID)
                        .queryParam("documentId", DOCUMENT_ID)
                        .queryParam("credentialType", CREDENTIAL_TYPE)
                        .request()
                        .get();

        Mockito.verify(mockDataStore, Mockito.times(1)).saveCredentialOffer(any());
        assertThat(response.getStatus(), is(500));
    }

    private CredentialOffer getMockCredentialOffer() {
        String mockSignedJwt =
                "eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6ImFiYzEyMyIsImlzcyI6InVybjpmZGM6Z292OnVrOjxITVJDPiIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiOWVlNzQxNjctYzYxZC00ZWE3LWFiZTEtZTI3OGYxMThlYTU1Il0sImV4cCI6MTcxMDIzNjM0NSwiaWF0IjoxNzEwMjM2MDQ1fQ.X89-rmLzo9UhzPe1t857N-0YBLRwQLu2jNYnxjSgAcU87d8wyWbbzML2wM_-rrdG5PyOWcup4-mpuFEI4VsSVA";
        Map<String, Map<String, String>> mockGrantsMap = new HashMap<>();
        Map<String, String> mockPreAuthorizedCodeMap = new HashMap<>();
        mockPreAuthorizedCodeMap.put("pre-authorized_code", mockSignedJwt);
        mockGrantsMap.put(
                "urn:ietf:params:oauth:grant-type:pre-authorized_code", mockPreAuthorizedCodeMap);

        return new CredentialOffer(
                "https://credential-issuer.example.com", "TestCredentialType", mockGrantsMap);
    }

    private SignResponse getMockedSignResponse() throws JOSEException {
        var signingKey =
                new ECKeyGenerator(Curve.P_256)
                        .keyID(configurationService.getSigningKeyId())
                        .algorithm(JWSAlgorithm.ES256)
                        .generate();
        var ecdsaSigner = new ECDSASigner(signingKey);
        var jwtClaimsSet = new JWTClaimsSet.Builder().build();
        var jwsHeader = new JWSHeader(JWSAlgorithm.ES256);
        var signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
        signedJWT.sign(ecdsaSigner);
        byte[] derSignature = ECDSA.transcodeSignatureToDER(signedJWT.getSignature().decode());

        return SignResponse.builder()
                .signature(SdkBytes.fromByteArray(derSignature))
                .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                .keyId(configurationService.getSigningKeyId())
                .build();
    }
}
