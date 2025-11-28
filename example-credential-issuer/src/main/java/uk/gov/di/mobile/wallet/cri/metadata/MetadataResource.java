package uk.gov.di.mobile.wallet.cri.metadata;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataResource.class);

    private final ConfigurationService configurationService;
    private final MetadataBuilder metadataBuilder;

    /**
     * Creates a new MetadataResource instance.
     *
     * @param configurationService Service for accessing application configuration.
     * @param metadataBuilder Builder for constructing metadata responses.
     */
    public MetadataResource(
            ConfigurationService configurationService, MetadataBuilder metadataBuilder) {
        this.configurationService = configurationService;
        this.metadataBuilder = metadataBuilder;
    }

    /**
     * Returns the OID4VCI credential issuer metadata.
     *
     * <p>This endpoint provides metadata about the credential issuer including: - Credential issuer
     * identifier - Authorization server URLs - Service endpoints (credential, notification, IACAs)
     * - Supported credential configurations
     *
     * @return HTTP 200 with metadata JSON on success, HTTP 500 on error.
     */
    @GET
    public Response getMetadata() {
        try {
            String selfUrl = configurationService.getSelfUrl().toString();
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

    /**
     * Constructs the IACAs (Issuing Authority Certificate Authority) endpoint URL.
     *
     * <p>This special handling of the IACAs required because the Private Certificate Authority is
     * not deployed to the staging environment, only to dev and build environments. When running in
     * staging, the Example CRI points to the build instance of the Private Certificate Authority.
     *
     * @param selfUrl The base URL of this credential issuer service.
     * @return The IACAs endpoint URL.
     */
    private String getIacasEndpoint(String selfUrl) {
        if (isRunningInStaging() && isStagingUrl(selfUrl)) {
            String buildUrl = selfUrl.replace(STAGING, BUILD);
            return buildUrl + IACAS_ENDPOINT;
        }
        return selfUrl + IACAS_ENDPOINT;
    }

    /**
     * Determines if the application is running in the staging environment.
     *
     * @return True if running in staging environment, false otherwise.
     */
    private boolean isRunningInStaging() {
        String environment = configurationService.getEnvironment();
        return STAGING.equals(environment);
    }

    /**
     * Determines if the given URL is a staging environment URL.
     *
     * @param url The URL to check
     * @return True if the URL contains the staging identifier, false otherwise.
     */
    private boolean isStagingUrl(String url) {
        return url != null && url.contains(STAGING);
    }
}
