package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.client.Client;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.BasicCheckCredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialExpiryCalculator;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandlerRegistry;
import uk.gov.di.mobile.wallet.cri.credential.CredentialService;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.DigitalVeteranCardHandler;
import uk.gov.di.mobile.wallet.cri.credential.DocumentStoreClient;
import uk.gov.di.mobile.wallet.cri.credential.MobileDrivingLicenceHandler;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.credential.SocialSecurityCredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MobileDrivingLicenceService;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.JacksonCBOREncoderProvider;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKeyFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESigner;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DigestIDGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItemFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObjectFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.NamespacesFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfoFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValueDigestsFactory;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.credential_offer.PreAuthorizedCodeBuilder;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.iacas.IacasService;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.notification.NotificationService;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.certificate.CertificateProvider;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.S3Service;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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
            throws MalformedURLException, NoSuchAlgorithmException {

        KmsService kmsService = new KmsService(configurationService);
        PreAuthorizedCodeBuilder preAuthorizedCodeBuilder =
                new PreAuthorizedCodeBuilder(configurationService, kmsService);

        CredentialOfferService credentialOfferService =
                new CredentialOfferService(configurationService, preAuthorizedCodeBuilder);

        DynamoDbService dynamoDbService =
                new DynamoDbService(
                        DynamoDbService.getClient(configurationService),
                        configurationService.getCredentialOfferCacheTableName(),
                        configurationService.getCredentialStoreTableName());

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
        ValueDigestsFactory valueDigestsFactory =
                new ValueDigestsFactory(cborEncoder, MessageDigest.getInstance("SHA-256"));
        ValidityInfoFactory validityInfoFactory = new ValidityInfoFactory();
        COSEKeyFactory coseKeyFactory = new COSEKeyFactory();
        MobileSecurityObjectFactory mobileSecurityObjectFactory =
                new MobileSecurityObjectFactory(
                        valueDigestsFactory, validityInfoFactory, coseKeyFactory);
        COSESigner coseSigner =
                new COSESigner(
                        cborEncoder, kmsService, configurationService.getDocumentSigningKey1Arn());
        S3Service s3Service = new S3Service(S3Service.getClient(configurationService));
        CertificateProvider certificateProvider =
                new CertificateProvider(
                        s3Service, configurationService.getCertificatesBucketName());
        NamespacesFactory namespacesFactory = new NamespacesFactory(issuerSignedItemFactory);
        IssuerSignedFactory issuerSignedFactory =
                new IssuerSignedFactory(
                        mobileSecurityObjectFactory,
                        cborEncoder,
                        coseSigner,
                        certificateProvider,
                        configurationService.getDocumentSigningKey1Arn());

        MobileDrivingLicenceService mobileDrivingLicenceService =
                new MobileDrivingLicenceService(
                        cborEncoder, namespacesFactory, issuerSignedFactory);

        DocumentStoreClient documentStoreClient =
                new DocumentStoreClient(configurationService, httpClient);

      CredentialHandler socialSecurityHandler = new SocialSecurityCredentialHandler(new CredentialBuilder<>(configurationService, kmsService));
      CredentialHandler basicCheckHandler = new BasicCheckCredentialHandler(new CredentialBuilder<>(configurationService, kmsService));
      CredentialHandler veteranCardHandler = new DigitalVeteranCardHandler(new CredentialBuilder<>(configurationService, kmsService));
      CredentialHandler mobileDrivingLicenceHandler = new MobileDrivingLicenceHandler(mobileDrivingLicenceService);

      CredentialHandlerRegistry registry = new CredentialHandlerRegistry(
              List.of(
                      socialSecurityHandler,
                      basicCheckHandler,
                      veteranCardHandler,
                      mobileDrivingLicenceHandler
                     )
      );

        CredentialService credentialService =
                new CredentialService(
                        dynamoDbService,
                        accessTokenService,
                        proofJwtService,
                        documentStoreClient,
                        registry,
                        new CredentialExpiryCalculator());

        DidDocumentService didDocumentService =
                new DidDocumentService(configurationService, kmsService);

        NotificationService notificationService =
                new NotificationService(dynamoDbService, accessTokenService);

        IacasService iacasService =
                new IacasService(
                        certificateProvider, configurationService.getCertificateAuthorityArn());

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
