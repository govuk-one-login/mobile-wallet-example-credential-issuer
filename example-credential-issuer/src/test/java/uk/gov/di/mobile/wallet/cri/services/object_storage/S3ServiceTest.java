package uk.gov.di.mobile.wallet.cri.services.object_storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3ServiceTest {

    private S3Client mockS3Client;
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        mockS3Client = mock(S3Client.class);
        s3Service = new S3Service(mockS3Client);
    }

    @Test
    void Should_GetClientForLocalEnvironment() {
        ConfigurationService config = mock(ConfigurationService.class);
        when(config.getEnvironment()).thenReturn("local");
        when(config.getLocalstackEndpoint()).thenReturn("http://localhost:4566");
        when(config.getAwsRegion()).thenReturn("eu-west-2");

        S3Client client = S3Service.getClient(config);

        assertNotNull(client);
        verify(config, times(1)).getEnvironment();
        verify(config, times(1)).getLocalstackEndpoint();
        verify(config, times(1)).getAwsRegion();
        assertEquals(Region.of("eu-west-2"), client.serviceClientConfiguration().region());
        assertEquals(
                URI.create("http://localhost:4566"),
                client.serviceClientConfiguration().endpointOverride().get());
    }

    @Test
    void Should_GetClientForNonLocalEnvironment() {
        ConfigurationService config = mock(ConfigurationService.class);
        when(config.getEnvironment()).thenReturn("dev");
        when(config.getAwsRegion()).thenReturn("eu-west-2");

        S3Client client = S3Service.getClient(config);

        assertNotNull(client);
        verify(config, times(1)).getEnvironment();
        verify(config, never()).getLocalstackEndpoint(); // Not called for non-local client
        verify(config, times(1)).getAwsRegion();
        assertEquals(Region.of("eu-west-2"), client.serviceClientConfiguration().region());
    }

    @Test
    void Should_PropagateExceptionThrownByS3Client() {
        when(mockS3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new RuntimeException("S3 error"));

        ObjectStoreException exception =
                assertThrows(
                        ObjectStoreException.class, () -> s3Service.getObject("bucket", "key"));
        assertTrue(exception.getMessage().contains("Error fetching object from S3"));
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }

    @Test
    void Should_SuccessfullyGetObject() throws Exception {
        byte[] expectedContent = {1, 2, 3, 4, 5};
        ResponseInputStream<GetObjectResponse> mockResponse =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        new ByteArrayInputStream(expectedContent));

        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponse);

        byte[] result = s3Service.getObject("bucket", "key");

        assertArrayEquals(expectedContent, result);
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockS3Client).getObject(captor.capture());
        assertEquals("bucket", captor.getValue().bucket());
        assertEquals("key", captor.getValue().key());
    }
}
