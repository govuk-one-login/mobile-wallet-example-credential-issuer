package testUtils;

import org.jetbrains.annotations.NotNull;
import uk.gov.di.mobile.wallet.cri.credential.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockDocuments {

    public static @NotNull Document getMockSocialSecurityDocument(String documentId) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("familyName", "Edwards Green");
        data.put("givenName", "Sarah Elizabeth");
        data.put("nino", "QQ123456C");
        data.put("title", "Miss");
        data.put("credentialTtlMinutes", "525600");
        return new Document(documentId, data, "SocialSecurityCredential");
    }

    public static @NotNull Document getMockBasicCheckDocument(String documentId) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("issuance-day", "11");
        data.put("issuance-month", "07");
        data.put("issuance-year", "2024");
        data.put("expiration-day", "11");
        data.put("expiration-month", "07");
        data.put("expiration-year", "2025");
        data.put("birth-day", "05");
        data.put("birth-month", "12");
        data.put("birth-year", "1970");
        data.put("firstName", "Bonnie");
        data.put("lastName", "Blue");
        data.put("subBuildingName", "Flat 11");
        data.put("buildingName", "Blashford");
        data.put("streetName", "Adelaide Road");
        data.put("addressLocality", "London");
        data.put("addressCountry", "GB");
        data.put("postalCode", "NW3 3RX");
        data.put("certificateNumber", "009878863");
        data.put("applicationNumber", "E0023455534");
        data.put("certificateType", "basic");
        data.put("outcome", "Result clear");
        data.put("policeRecordsCheck", "Clear");
        data.put("credentialTtlMinutes", "525600");
        return new Document(documentId, data, "BasicCheckCredential");
    }

    public static @NotNull Document getMockVeteranCardDocument(String documentId) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("cardExpiryDate-day", "11");
        data.put("cardExpiryDate-month", "07");
        data.put("cardExpiryDate-year", "2000");
        data.put("dateOfBirth-day", "05");
        data.put("dateOfBirth-month", "12");
        data.put("dateOfBirth-year", "1970");
        data.put("givenName", "Bonnie");
        data.put("familyName", "Blue");
        data.put("serviceNumber", "25057386");
        data.put("serviceBranch", "HM Naval Service");
        data.put("photo", "base64EncodedPhoto");
        data.put("credentialTtlMinutes", "525600");
        return new Document(documentId, data, "digitalVeteranCard");
    }

    public static @NotNull Document getMockMobileDrivingLicence(String documentId) {
        List<Map<String, String>> drivingPrivileges = new ArrayList<>();
        Map<String, String> drivingPrivilege = new HashMap<>();
        drivingPrivilege.put("vehicle_category_code", "A");
        drivingPrivileges.add(drivingPrivilege);

        HashMap<String, Object> data = new HashMap<>();
        data.put("family_name", "Edwards");
        data.put("given_name", "Sarah Ann");
        data.put("portrait", "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg==");
        data.put("birth_date", "01-02-2000");
        data.put("birth_place", "London");
        data.put("issue_date", "08-04-2020");
        data.put("expiry_date", "08-04-2025");
        data.put("issuing_authority", "TEST");
        data.put("issuing_country", "GB");
        data.put("document_number", "123456789");
        data.put("resident_address", new String[] {"Flat 2a", "64 Berry Street"});
        data.put("resident_postal_code", "N1 7FN");
        data.put("resident_city", "London");
        data.put("driving_privileges", drivingPrivileges);
        data.put("un_distinguishing_sign", "UK");

        return new Document(documentId, data, "mobileDrivingLicence");
    }

    public static @NotNull Document getMockDocumentWithInvalidVcType(String documentId) {
        HashMap<String, Object> data = new HashMap<>();
        return new Document(documentId, data, "SomeOtherVcType");
    }
}
