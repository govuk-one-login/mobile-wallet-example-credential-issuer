package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.identity.spi.IdentityProvider;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SigV4RequestFilterTest {

    @Mock private AwsV4HttpSigner mockSigner;
    @Mock private IdentityProvider<AwsCredentialsIdentity> mockCredentialsProvider;
    @Mock private AwsCredentialsIdentity mockCredentials;
    @Mock private ClientRequestContext mockRequestContext;

    private static final URI TEST_URI = URI.create("https://api.crs.staging.account.gov.uk/issue");

    @Nested
    class WhenDisabled {

        @Test
        void shouldNotSignRequest() {
            SigV4RequestFilter filter =
                    new SigV4RequestFilter(mockSigner, mockCredentialsProvider, false);

            assertDoesNotThrow(() -> filter.filter(mockRequestContext));

            verifyNoInteractions(mockSigner);
            verifyNoInteractions(mockCredentialsProvider);
            verify(mockRequestContext, never()).getUri();
        }
    }

    @Nested
    class WhenEnabled {

        @Test
        @SuppressWarnings("unchecked")
        void shouldAddSigningHeadersToRequest() throws IOException {
            SigV4RequestFilter filter =
                    new SigV4RequestFilter(mockSigner, mockCredentialsProvider, true);

            doReturn(CompletableFuture.completedFuture(mockCredentials))
                    .when(mockCredentialsProvider)
                    .resolveIdentity();
            when(mockRequestContext.getUri()).thenReturn(TEST_URI);
            when(mockRequestContext.getMethod()).thenReturn("POST");

            MultivaluedMap<String, Object> requestHeaders = new MultivaluedHashMap<>();
            requestHeaders.put("Content-Type", List.of("application/jwt"));
            when(mockRequestContext.getHeaders()).thenReturn(requestHeaders);

            SdkHttpRequest signedHttpRequest =
                    SdkHttpRequest.builder()
                            .uri(TEST_URI)
                            .method(software.amazon.awssdk.http.SdkHttpMethod.POST)
                            .putHeader("Authorization", "AWS4-HMAC-SHA256 Credential=...")
                            .putHeader("X-Amz-Date", "20260723T103000Z")
                            .putHeader("X-Amz-Security-Token", "test-session-token")
                            .build();

            SignedRequest signedRequest =
                    SignedRequest.builder().request(signedHttpRequest).build();

            when(mockSigner.sign(any(Consumer.class))).thenReturn(signedRequest);

            filter.filter(mockRequestContext);

            verify(mockSigner).sign(any(Consumer.class));
            assertEquals(
                    "AWS4-HMAC-SHA256 Credential=...", requestHeaders.getFirst("Authorization"));
            assertEquals("20260723T103000Z", requestHeaders.getFirst("X-Amz-Date"));
            assertEquals("test-session-token", requestHeaders.getFirst("X-Amz-Security-Token"));
        }

        @Test
        void shouldThrowIOExceptionWhenCredentialResolutionFails() {
            SigV4RequestFilter filter =
                    new SigV4RequestFilter(mockSigner, mockCredentialsProvider, true);

            doReturn(
                            CompletableFuture.failedFuture(
                                    new RuntimeException("No credentials available")))
                    .when(mockCredentialsProvider)
                    .resolveIdentity();

            IOException exception =
                    assertThrows(IOException.class, () -> filter.filter(mockRequestContext));

            assertEquals("Failed to sign request with SigV4", exception.getMessage());
            assertInstanceOf(Exception.class, exception.getCause());
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowIOExceptionWhenSigningFails() {
            SigV4RequestFilter filter =
                    new SigV4RequestFilter(mockSigner, mockCredentialsProvider, true);

            doReturn(CompletableFuture.completedFuture(mockCredentials))
                    .when(mockCredentialsProvider)
                    .resolveIdentity();
            when(mockRequestContext.getUri()).thenReturn(TEST_URI);
            when(mockRequestContext.getMethod()).thenReturn("POST");

            MultivaluedMap<String, Object> requestHeaders = new MultivaluedHashMap<>();
            requestHeaders.put("Content-Type", List.of("application/jwt"));
            when(mockRequestContext.getHeaders()).thenReturn(requestHeaders);

            when(mockSigner.sign(any(Consumer.class)))
                    .thenThrow(new RuntimeException("Signing failed"));

            IOException exception =
                    assertThrows(IOException.class, () -> filter.filter(mockRequestContext));

            assertEquals("Failed to sign request with SigV4", exception.getMessage());
            assertInstanceOf(RuntimeException.class, exception.getCause());
            assertEquals("Signing failed", exception.getCause().getMessage());
        }
    }
}
