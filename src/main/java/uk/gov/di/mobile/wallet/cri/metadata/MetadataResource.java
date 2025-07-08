package uk.gov.di.mobile.wallet.cri.metadata;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.responses.ResponseUtil;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

@Singleton
@Path("/.well-known/openid-credential-issuer")
public class MetadataResource {

    private static final String STAGING = "staging";
    private static final String BUILD = "build";
    private static final String CREDENTIAL_ENDPOINT = "/credential";
    private static final String NOTIFICATION_ENDPOINT = "/notification";
    private static final String IACAS_ENDPOINT = "/iacas";
    private static final String CREDENTIAL_CONFIGURATION_SUPPORTED_FILE_NAME =
            "credential_configurations_supported.json";
    private final ConfigurationService configurationService;
    private final MetadataBuilder metadataBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataResource.class);

    public MetadataResource(
            ConfigurationService configurationService, MetadataBuilder metadataBuilder) {
        this.configurationService = configurationService;
        this.metadataBuilder = metadataBuilder;
    }

    @GET
    public Response getMetadata() {
        try {
            String selfUrl = configurationService.getSelfUrl();
            String iacasEndpoint = getIacasEndpoint(selfUrl);

            Metadata metadata =
                    metadataBuilder
                            .setCredentialIssuer(selfUrl)
                            .setCredentialEndpoint(selfUrl + CREDENTIAL_ENDPOINT)
                            .setAuthorizationServers(
                                    configurationService.getOneLoginAuthServerUrl())
                            .setNotificationEndpoint(selfUrl + NOTIFICATION_ENDPOINT)
                            .setIacasEndpoint(iacasEndpoint)
                            .setCredentialConfigurationsSupported(
                                    CREDENTIAL_CONFIGURATION_SUPPORTED_FILE_NAME)
                            .build();
            return ResponseUtil.ok(metadata, true);
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to get the metadata: ", exception);
            return ResponseUtil.internalServerError();
        }
    }

    private @NotNull String getIacasEndpoint(String selfUrl) {
        String iacasEndpoint;
        // The Private Certificate Authority is not deployed to the staging environment,
        // only to dev and build environments. When running in staging, the Example CRI points
        // to the build instance of the Private Certificate Authority, hence the IACAs endpoint
        // must point to build as well.
        if (isStaging() && selfUrl.contains(STAGING)) {
            String buildUrl = selfUrl.replace(STAGING, BUILD);
            iacasEndpoint = buildUrl + IACAS_ENDPOINT;
        } else {
            iacasEndpoint = selfUrl + IACAS_ENDPOINT;
        }
        return iacasEndpoint;
    }

    /**
     * Determines if the application is running in the staging environment. This is used to handle
     * the case where the Private Certificate Authority is not deployed to staging and the Example
     * CRI must point to the build instance instead.
     *
     * @return true if running in staging environment, false otherwise
     */
    private boolean isStaging() {
        String environment = configurationService.getEnvironment();
        return "staging".equals(environment);
    }
}
