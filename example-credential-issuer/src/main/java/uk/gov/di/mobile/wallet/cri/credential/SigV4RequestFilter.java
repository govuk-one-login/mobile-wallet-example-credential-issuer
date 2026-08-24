package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4FamilyHttpSigner;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.identity.spi.IdentityProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * A JAX-RS client request filter that adds AWS SigV4 authentication headers to outgoing HTTP
 * requests. Used to authenticate requests to API Gateway endpoints that require IAM authorization.
 *
 * <p>When enabled, this filter intercepts outgoing requests and adds the {@code Authorization},
 * {@code X-Amz-Date}, {@code X-Amz-Security-Token}, and {@code x-amz-content-sha256} headers using
 * AWS SigV4 signing.
 */
public class SigV4RequestFilter implements ClientRequestFilter {

    private static final String SERVICE_NAME = "execute-api";

    private final AwsV4HttpSigner signer;
    private final IdentityProvider<AwsCredentialsIdentity> credentialsProvider;
    private final boolean enabled;
    private final String region;

    public SigV4RequestFilter(
            AwsV4HttpSigner signer,
            IdentityProvider<AwsCredentialsIdentity> credentialsProvider,
            boolean enabled,
            String region) {
        this.signer = signer;
        this.credentialsProvider = credentialsProvider;
        this.enabled = enabled;
        this.region = region;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (!enabled) {
            return;
        }

        try {
            AwsCredentialsIdentity credentials = credentialsProvider.resolveIdentity().get();

            SdkHttpRequest.Builder requestBuilder =
                    SdkHttpRequest.builder()
                            .uri(requestContext.getUri())
                            .method(SdkHttpMethod.fromValue(requestContext.getMethod()));

            for (Map.Entry<String, List<Object>> header : requestContext.getHeaders().entrySet()) {
                for (Object value : header.getValue()) {
                    requestBuilder.appendHeader(header.getKey(), value.toString());
                }
            }

            SdkHttpRequest sdkRequest = requestBuilder.build();

            byte[] payload = getPayloadBytes(requestContext);
            ContentStreamProvider contentStreamProvider = () -> new ByteArrayInputStream(payload);

            SignedRequest signedRequest =
                    signer.sign(
                            r ->
                                    r.identity(credentials)
                                            .request(sdkRequest)
                                            .payload(contentStreamProvider)
                                            .putProperty(
                                                    AwsV4FamilyHttpSigner.SERVICE_SIGNING_NAME,
                                                    SERVICE_NAME)
                                            .putProperty(
                                                    AwsV4FamilyHttpSigner.PAYLOAD_SIGNING_ENABLED,
                                                    true)
                                            .putProperty(AwsV4HttpSigner.REGION_NAME, region));

            SdkHttpRequest signedHttpRequest = signedRequest.request();
            copySigningHeaders(signedHttpRequest, requestContext);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Failed to sign request with SigV4", exception);
        } catch (Exception exception) {
            throw new IOException("Failed to sign request with SigV4", exception);
        }
    }

    private byte[] getPayloadBytes(ClientRequestContext requestContext) {
        Object entity = requestContext.getEntity();
        if (entity == null) {
            return new byte[0];
        }
        // Entity is always a String (JWT) as this filter is only registered on the status list
        // client
        return entity.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void copySigningHeaders(
            SdkHttpRequest signedRequest, ClientRequestContext requestContext) {
        copyHeader(signedRequest, requestContext, "Authorization");
        copyHeader(signedRequest, requestContext, "X-Amz-Date");
        copyHeader(signedRequest, requestContext, "X-Amz-Security-Token");
        copyHeader(signedRequest, requestContext, "X-Amz-Content-Sha256");
    }

    private void copyHeader(
            SdkHttpRequest signedRequest, ClientRequestContext requestContext, String headerName) {
        signedRequest
                .firstMatchingHeader(headerName)
                .ifPresent(value -> requestContext.getHeaders().putSingle(headerName, value));
    }
}
