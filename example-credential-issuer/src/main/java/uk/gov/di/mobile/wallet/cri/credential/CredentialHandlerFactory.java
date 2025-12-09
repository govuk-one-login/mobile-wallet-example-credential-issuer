package uk.gov.di.mobile.wallet.cri.credential;

import uk.gov.di.mobile.wallet.cri.credential.jwt.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.BasicCheckCredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.jwt.digital_veteran_card.DigitalVeteranCardHandler;
import uk.gov.di.mobile.wallet.cri.credential.jwt.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.jwt.social_security_credential.SocialSecurityCredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.jwt.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.MdocCredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.MobileDrivingLicenceHandler;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.simple_mdoc.SimpleDocument;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.simple_mdoc.SimpleMdocHandler;

public class CredentialHandlerFactory {

    private final CredentialBuilder<BasicCheckCredentialSubject> basicDisclosureCredentialBuilder;
    private final CredentialBuilder<SocialSecurityCredentialSubject>
            socialSecurityCredentialBuilder;
    private final CredentialBuilder<VeteranCardCredentialSubject> digitalVeteranCardBuilder;
    private final MdocCredentialBuilder<DrivingLicenceDocument> mobileDrivingLicenceBuilder;
    private final MdocCredentialBuilder<SimpleDocument> exampleMdocBuilder;

    public CredentialHandlerFactory(
            CredentialBuilder<BasicCheckCredentialSubject> basicDisclosureCredentialBuilder,
            CredentialBuilder<SocialSecurityCredentialSubject> socialSecurityCredentialBuilder,
            CredentialBuilder<VeteranCardCredentialSubject> digitalVeteranCardBuilder,
            MdocCredentialBuilder<DrivingLicenceDocument> mobileDrivingLicenceBuilder,
            MdocCredentialBuilder<SimpleDocument> simpleMdocBuilder) {
        this.basicDisclosureCredentialBuilder = basicDisclosureCredentialBuilder;
        this.socialSecurityCredentialBuilder = socialSecurityCredentialBuilder;
        this.digitalVeteranCardBuilder = digitalVeteranCardBuilder;
        this.mobileDrivingLicenceBuilder = mobileDrivingLicenceBuilder;
        this.exampleMdocBuilder = simpleMdocBuilder;
    }

    public CredentialHandler createHandler(String vcType) {
        CredentialType credentialType = CredentialType.fromType(vcType);

        return switch (credentialType) {
            case BASIC_DISCLOSURE_CREDENTIAL -> new BasicCheckCredentialHandler(
                    basicDisclosureCredentialBuilder);
            case SOCIAL_SECURITY_CREDENTIAL -> new SocialSecurityCredentialHandler(
                    socialSecurityCredentialBuilder);
            case DIGITAL_VETERAN_CARD -> new DigitalVeteranCardHandler(digitalVeteranCardBuilder);
            case MOBILE_DRIVING_LICENCE -> new MobileDrivingLicenceHandler(
                    mobileDrivingLicenceBuilder);
            case SIMPLE_MDOC -> new SimpleMdocHandler(exampleMdocBuilder);
        };
    }
}
