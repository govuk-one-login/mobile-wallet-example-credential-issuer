package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.core.setup.Environment;
import uk.gov.di.mobile.wallet.cri.credential.CredentialResource;
import uk.gov.di.mobile.wallet.cri.credential.CredentialService;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferResource;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentResource;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.healthcheck.HealthCheckResource;
import uk.gov.di.mobile.wallet.cri.healthcheck.Ping;
import uk.gov.di.mobile.wallet.cri.jwks.JwksResource;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataResource;
import uk.gov.di.mobile.wallet.cri.notification.NotificationResource;
import uk.gov.di.mobile.wallet.cri.notification.NotificationService;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;

public class ResourceRegistrar {
    public static void registerResources(
            Environment environment,
            CredentialOfferService credentialOfferService,
            ConfigurationService configurationService,
            DynamoDbService dynamoDbService,
            MetadataBuilder metadataBuilder,
            CredentialService credentialService,
            DidDocumentService didDocumentService,
            JwksService jwksService,
            NotificationService notificationService) {
        environment.healthChecks().register("ping", new Ping());
        environment.jersey().register(new HealthCheckResource(environment));
        environment
                .jersey()
                .register(
                        new CredentialOfferResource(
                                credentialOfferService, configurationService, dynamoDbService));
        environment.jersey().register(new MetadataResource(configurationService, metadataBuilder));
        environment.jersey().register(new CredentialResource(credentialService));
        environment.jersey().register(new DidDocumentResource(didDocumentService));
        environment.jersey().register(new JwksResource(jwksService));
        environment.jersey().register(new NotificationResource(notificationService));
    }
}
