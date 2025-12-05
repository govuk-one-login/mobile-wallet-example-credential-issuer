package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.CredentialResource;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferResource;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentResource;
import uk.gov.di.mobile.wallet.cri.healthcheck.HealthCheckResource;
import uk.gov.di.mobile.wallet.cri.healthcheck.Ping;
import uk.gov.di.mobile.wallet.cri.iacas.IacasResource;
import uk.gov.di.mobile.wallet.cri.jwks.JwksResource;
import uk.gov.di.mobile.wallet.cri.logo.LogoResource;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataResource;
import uk.gov.di.mobile.wallet.cri.notification.NotificationResource;
import uk.gov.di.mobile.wallet.cri.revoke.RevokeResource;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

/**
 * Main application class for the Example CRI.
 *
 * <p>Initializes and runs the Dropwizard application, configuring services and registering
 * resources.
 */
@ExcludeFromGeneratedCoverageReport
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

    /**
     * Runs the application, creating and registering all services and resources.
     *
     * @param configurationService The application's configuration.
     * @param environment The Dropwizard environment.
     * @throws MalformedURLException If a URL used in service initialization is malformed.
     */
    @Override
    public void run(final ConfigurationService configurationService, final Environment environment)
            throws MalformedURLException, NoSuchAlgorithmException {

        Services services = ServicesFactory.create(configurationService, environment);

        environment.healthChecks().register("ping", new Ping());
        environment.jersey().register(new HealthCheckResource(environment));
        environment
                .jersey()
                .register(
                        new CredentialOfferResource(
                                services.getCredentialOfferService(),
                                configurationService,
                                services.getDynamoDbService()));
        environment
                .jersey()
                .register(
                        new MetadataResource(configurationService, services.getMetadataBuilder()));
        environment.jersey().register(new CredentialResource(services.getCredentialService()));
        environment.jersey().register(new DidDocumentResource(services.getDidDocumentService()));
        environment.jersey().register(new JwksResource(services.getJwksService()));
        environment.jersey().register(new NotificationResource(services.getNotificationService()));
        environment.jersey().register(new IacasResource(services.getIacasService()));
        environment.jersey().register(new RevokeResource(services.getRevokeService()));
        environment.jersey().register(new LogoResource());
    }
}
