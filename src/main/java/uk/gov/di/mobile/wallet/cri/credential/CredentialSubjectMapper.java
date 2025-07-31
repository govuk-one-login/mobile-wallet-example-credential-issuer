package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.*;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCard;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubjectBuilder;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardDocument;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubjectBuilder;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityDocument;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityRecord;

import java.util.ArrayList;
import java.util.List;

public class CredentialSubjectMapper {

    static final ObjectMapper objectMapper = new ObjectMapper();

    @ExcludeFromGeneratedCoverageReport
    private CredentialSubjectMapper() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static SocialSecurityCredentialSubject buildSocialSecurityCredentialSubject(
            SocialSecurityDocument socialSecurityDocument, String id) {
        List<Name> name =
                buildName(
                        socialSecurityDocument.getGivenName(),
                        socialSecurityDocument.getFamilyName(),
                        socialSecurityDocument.getTitle());

        List<SocialSecurityRecord> socialSecurityRecord =
                buildSocialSecurityRecord(socialSecurityDocument);

        return new SocialSecurityCredentialSubjectBuilder()
                .setName(name)
                .setSocialSecurityRecord(socialSecurityRecord)
                .setId(id)
                .build();
    }

    public static BasicCheckCredentialSubject buildBasicCheckCredentialSubject(
            BasicCheckDocument basicCheckDocument, String id) {
        List<Name> name =
                buildName(
                        basicCheckDocument.getFirstName(), basicCheckDocument.getLastName(), null);

        List<BirthDate> birthDate =
                buildBirthDate(
                        basicCheckDocument.getBirthYear(),
                        basicCheckDocument.getBirthMonth(),
                        basicCheckDocument.getBirthDay());

        List<Address> address = buildAddress(basicCheckDocument);

        String issuanceDate =
                getFormattedDate(
                        basicCheckDocument.getIssuanceYear(),
                        basicCheckDocument.getIssuanceMonth(),
                        basicCheckDocument.getIssuanceDay());

        String expirationDate =
                getFormattedDate(
                        basicCheckDocument.getExpirationYear(),
                        basicCheckDocument.getExpirationMonth(),
                        basicCheckDocument.getExpirationDay());

        List<BasicCheckRecord> basicCheckRecord = buildBasicCheckRecord(basicCheckDocument);

        return new BasicCheckCredentialSubjectBuilder()
                .setName(name)
                .setBirthDate(birthDate)
                .setAddress(address)
                .setIssuanceDate(issuanceDate)
                .setExpirationDate(expirationDate)
                .setBasicCheckRecord(basicCheckRecord)
                .setId(id)
                .build();
    }

    public static VeteranCardCredentialSubject buildVeteranCardCredentialSubject(
            VeteranCardDocument veteranCardDocument, String id) {
        List<Name> name =
                buildName(
                        veteranCardDocument.getGivenName(),
                        veteranCardDocument.getFamilyName(),
                        null);

        List<BirthDate> birthDate =
                buildBirthDate(
                        veteranCardDocument.getDateOfBirthYear(),
                        veteranCardDocument.getDateOfBirthMonth(),
                        veteranCardDocument.getDateOfBirthDay());

        List<VeteranCard> veteranCard = buildVeteranCard(veteranCardDocument);

        return new VeteranCardCredentialSubjectBuilder()
                .setName(name)
                .setBirthDate(birthDate)
                .setVeteranCard(veteranCard)
                .setId(id)
                .build();
    }

    private static @NotNull List<SocialSecurityRecord> buildSocialSecurityRecord(
            SocialSecurityDocument ninoDocument) {
        List<SocialSecurityRecord> socialSecurityRecordList = new ArrayList<>();

        SocialSecurityRecord socialSecurityRecord = new SocialSecurityRecord();
        socialSecurityRecord.setPersonalNumber(ninoDocument.getNino());

        socialSecurityRecordList.add(socialSecurityRecord);

        return socialSecurityRecordList;
    }

    private static @NotNull List<BasicCheckRecord> buildBasicCheckRecord(
            BasicCheckDocument basicCheckDocument) {
        List<BasicCheckRecord> basicCheckRecordList = new ArrayList<>();

        BasicCheckRecord basicCheckRecord = new BasicCheckRecord();

        basicCheckRecord.setCertificateNumber(basicCheckDocument.getCertificateNumber());
        basicCheckRecord.setApplicationNumber(basicCheckDocument.getApplicationNumber());
        basicCheckRecord.setCertificateType(basicCheckDocument.getCertificateType());
        basicCheckRecord.setOutcome(basicCheckDocument.getOutcome());
        basicCheckRecord.setPoliceRecordsCheck(basicCheckDocument.getPoliceRecordsCheck());

        basicCheckRecordList.add(basicCheckRecord);

        return basicCheckRecordList;
    }

    private static @NotNull List<VeteranCard> buildVeteranCard(
            VeteranCardDocument veteranCardDocument) {
        List<VeteranCard> veteranCardList = new ArrayList<>();

        VeteranCard veteranCard = new VeteranCard();

        veteranCard.setExpiryDate(
                getFormattedDate(
                        veteranCardDocument.getCardExpiryDateYear(),
                        veteranCardDocument.getCardExpiryDateMonth(),
                        veteranCardDocument.getCardExpiryDateDay()));
        veteranCard.setServiceNumber(veteranCardDocument.getServiceNumber());
        veteranCard.setServiceBranch(veteranCardDocument.getServiceBranch());
        veteranCard.setPhoto(veteranCardDocument.getPhoto());

        veteranCardList.add(veteranCard);

        return veteranCardList;
    }

    private static @NotNull List<Name> buildName(
            String givenName, String familyName, String title) {
        List<NamePart> nameParts = new ArrayList<>();

        if (title != null && !title.isEmpty()) {
            nameParts.add(setNamePart(title, "Title"));
        }

        String[] givenNames = givenName.split(" ");
        for (String name : givenNames) {
            nameParts.add(setNamePart(name, "GivenName"));
        }

        String[] familyNames = familyName.split(" ");
        for (String name : familyNames) {
            nameParts.add(setNamePart(name, "FamilyName"));
        }

        Name name = new Name();
        name.setNameParts(nameParts);

        List<Name> nameList = new ArrayList<>();
        nameList.add(name);

        return nameList;
    }

    private static NamePart setNamePart(String value, String type) {
        NamePart namePart = new NamePart();

        namePart.setValue(value);
        namePart.setType(type);

        return namePart;
    }

    private static @NotNull String getFormattedDate(String year, String month, String day) {
        return String.format("%s-%s-%s", year, month, day);
    }

    private static @NotNull List<BirthDate> buildBirthDate(String year, String month, String day) {
        List<BirthDate> birthDateList = new ArrayList<>();

        BirthDate birthDate = new BirthDate();
        birthDate.setValue(getFormattedDate(year, month, day));

        birthDateList.add(birthDate);

        return birthDateList;
    }

    private static @NotNull List<Address> buildAddress(BasicCheckDocument basicCheckDocument) {
        List<Address> addressList = new ArrayList<>();

        Address address = new Address();

        address.setSubBuildingName(basicCheckDocument.getSubBuildingName());
        address.setBuildingName(basicCheckDocument.getBuildingName());
        address.setStreetName(basicCheckDocument.getStreetName());
        address.setAddressLocality(basicCheckDocument.getAddressLocality());
        address.setPostalCode(basicCheckDocument.getPostalCode());
        address.setAddressCountry(basicCheckDocument.getAddressCountry());

        addressList.add(address);

        return addressList;
    }
}
