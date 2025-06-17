package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.client.Client;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.*;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MobileDrivingLicenceService;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.JacksonCBOREncoderProvider;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.*;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.credential_offer.PreAuthorizedCodeBuilder;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.iacas.IacasService;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.notification.NotificationService;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.S3Service;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

import java.net.MalformedURLException;

/**
 * Factory for creating and wiring all application services.
 *
 * <p>Example usage: {@code Services services = ServicesFactory.create(configService, environment);}
 */
@ExcludeFromGeneratedCoverageReport
public class ServicesFactory {

    private ServicesFactory() {
        // Should never be instantiated
    }

    /**
     * Creates and configures all application services.
     *
     * @param configurationService The application's configuration.
     * @param environment The application's environment.
     * @return A {@link Services} instance containing all initialized services.
     * @throws MalformedURLException If a URL used in service initialization is malformed.
     */
    public static Services create(
            ConfigurationService configurationService, Environment environment)
            throws MalformedURLException {

        KmsService kmsService = new KmsService(configurationService);
        PreAuthorizedCodeBuilder preAuthorizedCodeBuilder =
                new PreAuthorizedCodeBuilder(configurationService, kmsService);

        CredentialOfferService credentialOfferService =
                new CredentialOfferService(configurationService, preAuthorizedCodeBuilder);

        DynamoDbService dynamoDbService =
                new DynamoDbService(
                        DynamoDbService.getClient(configurationService),
                        configurationService.getCredentialOfferCacheTableName());

        MetadataBuilder metadataBuilder = new MetadataBuilder();

        Client httpClient =
                new JerseyClientBuilder(environment)
                        .using(new JerseyClientConfiguration())
                        .build("example-cri");

        JwksService jwksService = new JwksService(configurationService, kmsService);
        AccessTokenService accessTokenService =
                new AccessTokenService(jwksService, configurationService);
        ProofJwtService proofJwtService = new ProofJwtService(configurationService);
        CredentialBuilder<? extends CredentialSubject> credentialBuilder =
                new CredentialBuilder<>(configurationService, kmsService);

        CBOREncoder cborEncoder =
                new CBOREncoder(JacksonCBOREncoderProvider.configuredCBORMapper());
        IssuerSignedItemFactory issuerSignedItemFactory =
                new IssuerSignedItemFactory(new DigestIDGenerator());
        DocumentFactory documentFactory = new DocumentFactory();
        NamespaceFactory namespaceFactory =
                new NamespaceFactory(issuerSignedItemFactory, cborEncoder);

        MobileDrivingLicenceService mobileDrivingLicenceService =
                new MobileDrivingLicenceService(cborEncoder, documentFactory, namespaceFactory);

        DocumentStoreClient documentStoreClient =
                new DocumentStoreClient(configurationService, httpClient);

        CredentialService credentialService =
                new CredentialService(
                        dynamoDbService,
                        accessTokenService,
                        proofJwtService,
                        documentStoreClient,
                        credentialBuilder,
                        mobileDrivingLicenceService);

        DidDocumentService didDocumentService =
                new DidDocumentService(configurationService, kmsService);

        NotificationService notificationService =
                new NotificationService(dynamoDbService, accessTokenService);

        S3Service s3Service = new S3Service(S3Service.getClient(configurationService));

        IacasService iacasService = new IacasService(configurationService, s3Service);

        return new Services.Builder()
                .kmsService(kmsService)
                .dynamoDbService(dynamoDbService)
                .preAuthorizedCodeBuilder(preAuthorizedCodeBuilder)
                .credentialOfferService(credentialOfferService)
                .metadataBuilder(metadataBuilder)
                .credentialService(credentialService)
                .didDocumentService(didDocumentService)
                .jwksService(jwksService)
                .notificationService(notificationService)
                .iacasService(iacasService)
                .build();
    }
}
