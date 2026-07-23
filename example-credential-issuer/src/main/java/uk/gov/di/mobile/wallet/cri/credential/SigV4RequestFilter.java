package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4FamilyHttpSigner;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.identity.spi.IdentityProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A JAX-RS client request filter that adds AWS SigV4 authentication headers to outgoing HTTP
 * requests. Used to authenticate requests to API Gateway endpoints that require IAM authorization.
 *
 * <p>When enabled, this filter intercepts outgoing requests and adds the {@code Authorization},
 * {@code X-Amz-Date}, and {@code X-Amz-Security-Token} headers using AWS SigV4 signing.
 */
public class SigV4RequestFilter implements ClientRequestFilter {

    private static final String SERVICE_NAME = "execute-api";
    private static final String REGION = "eu-west-2";

    private final AwsV4HttpSigner signer;
    private final IdentityProvider<AwsCredentialsIdentity> credentialsProvider;
    private final boolean enabled;

    public SigV4RequestFilter(
            AwsV4HttpSigner signer,
            IdentityProvider<AwsCredentialsIdentity> credentialsProvider,
            boolean enabled) {
        this.signer = signer;
        this.credentialsProvider = credentialsProvider;
        this.enabled = enabled;
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

            SignedRequest signedRequest =
                    signer.sign(
                            r ->
                                    r.identity(credentials)
                                            .request(sdkRequest)
                                            .putProperty(
                                                    AwsV4FamilyHttpSigner.SERVICE_SIGNING_NAME,
                                                    SERVICE_NAME)
                                            .putProperty(AwsV4HttpSigner.REGION_NAME, REGION));

            SdkHttpRequest signedHttpRequest = signedRequest.request();
            copySigningHeaders(signedHttpRequest, requestContext);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Failed to sign request with SigV4", exception);
        } catch (Exception exception) {
            throw new IOException("Failed to sign request with SigV4", exception);
        }
    }

    private void copySigningHeaders(
            SdkHttpRequest signedRequest, ClientRequestContext requestContext) {
        copyHeader(signedRequest, requestContext, "Authorization");
        copyHeader(signedRequest, requestContext, "X-Amz-Date");
        copyHeader(signedRequest, requestContext, "X-Amz-Security-Token");
    }

    private void copyHeader(
            SdkHttpRequest signedRequest, ClientRequestContext requestContext, String headerName) {
        List<String> values = signedRequest.headers().get(headerName);
        if (values != null && !values.isEmpty()) {
            requestContext.getHeaders().putSingle(headerName, values.get(0));
        }
    }
}
