package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CredentialHandlerFactoryTest {

    @Mock private CredentialBuilder<BasicCheckCredentialSubject> mockBasicCheckCredentialBuilder;

    @Mock
    private CredentialBuilder<SocialSecurityCredentialSubject> mockSocialSecurityCredentialBuilder;

    @Mock private CredentialBuilder<VeteranCardCredentialSubject> mockDigitalVeteranCardBuilder;
    @Mock private MdocCredentialBuilder<DrivingLicenceDocument> mockMobileDrivingLicenceBuilder;
    @Mock private MdocCredentialBuilder<ExampleDocument> mockExampleMdocBuilder;

    private CredentialHandlerFactory factory;

    @BeforeEach
    void setUp() {
        factory =
                new CredentialHandlerFactory(
                        mockBasicCheckCredentialBuilder,
                        mockSocialSecurityCredentialBuilder,
                        mockDigitalVeteranCardBuilder,
                        mockMobileDrivingLicenceBuilder,
                        mockExampleMdocBuilder);
    }

    @Test
    void Should_CreateBasicCheckCredentialHandler() {
        String vcType = "BasicDisclosureCredential";

        CredentialHandler handler = factory.createHandler(vcType);

        assertInstanceOf(
                BasicCheckCredentialHandler.class,
                handler,
                "Handler should be instance of BasicCheckCredentialHandler");
    }

    @Test
    void Should_CreateSocialSecurityCredentialHandler() {
        String vcType = "SocialSecurityCredential";

        CredentialHandler handler = factory.createHandler(vcType);

        assertInstanceOf(
                SocialSecurityCredentialHandler.class,
                handler,
                "Handler should be instance of SocialSecurityCredentialHandler");
    }

    @Test
    void Should_CreateDigitalVeteranCardHandler() {
        String vcType = "DigitalVeteranCard";

        CredentialHandler handler = factory.createHandler(vcType);

        assertInstanceOf(
                DigitalVeteranCardHandler.class,
                handler,
                "Handler should be instance of DigitalVeteranCardHandler");
    }

    @Test
    void Should_CreateMobileDrivingLicenceHandler() {
        String vcType = "org.iso.18013.5.1.mDL";

        CredentialHandler handler = factory.createHandler(vcType);

        assertInstanceOf(
                MobileDrivingLicenceHandler.class,
                handler,
                "Handler should be instance of MobileDrivingLicenceHandler");
    }

    @Test
    void Should_CreateExampleDocumentHandler() {
        String vcType = "uk.gov.account.mobile.example-credential-issuer.examplemdoc.1";

        CredentialHandler handler = factory.createHandler(vcType);

        assertInstanceOf(
                ExampleDocumentHandler.class,
                handler,
                "Handler should be instance of ExampleDocumentHandler");
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_CredentialTypeIsUnknown() {
        String vcType = "UnknownCredential";

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> factory.createHandler(vcType),
                        "Should throw IllegalArgumentException for unknown type");
        assertEquals("Unknown credential type: " + vcType, exception.getMessage());
    }
}
