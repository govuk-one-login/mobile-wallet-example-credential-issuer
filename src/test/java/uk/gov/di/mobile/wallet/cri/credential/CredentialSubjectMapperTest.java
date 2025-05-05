package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckDocument;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardDocument;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialSubjectMapperTest {
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";

    @Test
    void Should_ReturnSocialSecurityCredentialSubject() {
        SocialSecurityDocument testSocialSecurityDocument = createTestSocialSecurityDocument();

        SocialSecurityCredentialSubject socialSecurityCredentialSubject =
                CredentialSubjectMapper.buildSocialSecurityCredentialSubject(
                        testSocialSecurityDocument, WALLET_SUBJECT_ID);

        assertEquals(WALLET_SUBJECT_ID, socialSecurityCredentialSubject.getId());
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
        assertEquals(
                "QQ123456C",
                socialSecurityCredentialSubject
                        .getSocialSecurityRecord()
                        .get(0)
                        .getPersonalNumber());
    }

    @Test
    void Should_ReturnVeteranCardCredentialSubject() {
        VeteranCardDocument veteranCardDocument = createTestVeteranCardDocument();

        VeteranCardCredentialSubject veteranCardCredentialSubject =
                CredentialSubjectMapper.buildVeteranCardCredentialSubject(
                        veteranCardDocument, WALLET_SUBJECT_ID);

        assertEquals(WALLET_SUBJECT_ID, veteranCardCredentialSubject.getId());
        assertEquals("1970-12-05", veteranCardCredentialSubject.getBirthDate().get(0).getValue());
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
    }

    @Test
    void Should_ReturnBasicCheckCredentialSubject() {
        BasicCheckDocument basicCheckDocument = createTestBasicCheckDocument();

        BasicCheckCredentialSubject basicCheckCredentialSubject =
                CredentialSubjectMapper.buildBasicCheckCredentialSubject(
                        basicCheckDocument, WALLET_SUBJECT_ID);

        assertEquals(WALLET_SUBJECT_ID, basicCheckCredentialSubject.getId());
        assertEquals("1970-12-05", basicCheckCredentialSubject.getBirthDate().get(0).getValue());
        assertEquals(
                "GivenName",
                basicCheckCredentialSubject.getName().get(0).getNameParts().get(0).getType());
        assertEquals(
                "Bonnie",
                basicCheckCredentialSubject.getName().get(0).getNameParts().get(0).getValue());
        assertEquals(
                "FamilyName",
                basicCheckCredentialSubject.getName().get(0).getNameParts().get(1).getType());
        assertEquals(
                "Blue",
                basicCheckCredentialSubject.getName().get(0).getNameParts().get(1).getValue());
        assertEquals("2024-07-11", basicCheckCredentialSubject.getIssuanceDate());
        assertEquals("2025-07-11", basicCheckCredentialSubject.getExpirationDate());
        assertEquals(
                "Clear",
                basicCheckCredentialSubject.getBasicCheckRecord().get(0).getPoliceRecordsCheck());
        assertEquals(
                "E0023455534",
                basicCheckCredentialSubject.getBasicCheckRecord().get(0).getApplicationNumber());
        assertEquals(
                "009878863",
                basicCheckCredentialSubject.getBasicCheckRecord().get(0).getCertificateNumber());
        assertEquals(
                "basic",
                basicCheckCredentialSubject.getBasicCheckRecord().get(0).getCertificateType());
        assertEquals(
                "Result clear",
                basicCheckCredentialSubject.getBasicCheckRecord().get(0).getOutcome());
        assertEquals(
                "Flat 11", basicCheckCredentialSubject.getAddress().get(0).getSubBuildingName());
        assertEquals(
                "Blashford", basicCheckCredentialSubject.getAddress().get(0).getBuildingName());
        assertEquals(
                "Adelaide Road", basicCheckCredentialSubject.getAddress().get(0).getStreetName());
        assertEquals("NW3 3RX", basicCheckCredentialSubject.getAddress().get(0).getPostalCode());
        assertEquals(
                "London", basicCheckCredentialSubject.getAddress().get(0).getAddressLocality());
        assertEquals("GB", basicCheckCredentialSubject.getAddress().get(0).getAddressCountry());
    }

    @Test
    void Should_AllowEmptyGivenName() {
        SocialSecurityDocument testSocialSecurityDocument =
                createTestSocialSecurityDocumentWithEmptyGivenName();

        SocialSecurityCredentialSubject socialSecurityCredentialSubject =
                CredentialSubjectMapper.buildSocialSecurityCredentialSubject(
                        testSocialSecurityDocument, WALLET_SUBJECT_ID);

        assertEquals(WALLET_SUBJECT_ID, socialSecurityCredentialSubject.getId());
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
                "",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(1).getValue());
        assertEquals(
                "FamilyName",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(2).getType());
        assertEquals(
                "Edwards",
                socialSecurityCredentialSubject.getName().get(0).getNameParts().get(2).getValue());
        assertEquals(
                "QQ123456C",
                socialSecurityCredentialSubject
                        .getSocialSecurityRecord()
                        .get(0)
                        .getPersonalNumber());
    }

    public static SocialSecurityDocument createTestSocialSecurityDocument() {
        SocialSecurityDocument document = new SocialSecurityDocument();
        document.setTitle("Miss");
        document.setGivenName("Sarah Elizabeth");
        document.setFamilyName("Edwards");
        document.setNino("QQ123456C");
        return document;
    }

    public static SocialSecurityDocument createTestSocialSecurityDocumentWithEmptyGivenName() {
        SocialSecurityDocument document = new SocialSecurityDocument();
        document.setTitle("Miss");
        document.setGivenName("");
        document.setFamilyName("Edwards");
        document.setNino("QQ123456C");
        return document;
    }

    public static VeteranCardDocument createTestVeteranCardDocument() {
        VeteranCardDocument document = new VeteranCardDocument();
        document.setGivenName("Bonnie");
        document.setFamilyName("Blue");
        document.setDateOfBirthDay("05");
        document.setDateOfBirthMonth("12");
        document.setDateOfBirthYear("1970");
        document.setCardExpiryDateDay("11");
        document.setCardExpiryDateMonth("07");
        document.setCardExpiryDateYear("2000");
        document.setServiceNumber("25057386");
        document.setServiceBranch("HM Naval Service");
        document.setPhoto("base64EncodedPhoto");
        return document;
    }

    public static BasicCheckDocument createTestBasicCheckDocument() {
        BasicCheckDocument document = new BasicCheckDocument();
        document.setIssuanceDay("11");
        document.setIssuanceMonth("07");
        document.setIssuanceYear("2024");
        document.setExpirationDay("11");
        document.setExpirationMonth("07");
        document.setExpirationYear("2025");
        document.setBirthDay("05");
        document.setBirthMonth("12");
        document.setBirthYear("1970");
        document.setFirstName("Bonnie");
        document.setLastName("Blue");
        document.setSubBuildingName("Flat 11");
        document.setBuildingName("Blashford");
        document.setStreetName("Adelaide Road");
        document.setAddressLocality("London");
        document.setAddressCountry("GB");
        document.setPostalCode("NW3 3RX");
        document.setCertificateNumber("009878863");
        document.setApplicationNumber("E0023455534");
        document.setCertificateType("basic");
        document.setOutcome("Result clear");
        document.setPoliceRecordsCheck("Clear");
        return document;
    }
}
