package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.client.Client;
import uk.gov.di.mobile.wallet.cri.credential.*;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferResource;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentResource;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.healthcheck.HealthCheckResource;
import uk.gov.di.mobile.wallet.cri.healthcheck.Ping;
import uk.gov.di.mobile.wallet.cri.jwks.JwksResource;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataResource;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

import java.net.MalformedURLException;

public class ExampleCriApp extends Application<ConfigurationService> {

    public static void main(String[] args) throws Exception {
        new ExampleCriApp().run(args);
    }

    @Override
    public void initialize(final Bootstrap<ConfigurationService> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(final ConfigurationService configurationService, final Environment environment)
            throws MalformedURLException {

        KmsService kmsService = new KmsService(configurationService);

        CredentialOfferService credentialOfferService =
                new CredentialOfferService(configurationService, kmsService);

        DynamoDbService dynamoDbService =
                new DynamoDbService(
                        DynamoDbService.getClient(configurationService),
                        configurationService.getCredentialOfferCacheTableName(),
                        configurationService.getCredentialOfferTtlInSecs());

        MetadataBuilder metadataBuilder = new MetadataBuilder();
        Client httpClient =
                new JerseyClientBuilder(environment)
                        .using(new JerseyClientConfiguration())
                        .build("example-cri");

        JwksService jwksService = new JwksService(configurationService, kmsService);
        AccessTokenService accessTokenService =
                new AccessTokenService(jwksService, configurationService);
        ProofJwtService proofJwtService = new ProofJwtService(configurationService);
        CredentialBuilder credentialBuilder =
                new CredentialBuilder(configurationService, kmsService);

        CredentialService credentialService =
                new CredentialService(
                        configurationService,
                        dynamoDbService,
                        accessTokenService,
                        proofJwtService,
                        httpClient,
                        credentialBuilder);

        DidDocumentService didDocumentService =
                new DidDocumentService(configurationService, kmsService);

        environment.healthChecks().register("ping", new Ping());
        environment.jersey().register(new HealthCheckResource());

        environment
                .jersey()
                .register(
                        new CredentialOfferResource(
                                credentialOfferService, configurationService, dynamoDbService));

        environment.jersey().register(new MetadataResource(configurationService, metadataBuilder));

        environment.jersey().register(new CredentialResource(credentialService));

        environment.jersey().register(new DidDocumentResource(didDocumentService));

        environment.jersey().register(new JwksResource(jwksService));
    }
}
