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

    @ExcludeFromGeneratedCoverageReport
    private CredentialSubjectMapper() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static SocialSecurityCredentialSubject buildSocialSecurityCredentialSubject(
            Document document, String id) {
        ObjectMapper objectMapper = new ObjectMapper();
        final SocialSecurityDocument ninoDocument =
                objectMapper.convertValue(document.getData(), SocialSecurityDocument.class);

        String[] givenNames = ninoDocument.getGivenName().split(" ");
        String[] familyNames = ninoDocument.getFamilyName().split(" ");
        String title = ninoDocument.getTitle();
        List<Name> names = buildNames(title, givenNames, familyNames);

        List<SocialSecurityRecord> socialSecurityRecords = new ArrayList<>();
        SocialSecurityRecord socialSecurityRecord = new SocialSecurityRecord();
        socialSecurityRecord.setPersonalNumber(ninoDocument.getNino());
        socialSecurityRecords.add(socialSecurityRecord);

        return new SocialSecurityCredentialSubjectBuilder()
                .setName(names)
                .setSocialSecurityRecord(socialSecurityRecords)
                .setId(id)
                .build();
    }

    public static BasicCheckCredentialSubject buildBasicDisclosureCredentialSubject(
            Document document, String id) {
        ObjectMapper objectMapper = new ObjectMapper();
        final BasicCheckDocument basicCheckDocument =
                objectMapper.convertValue(document.getData(), BasicCheckDocument.class);

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

        String[] givenNames = basicCheckDocument.getFirstName().split(" ");
        String[] familyNames = basicCheckDocument.getLastName().split(" ");
        List<Name> names = buildNames(givenNames, familyNames);

        List<BirthDate> birthDates = new ArrayList<>();
        BirthDate birthDate = new BirthDate();
        birthDate.setValue(
                getFormattedDate(
                        basicCheckDocument.getBirthYear(),
                        basicCheckDocument.getBirthMonth(),
                        basicCheckDocument.getBirthDay()));
        birthDates.add(birthDate);

        List<Address> addresses = buildAddresses(basicCheckDocument);
        List<BasicCheckRecord> basicCheckRecords = buildBasicCheckRecords(basicCheckDocument);

        return new BasicCheckCredentialSubjectBuilder()
                .setIssuanceDate(issuanceDate)
                .setExpirationDate(expirationDate)
                .setName(names)
                .setBirthDate(birthDates)
                .setAddress(addresses)
                .setBasicCheckRecord(basicCheckRecords)
                .setId(id)
                .build();
    }

    public static VeteranCardCredentialSubject buildVeteranCardCredentialSubject(
            Document document, String id) {
        ObjectMapper objectMapper = new ObjectMapper();
        final VeteranCardDocument veteranCardDocument =
                objectMapper.convertValue(document.getData(), VeteranCardDocument.class);

        String[] givenNames = veteranCardDocument.getGivenName().split(" ");
        String[] familyNames = veteranCardDocument.getFamilyName().split(" ");

        List<Name> names = buildNames(givenNames, familyNames);

        List<BirthDate> birthDates = new ArrayList<>();
        BirthDate birthDate = new BirthDate();

        birthDate.setValue(
                getFormattedDate(
                        veteranCardDocument.getDateOfBirthYear(),
                        veteranCardDocument.getDateOfBirthMonth(),
                        veteranCardDocument.getDateOfBirthDay()));
        birthDates.add(birthDate);

        List<VeteranCard> veteranCards = buildVeteranCards(veteranCardDocument);

        return new VeteranCardCredentialSubjectBuilder()
                .setName(names)
                .setBirthDate(birthDates)
                .setVeteranCard(veteranCards)
                .setId(id)
                .build();
    }

    private static @NotNull List<BasicCheckRecord> buildBasicCheckRecords(
            BasicCheckDocument basicCheckDocument) {
        List<BasicCheckRecord> basicCheckRecords = new ArrayList<>();
        BasicCheckRecord basicCheckRecord = new BasicCheckRecord();
        basicCheckRecord.setCertificateNumber(basicCheckDocument.getCertificateNumber());
        basicCheckRecord.setApplicationNumber(basicCheckDocument.getApplicationNumber());
        basicCheckRecord.setCertificateType("basic");
        basicCheckRecord.setOutcome("Result clear");
        basicCheckRecord.setPoliceRecordsCheck("Clear");
        basicCheckRecords.add(basicCheckRecord);
        return basicCheckRecords;
    }

    private static @NotNull List<Address> buildAddresses(BasicCheckDocument basicCheckDocument) {
        List<Address> addresses = new ArrayList<>();
        Address address = new Address();
        address.setSubBuildingName(basicCheckDocument.getSubBuildingName());
        address.setBuildingName(basicCheckDocument.getBuildingName());
        address.setStreetName(basicCheckDocument.getStreetName());
        address.setAddressLocality(basicCheckDocument.getAddressLocality());
        address.setPostalCode(basicCheckDocument.getPostalCode());
        address.setAddressCountry(basicCheckDocument.getAddressCountry());
        addresses.add(address);
        return addresses;
    }

    private static @NotNull List<VeteranCard> buildVeteranCards(
            VeteranCardDocument veteranCardDocument) {
        List<VeteranCard> veteranCards = new ArrayList<>();
        VeteranCard veteranCard = new VeteranCard();
        veteranCard.setExpiryDate(
                String.format(
                        "%s-%s-%s",
                        veteranCardDocument.getCardExpiryDateYear(),
                        veteranCardDocument.getCardExpiryDateMonth(),
                        veteranCardDocument.getCardExpiryDateDay()));
        veteranCard.setServiceNumber(veteranCardDocument.getServiceNumber());
        veteranCard.setServiceBranch(veteranCardDocument.getServiceBranch());
        veteranCard.setPhoto(veteranCardDocument.getPhoto());
        veteranCards.add(veteranCard);
        return veteranCards;
    }

    private static @NotNull List<Name> buildNames(
            String title, String[] givenNames, String[] familyNames) {
        List<NamePart> nameParts = new ArrayList<>();
        if (title != null && !title.isEmpty()) {
            nameParts.add(setNamePart(title, "Title"));
        }
        return buildNames(givenNames, familyNames, nameParts);
    }

    private static @NotNull List<Name> buildNames(String[] givenNames, String[] familyNames) {
        return buildNames(givenNames, familyNames, new ArrayList<>());
    }

    private static List<Name> buildNames(
            String[] givenNames, String[] familyNames, List<NamePart> nameParts) {
        for (String name : givenNames) {
            nameParts.add(setNamePart(name, "GivenName"));
        }
        for (String name : familyNames) {
            nameParts.add(setNamePart(name, "FamilyName"));
        }
        Name name = new Name();
        name.setNameParts(nameParts);
        List<Name> names = new ArrayList<>();
        names.add(name);
        return names;
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
}
