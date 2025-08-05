package uk.gov.di.mobile.wallet.cri;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.CredentialService;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.credential_offer.PreAuthorizedCodeBuilder;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.iacas.IacasService;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.notification.NotificationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ServicesTest {

    @Test
    void Should_SetAllServicesCorrectly() {
        // Arrange: Create mocks for all dependencies
        KmsService kmsService = mock(KmsService.class);
        PreAuthorizedCodeBuilder preAuthorizedCodeBuilder = mock(PreAuthorizedCodeBuilder.class);
        CredentialOfferService credentialOfferService = mock(CredentialOfferService.class);
        DynamoDbService dynamoDbService = mock(DynamoDbService.class);
        MetadataBuilder metadataBuilder = mock(MetadataBuilder.class);
        CredentialService credentialService = mock(CredentialService.class);
        DidDocumentService didDocumentService = mock(DidDocumentService.class);
        JwksService jwksService = mock(JwksService.class);
        NotificationService notificationService = mock(NotificationService.class);
        IacasService iacasService = mock(IacasService.class);

        // Act: Build the Services object
        Services services =
                new Services.Builder()
                        .kmsService(kmsService)
                        .preAuthorizedCodeBuilder(preAuthorizedCodeBuilder)
                        .credentialOfferService(credentialOfferService)
                        .dynamoDbService(dynamoDbService)
                        .metadataBuilder(metadataBuilder)
                        .credentialService(credentialService)
                        .didDocumentService(didDocumentService)
                        .jwksService(jwksService)
                        .notificationService(notificationService)
                        .iacasService(iacasService)
                        .build();

        // Assert: Each getter returns the correct instance
        assertSame(kmsService, services.getKmsService());
        assertSame(preAuthorizedCodeBuilder, services.getPreAuthorizedCodeBuilder());
        assertSame(credentialOfferService, services.getCredentialOfferService());
        assertSame(dynamoDbService, services.getDynamoDbService());
        assertSame(metadataBuilder, services.getMetadataBuilder());
        assertSame(credentialService, services.getCredentialService());
        assertSame(didDocumentService, services.getDidDocumentService());
        assertSame(jwksService, services.getJwksService());
        assertSame(notificationService, services.getNotificationService());
        assertSame(iacasService, services.getIacasService());
    }
}
