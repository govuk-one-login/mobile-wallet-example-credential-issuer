// package uk.gov.di.mobile.wallet.cri.credential;
//
// import jakarta.ws.rs.client.Client;
// import jakarta.ws.rs.client.Invocation;
// import jakarta.ws.rs.client.WebTarget;
// import jakarta.ws.rs.core.MediaType;
// import jakarta.ws.rs.core.Response;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
//
// import java.net.URI;
//
// import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.Matchers.containsString;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.when;
//
// public class DocumentStoreClientTest {
//  @Mock
//  private Client mockHttpClient;
//  @Mock private WebTarget mockWebTarget;
//  @Mock private Invocation.Builder mockInvocationBuilder;
//  @Mock private Response mockResponse;
//
//  @Test
//  void Should_Throw_CredentialServiceException_When_Document_Endpoint_Returns_500()
//          throws DataStoreException {
//    when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
//    when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
//    when(mockInvocationBuilder.get()).thenReturn(mockResponse);
//    when(mockResponse.getStatus()).thenReturn(500);
//
//    CredentialServiceException exception =
//            assertThrows(
//                    CredentialServiceException.class,
//                    () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
//
//    assertThat(
//            exception.getMessage(),
//            containsString(
//                    "Request to fetch document de9cbf02-2fbc-4d61-a627-f97851f6840b failed with
// status code 500"));
//  }
// }
