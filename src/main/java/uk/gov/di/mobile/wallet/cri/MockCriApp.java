package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.client.Client;
import uk.gov.di.mobile.wallet.cri.credential.CredentialResource;
import uk.gov.di.mobile.wallet.cri.credential.CredentialService;
import uk.gov.di.mobile.wallet.cri.credential.TokenService;
import uk.gov.di.mobile.wallet.cri.credential.TokenSignatureVerificationService;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferResource;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataResource;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

public class MockCriApp extends Application<ConfigurationService> {

    public static void main(String[] args) throws Exception {
        new MockCriApp().run(args);
    }

    @Override
    public void initialize(final Bootstrap<ConfigurationService> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(
            final ConfigurationService configurationService, final Environment environment) {

        KmsService kmsService = new KmsService(configurationService);

        CredentialOfferService credentialOfferService =
                new CredentialOfferService(configurationService, kmsService);

        DynamoDbService dynamoDbService =
                new DynamoDbService(
                        DynamoDbService.getClient(configurationService),
                        configurationService.getCriCacheTableName());

        MetadataBuilder metadataBuilder = new MetadataBuilder();

        CredentialService credentialService = new CredentialService();

        Client client =
                new JerseyClientBuilder(environment)
                        .using(new JerseyClientConfiguration())
                        .build("test");

        TokenSignatureVerificationService tokenSignatureVerificationService =
                new TokenSignatureVerificationService(client);
        TokenService tokenService = new TokenService(tokenSignatureVerificationService);

        environment
                .jersey()
                .register(
                        new CredentialOfferResource(
                                credentialOfferService, configurationService, dynamoDbService));

        environment.jersey().register(new MetadataResource(configurationService, metadataBuilder));

        environment
                .jersey()
                .register(
                        new CredentialResource(
                                credentialService,
                                configurationService,
                                dynamoDbService,
                                tokenService));
    }
}
