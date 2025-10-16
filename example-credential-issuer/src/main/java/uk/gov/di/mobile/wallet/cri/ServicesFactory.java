package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.client.Client;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandlerFactory;
import uk.gov.di.mobile.wallet.cri.credential.CredentialService;
import uk.gov.di.mobile.wallet.cri.credential.DocumentStoreClient;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.StatusListRequestTokenBuilder;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MobileDrivingLicenceBuilder;
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
import uk.gov.di.mobile.wallet.cri.credential.util.CredentialExpiryCalculator;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.credential_offer.PreAuthorizedCodeBuilder;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.iacas.IacasService;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.notification.NotificationService;
import uk.gov.di.mobile.wallet.cri.revoke.RevokeService;
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
                        .using(configurationService.getHttpClient())
                        .build("example-cri");

        JwksService jwksService = new JwksService(configurationService, kmsService);
        AccessTokenService accessTokenService =
                new AccessTokenService(jwksService, configurationService);
        ProofJwtService proofJwtService = new ProofJwtService(configurationService);

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

        DocumentStoreClient documentStoreClient =
                new DocumentStoreClient(configurationService, httpClient);

        CredentialBuilder<BasicCheckCredentialSubject> basicCheckCredentialBuilder =
                new CredentialBuilder<>(configurationService, kmsService);
        CredentialBuilder<SocialSecurityCredentialSubject> socialSecurityCredentialBuilder =
                new CredentialBuilder<>(configurationService, kmsService);
        CredentialBuilder<VeteranCardCredentialSubject> digitalVeteranCardCredentialBuilder =
                new CredentialBuilder<>(configurationService, kmsService);

        MobileDrivingLicenceBuilder mobileDrivingLicenceBuilder =
                new MobileDrivingLicenceBuilder(
                        cborEncoder, namespacesFactory, issuerSignedFactory);

        CredentialHandlerFactory credentialHandlerFactory =
                new CredentialHandlerFactory(
                        basicCheckCredentialBuilder,
                        socialSecurityCredentialBuilder,
                        digitalVeteranCardCredentialBuilder,
                        mobileDrivingLicenceBuilder);

        StatusListRequestTokenBuilder statusListRequestTokenBuilder =
                new StatusListRequestTokenBuilder(configurationService, kmsService);
        StatusListClient statusListClient =
                new StatusListClient(
                        configurationService, httpClient, statusListRequestTokenBuilder);

        CredentialService credentialService =
                new CredentialService(
                        dynamoDbService,
                        accessTokenService,
                        proofJwtService,
                        documentStoreClient,
                        credentialHandlerFactory,
                        new CredentialExpiryCalculator(),
                        statusListClient,
                        configurationService.getEnvironment());

        DidDocumentService didDocumentService =
                new DidDocumentService(configurationService, kmsService);

        NotificationService notificationService =
                new NotificationService(dynamoDbService, accessTokenService);

        IacasService iacasService =
                new IacasService(
                        certificateProvider, configurationService.getCertificateAuthorityArn());

        RevokeService revokeService = new RevokeService(dynamoDbService, statusListClient);

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
                .revokeService(revokeService)
                .build();
    }
}
