package uk.gov.di.mobile.wallet.cri;

import lombok.Getter;
import uk.gov.di.mobile.wallet.cri.credential.CredentialService;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.credential_offer.PreAuthorizedCodeBuilder;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.notification.NotificationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

@Getter
public class Services {
    private final KmsService kmsService;
    private final PreAuthorizedCodeBuilder preAuthorizedCodeBuilder;
    private final CredentialOfferService credentialOfferService;
    private final DynamoDbService dynamoDbService;
    private final MetadataBuilder metadataBuilder;
    private final CredentialService credentialService;
    private final DidDocumentService didDocumentService;
    private final JwksService jwksService;
    private final NotificationService notificationService;

    public Services(
            KmsService kmsService,
            PreAuthorizedCodeBuilder preAuthorizedCodeBuilder,
            CredentialOfferService credentialOfferService,
            DynamoDbService dynamoDbService,
            MetadataBuilder metadataBuilder,
            CredentialService credentialService,
            DidDocumentService didDocumentService,
            JwksService jwksService,
            NotificationService notificationService) {
        this.kmsService = kmsService;
        this.preAuthorizedCodeBuilder = preAuthorizedCodeBuilder;
        this.credentialOfferService = credentialOfferService;
        this.dynamoDbService = dynamoDbService;
        this.metadataBuilder = metadataBuilder;
        this.credentialService = credentialService;
        this.didDocumentService = didDocumentService;
        this.jwksService = jwksService;
        this.notificationService = notificationService;
    }
}
