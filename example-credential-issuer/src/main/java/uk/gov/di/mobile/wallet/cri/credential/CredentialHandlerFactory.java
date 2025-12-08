package uk.gov.di.mobile.wallet.cri.credential;

import uk.gov.di.mobile.wallet.cri.credential.jwt.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.BasicCheckCredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.jwt.digital_veteran_card.DigitalVeteranCardHandler;
import uk.gov.di.mobile.wallet.cri.credential.jwt.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.jwt.social_security_credential.SocialSecurityCredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.jwt.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.MdocCredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.example_document.ExampleDocument;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.example_document.ExampleDocumentHandler;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.MobileDrivingLicenceHandler;

public class CredentialHandlerFactory {

    private final CredentialBuilder<BasicCheckCredentialSubject> basicCheckCredentialBuilder;
    private final CredentialBuilder<SocialSecurityCredentialSubject>
            socialSecurityCredentialBuilder;
    private final CredentialBuilder<VeteranCardCredentialSubject> digitalVeteranCardBuilder;
    private final MdocCredentialBuilder<DrivingLicenceDocument> mobileDrivingLicenceBuilder;
    private final MdocCredentialBuilder<ExampleDocument> exampleMdocBuilder;

    public CredentialHandlerFactory(
            CredentialBuilder<BasicCheckCredentialSubject> basicCredentialBuilder,
            CredentialBuilder<SocialSecurityCredentialSubject> socialSecurityCredentialBuilder,
            CredentialBuilder<VeteranCardCredentialSubject> digitalVeteranCardBuilder,
            MdocCredentialBuilder<DrivingLicenceDocument> mobileDrivingLicenceBuilder,
            MdocCredentialBuilder<ExampleDocument> exampleMdocBuilder) {
        this.basicCheckCredentialBuilder = basicCredentialBuilder;
        this.socialSecurityCredentialBuilder = socialSecurityCredentialBuilder;
        this.digitalVeteranCardBuilder = digitalVeteranCardBuilder;
        this.mobileDrivingLicenceBuilder = mobileDrivingLicenceBuilder;
        this.exampleMdocBuilder = exampleMdocBuilder;
    }

    public CredentialHandler createHandler(String vcType) {
        CredentialType credentialType = CredentialType.fromType(vcType);

        return switch (credentialType) {
            case BASIC_DISCLOSURE_CREDENTIAL -> new BasicCheckCredentialHandler(
                    basicCheckCredentialBuilder);
            case SOCIAL_SECURITY_CREDENTIAL -> new SocialSecurityCredentialHandler(
                    socialSecurityCredentialBuilder);
            case DIGITAL_VETERAN_CARD -> new DigitalVeteranCardHandler(digitalVeteranCardBuilder);
            case MOBILE_DRIVING_LICENCE -> new MobileDrivingLicenceHandler(
                    mobileDrivingLicenceBuilder);
            case EXAMPLE_MDOC -> new ExampleDocumentHandler(exampleMdocBuilder);
        };
    }
}
