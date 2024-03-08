package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import uk.gov.di.mobile.wallet.cri.resources.CredentialOfferResource;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.services.KmsService;

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

        environment
                .jersey()
                .register(
                        new CredentialOfferResource(credentialOfferService, configurationService));
    }
}
