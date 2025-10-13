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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class CredentialOfferResourceTest {
    @Mock private Logger mockLogger;
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String ITEM_ID = "f8f7ae46-749a-4882-a426-2659fb81c9d2";
    private static final String CREDENTIAL_TYPE = "TestCredentialType";
    private static final String KEY_ID = "ff275b92-0def-4dfc-b0f6-87c96b26c6c7";
    private static final KmsService kmsService = mock(KmsService.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private static final CredentialOfferService credentialOfferService =
            mock(CredentialOfferService.class);
    private final DynamoDbService mockDataStore = mock(DynamoDbService.class);
    private final CredentialOfferResource credentialOfferResource =
            new CredentialOfferResource(
                    credentialOfferService, configurationService, mockDataStore) {
                @Override
                protected Logger getLogger() {
                    return mockLogger;
                }
            };
    private final ResourceExtension resource =
            ResourceExtension.builder().addResource(credentialOfferResource).build();

    @BeforeEach
    void setUp() {
        when(configurationService.getWalletDeepLinkUrl())
                .thenReturn("https://mobile.test.account.gov.uk/wallet");
    }

    @Test
    void Should_Return200AndCredentialOfferURL()
            throws JOSEException, DataStoreException, SigningException, NoSuchAlgorithmException {
        SignResponse signResponse = getMockedSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(signResponse);
        CredentialOffer mockCredentialOffer = getMockCredentialOffer();
        when(credentialOfferService.buildCredentialOffer(anyString(), anyString()))
                .thenReturn(mockCredentialOffer);
        UUID mockCredentialOfferId = UUID.fromString("76bb18c0-86c6-446e-884d-37550247d49d");
        MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class);
        mockedUUID.when(UUID::randomUUID).thenReturn(mockCredentialOfferId);

        final Response response =
                resource.target("/credential_offer")
                        .queryParam("walletSubjectId", WALLET_SUBJECT_ID)
                        .queryParam("itemId", ITEM_ID)
                        .queryParam("credentialType", CREDENTIAL_TYPE)
                        .request()
                        .get();

        String expectedCredentialOfferUrl =
                "https://mobile.test.account.gov.uk/wallet/add?credential_offer=%7B%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6ImFiYzEyMyIsImlzcyI6InVybjpmZGM6Z292OnVrOjxITVJDPiIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiOWVlNzQxNjctYzYxZC00ZWE3LWFiZTEtZTI3OGYxMThlYTU1Il0sImV4cCI6MTcxMDIzNjM0NSwiaWF0IjoxNzEwMjM2MDQ1fQ.X89-rmLzo9UhzPe1t857N-0YBLRwQLu2jNYnxjSgAcU87d8wyWbbzML2wM_-rrdG5PyOWcup4-mpuFEI4VsSVA%22%7D%7D%2C%22credential_issuer%22%3A%22https%3A%2F%2Fcredential-issuer.example.com%22%2C%22credential_configuration_ids%22%3A%5B%22TestCredentialType%22%5D%7D";
        assertThat(response.readEntity(String.class), is(expectedCredentialOfferUrl));
        assertThat(response.getStatus(), is(200));
        verify(mockDataStore, times(1)).saveCredentialOffer(any());
        verify(mockLogger)
                .info(
                        "Credential offer saved - walletSubjectId: {}, credentialOfferId: {}, itemId: {}",
                        "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                        "76bb18c0-86c6-446e-884d-37550247d49d",
                        "f8f7ae46-749a-4882-a426-2659fb81c9d2");
        mockedUUID.close();
    }

    @Test
    void Should_Return500_On_SigningException() throws JOSEException, DataStoreException {
        SignResponse signResponse = getMockedSignResponse();
        when(kmsService.sign(any(SignRequest.class))).thenReturn(signResponse);
        doThrow(new DataStoreException("Mock error message", new Exception()))
                .when(mockDataStore)
                .saveCredentialOffer(any());

        final Response response =
                resource.target("/credential_offer")
                        .queryParam("walletSubjectId", WALLET_SUBJECT_ID)
                        .queryParam("itemId", ITEM_ID)
                        .queryParam("credentialType", CREDENTIAL_TYPE)
                        .request()
                        .get();

        verify(mockDataStore, times(1)).saveCredentialOffer(any());
        assertThat(response.getStatus(), is(500));
    }

    @Test
    void Should_Return400_When_WalletSubjectIdIsInvalid() {
        final Response response =
                resource.target("/credential_offer")
                        .queryParam("walletSubjectId", "123")
                        .queryParam("itemId", ITEM_ID)
                        .queryParam("credentialType", CREDENTIAL_TYPE)
                        .request()
                        .get();
        assertThat(response.getStatus(), is(400));
    }

    @Test
    void Should_Return400_When_ItemIdIsInvalid() {
        final Response response =
                resource.target("/credential_offer")
                        .queryParam("walletSubjectId", WALLET_SUBJECT_ID)
                        .queryParam("itemId", "&&^")
                        .queryParam("credentialType", CREDENTIAL_TYPE)
                        .request()
                        .get();

        assertThat(response.getStatus(), is(400));
    }

    @Test
    @DisplayName("Should return 400 when a credentialType is not a valid value")
    void Should_Return400_When_CredentialTypeIsInvalid() {
        final Response response =
                resource.target("/credential_offer")
                        .queryParam("walletSubjectId", WALLET_SUBJECT_ID)
                        .queryParam("itemId", ITEM_ID)
                        .queryParam("credentialType", "???")
                        .request()
                        .get();
        assertThat(response.getStatus(), is(400));
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
                        .keyID(KEY_ID)
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
                .keyId(KEY_ID)
                .build();
    }
}
