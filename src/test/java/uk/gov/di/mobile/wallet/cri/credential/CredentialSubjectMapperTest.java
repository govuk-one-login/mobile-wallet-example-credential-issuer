package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static testUtils.mockDocuments.getMockSocialSecurityDocument;
import static testUtils.mockDocuments.getMockVeteranCardDocument;

public class CredentialSubjectMapperTest {
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String DOCUMENT_ID = "de9cbf02-2fbc-4d61-a627-f97851f6840b";

    @Test
    void Should_Map_Document_Into_SocialSecurityCredentialSubject() {
        Document document = getMockSocialSecurityDocument(DOCUMENT_ID, "v2.0");
        SocialSecurityCredentialSubject socialSecurityCredentialSubject =
                CredentialSubjectMapper.buildSocialSecurityCredentialSubject(
                        document, WALLET_SUBJECT_ID);

        assertEquals(WALLET_SUBJECT_ID, socialSecurityCredentialSubject.getId());
        assertEquals(
                "QQ123456C",
                socialSecurityCredentialSubject
                        .getSocialSecurityRecord()
                        .get(0)
                        .getPersonalNumber());
        assertEquals(
                "Title",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(0).getType());
        assertEquals(
                "Miss",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(0).getValue());
        assertEquals(
                "GivenName",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(1).getType());
        assertEquals(
                "Sarah",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(1).getValue());
        assertEquals(
                "GivenName",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(2).getType());
        assertEquals(
                "Elizabeth",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(2).getValue());
        assertEquals(
                "FamilyName",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(3).getType());
        assertEquals(
                "Edwards",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(3).getValue());
    }

    @Test
    void Should_Map_Document_Into_VeteranCardCredentialSubject() {
        Document document = getMockVeteranCardDocument(DOCUMENT_ID);
        VeteranCardCredentialSubject veteranCardCredentialSubject =
                CredentialSubjectMapper.buildVeteranCardCredentialSubject(
                        document, WALLET_SUBJECT_ID);

        assertEquals(WALLET_SUBJECT_ID, veteranCardCredentialSubject.getId());
        assertEquals(
                "2000-07-11", veteranCardCredentialSubject.getVeteranCard().get(0).getExpiryDate());
        assertEquals(
                "HM Naval Service",
                veteranCardCredentialSubject.getVeteranCard().get(0).getServiceBranch());
        assertEquals(
                "25057386",
                veteranCardCredentialSubject.getVeteranCard().get(0).getServiceNumber());
        assertEquals(
                "base64EncodedPhoto",
                veteranCardCredentialSubject.getVeteranCard().get(0).getPhoto());
        assertEquals(
                "GivenName",
                veteranCardCredentialSubject.getName().get(0).getNameParts().get(0).getType());
        assertEquals(
                "Bonnie",
                veteranCardCredentialSubject.getName().get(0).getNameParts().get(0).getValue());
        assertEquals(
                "FamilyName",
                veteranCardCredentialSubject.getName().get(0).getNameParts().get(1).getType());
        assertEquals(
                "Blue",
                veteranCardCredentialSubject.getName().get(0).getNameParts().get(1).getValue());
    }
}
