package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

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
            throws MalformedURLException, NoSuchAlgorithmException {

        Services services = ServicesFactory.create(configurationService, environment);

        ResourceRegistrar.registerResources(
                environment,
                services.getCredentialOfferService(),
                configurationService,
                services.getDynamoDbService(),
                services.getMetadataBuilder(),
                services.getCredentialService(),
                services.getDidDocumentService(),
                services.getJwksService(),
                services.getNotificationService());
    }
}
