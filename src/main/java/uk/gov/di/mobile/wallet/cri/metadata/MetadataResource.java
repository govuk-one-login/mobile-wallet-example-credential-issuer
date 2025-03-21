package uk.gov.di.mobile.wallet.cri.metadata;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.util.ResponseUtil;

@Singleton
@Path("/.well-known/openid-credential-issuer")
public class MetadataResource {

    private static final String CREDENTIAL_ENDPOINT = "/credential";
    private static final String NOTIFICATION_ENDPOINT = "/notification";
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
            Metadata metadata =
                    metadataBuilder
                            .setCredentialIssuer(selfUrl)
                            .setCredentialEndpoint(selfUrl + CREDENTIAL_ENDPOINT)
                            .setAuthorizationServers(
                                    configurationService.getOneLoginAuthServerUrl())
                            .setNotificationEndpoint(selfUrl + NOTIFICATION_ENDPOINT)
                            .setCredentialConfigurationsSupported(
                                    CREDENTIAL_CONFIGURATION_SUPPORTED_FILE_NAME)
                            .build();
            return ResponseUtil.ok(metadata);
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to get the metadata: ", exception);
            return ResponseUtil.internalServerError();
        }
    }
}
