package uk.gov.di.mobile.wallet.cri;

import lombok.Getter;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.CredentialService;
import uk.gov.di.mobile.wallet.cri.credential_offer.CredentialOfferService;
import uk.gov.di.mobile.wallet.cri.credential_offer.PreAuthorizedCodeBuilder;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.metadata.MetadataBuilder;
import uk.gov.di.mobile.wallet.cri.notification.NotificationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

/**
 * Container for all core application services.
 *
 * <p>Use the {@link Builder} to construct instances.
 */
@Getter
@ExcludeFromGeneratedCoverageReport
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

    private Services(Builder builder) {
        this.kmsService = builder.kmsService;
        this.preAuthorizedCodeBuilder = builder.preAuthorizedCodeBuilder;
        this.credentialOfferService = builder.credentialOfferService;
        this.dynamoDbService = builder.dynamoDbService;
        this.metadataBuilder = builder.metadataBuilder;
        this.credentialService = builder.credentialService;
        this.didDocumentService = builder.didDocumentService;
        this.jwksService = builder.jwksService;
        this.notificationService = builder.notificationService;
    }

    /** Builder for {@link Services}. */
    public static class Builder {
        private KmsService kmsService;
        private PreAuthorizedCodeBuilder preAuthorizedCodeBuilder;
        private CredentialOfferService credentialOfferService;
        private DynamoDbService dynamoDbService;
        private MetadataBuilder metadataBuilder;
        private CredentialService credentialService;
        private DidDocumentService didDocumentService;
        private JwksService jwksService;
        private NotificationService notificationService;

        public Builder kmsService(KmsService kmsService) {
            this.kmsService = kmsService;
            return this;
        }

        public Builder preAuthorizedCodeBuilder(PreAuthorizedCodeBuilder preAuthorizedCodeBuilder) {
            this.preAuthorizedCodeBuilder = preAuthorizedCodeBuilder;
            return this;
        }

        public Builder credentialOfferService(CredentialOfferService credentialOfferService) {
            this.credentialOfferService = credentialOfferService;
            return this;
        }

        public Builder dynamoDbService(DynamoDbService dynamoDbService) {
            this.dynamoDbService = dynamoDbService;
            return this;
        }

        public Builder metadataBuilder(MetadataBuilder metadataBuilder) {
            this.metadataBuilder = metadataBuilder;
            return this;
        }

        public Builder credentialService(CredentialService credentialService) {
            this.credentialService = credentialService;
            return this;
        }

        public Builder didDocumentService(DidDocumentService didDocumentService) {
            this.didDocumentService = didDocumentService;
            return this;
        }

        public Builder jwksService(JwksService jwksService) {
            this.jwksService = jwksService;
            return this;
        }

        public Builder notificationService(NotificationService notificationService) {
            this.notificationService = notificationService;
            return this;
        }

        public Services build() {
            return new Services(this);
        }
    }
}
