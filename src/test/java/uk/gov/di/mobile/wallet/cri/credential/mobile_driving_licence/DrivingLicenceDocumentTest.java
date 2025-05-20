package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DrivingLicenceDocumentTest {

    private static final String FAMILY_NAME = "Doe";
    private static final String GIVEN_NAME = "John";
    private static final String PORTRAIT = "base64EncodedPortraitString";
    private static final String BIRTH_DATE = "24-05-1985";
    private static final String BIRTH_PLACE = "London";
    private static final String ISSUE_DATE = "10-01-2020";
    private static final String EXPIRY_DATE = "09-01-2030";
    private static final String ISSUING_AUTHORITY = "DVLA";
    private static final String ISSUING_COUNTRY = "GB";
    private static final String DOCUMENT_NUMBER = "AB123456";
    private static final String[] RESIDENT_ADDRESS = {"123 Main St", "Apt 4B"};
    private static final String RESIDENT_POSTAL_CODE = "SW1A 1AA";
    private static final String RESIDENT_CITY = "London";
    private static final DrivingPrivilege[] DRIVING_PRIVILEGES = {
        mock(DrivingPrivilege.class), mock(DrivingPrivilege.class)
    };
    private static final String UN_DISTINGUISHING_SIGN = "UK";
    private static final DrivingPrivilege[] PROVISIONAL_DRIVING_PRIVILEGES = {
        mock(DrivingPrivilege.class), mock(DrivingPrivilege.class)
    };

    @Test
    void Should_CreateInstance_When_DataIsValid() {
        DrivingLicenceDocument document =
                new DrivingLicenceDocument(
                        FAMILY_NAME,
                        GIVEN_NAME,
                        PORTRAIT,
                        BIRTH_DATE,
                        BIRTH_PLACE,
                        ISSUE_DATE,
                        EXPIRY_DATE,
                        ISSUING_AUTHORITY,
                        ISSUING_COUNTRY,
                        DOCUMENT_NUMBER,
                        RESIDENT_ADDRESS,
                        RESIDENT_POSTAL_CODE,
                        RESIDENT_CITY,
                        DRIVING_PRIVILEGES,
                        UN_DISTINGUISHING_SIGN,
                        PROVISIONAL_DRIVING_PRIVILEGES);

        assertEquals(FAMILY_NAME, document.getFamilyName());
        assertEquals(GIVEN_NAME, document.getGivenName());
        assertEquals(PORTRAIT, document.getPortrait());
        assertEquals(LocalDate.of(1985, 5, 24), document.getBirthDate());
        assertEquals(BIRTH_PLACE, document.getBirthPlace());
        assertEquals(LocalDate.of(2020, 1, 10), document.getIssueDate());
        assertEquals(LocalDate.of(2030, 1, 9), document.getExpiryDate());
        assertEquals(ISSUING_AUTHORITY, document.getIssuingAuthority());
        assertEquals(ISSUING_COUNTRY, document.getIssuingCountry());
        assertEquals(DOCUMENT_NUMBER, document.getDocumentNumber());
        assertEquals("123 Main St, Apt 4B", document.getResidentAddress());
        assertEquals(RESIDENT_POSTAL_CODE, document.getResidentPostalCode());
        assertEquals(RESIDENT_CITY, document.getResidentCity());
        assertArrayEquals(DRIVING_PRIVILEGES, document.getDrivingPrivileges());
    }

    @Test
    void Should_CreateInstanceWithSingleLineAddress() {
        String[] singleLineAddress = {"123 Main St"};

        DrivingLicenceDocument document =
                new DrivingLicenceDocument(
                        FAMILY_NAME,
                        GIVEN_NAME,
                        PORTRAIT,
                        BIRTH_DATE,
                        BIRTH_PLACE,
                        ISSUE_DATE,
                        EXPIRY_DATE,
                        ISSUING_AUTHORITY,
                        ISSUING_COUNTRY,
                        DOCUMENT_NUMBER,
                        singleLineAddress,
                        RESIDENT_POSTAL_CODE,
                        RESIDENT_CITY,
                        DRIVING_PRIVILEGES,
                        UN_DISTINGUISHING_SIGN,
                        PROVISIONAL_DRIVING_PRIVILEGES);

        assertEquals("123 Main St", document.getResidentAddress());
    }

    @Test
    void Should_CreateInstanceWithMultiLineAddress() {
        String[] multiLineAddress = {"123 Main St", "Floor 2", "Suite 301"};

        DrivingLicenceDocument document =
                new DrivingLicenceDocument(
                        FAMILY_NAME,
                        GIVEN_NAME,
                        PORTRAIT,
                        BIRTH_DATE,
                        BIRTH_PLACE,
                        ISSUE_DATE,
                        EXPIRY_DATE,
                        ISSUING_AUTHORITY,
                        ISSUING_COUNTRY,
                        DOCUMENT_NUMBER,
                        multiLineAddress,
                        RESIDENT_POSTAL_CODE,
                        RESIDENT_CITY,
                        DRIVING_PRIVILEGES,
                        UN_DISTINGUISHING_SIGN,
                        PROVISIONAL_DRIVING_PRIVILEGES);

        assertEquals("123 Main St, Floor 2, Suite 301", document.getResidentAddress());
    }

    @Test
    void Should_CreateInstanceWithEmptyProvisionalDrivingPrivileges_When_IsNull() {
        DrivingPrivilege[] provisionalDrivingPrivileges = null;

        DrivingLicenceDocument document =
                new DrivingLicenceDocument(
                        FAMILY_NAME,
                        GIVEN_NAME,
                        PORTRAIT,
                        BIRTH_DATE,
                        BIRTH_PLACE,
                        ISSUE_DATE,
                        EXPIRY_DATE,
                        ISSUING_AUTHORITY,
                        ISSUING_COUNTRY,
                        DOCUMENT_NUMBER,
                        RESIDENT_ADDRESS,
                        RESIDENT_POSTAL_CODE,
                        RESIDENT_CITY,
                        DRIVING_PRIVILEGES,
                        UN_DISTINGUISHING_SIGN,
                        provisionalDrivingPrivileges);

        assertEquals(Optional.empty(), document.getProvisionalDrivingPrivileges());
    }

    @Test
    void Should_ThrowNullPointerException_When_FamilyNameIsNull() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new DrivingLicenceDocument(
                                null,
                                GIVEN_NAME,
                                PORTRAIT,
                                ISSUE_DATE,
                                BIRTH_PLACE,
                                ISSUE_DATE,
                                EXPIRY_DATE,
                                ISSUING_AUTHORITY,
                                ISSUING_COUNTRY,
                                DOCUMENT_NUMBER,
                                RESIDENT_ADDRESS,
                                RESIDENT_POSTAL_CODE,
                                RESIDENT_CITY,
                                DRIVING_PRIVILEGES,
                                UN_DISTINGUISHING_SIGN,
                                PROVISIONAL_DRIVING_PRIVILEGES));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2020-01-01", "01/01/2020", "01.01.2020", "2020", "Jan 1, 2020", ""})
    void Should_ThrowDateTimeParseException_When_BirthDateFormatIsInvalid(String invalidDate) {
        assertThrows(
                DateTimeParseException.class,
                () ->
                        new DrivingLicenceDocument(
                                FAMILY_NAME,
                                GIVEN_NAME,
                                PORTRAIT,
                                invalidDate,
                                BIRTH_PLACE,
                                ISSUE_DATE,
                                EXPIRY_DATE,
                                ISSUING_AUTHORITY,
                                ISSUING_COUNTRY,
                                DOCUMENT_NUMBER,
                                RESIDENT_ADDRESS,
                                RESIDENT_POSTAL_CODE,
                                RESIDENT_CITY,
                                DRIVING_PRIVILEGES,
                                UN_DISTINGUISHING_SIGN,
                                PROVISIONAL_DRIVING_PRIVILEGES));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2020-01-01", "01/01/2020", "01.01.2020", "2020", "Jan 1, 2020", ""})
    void Should_ThrowDateTimeParseException_When_IssueDateFormatIsInvalid(String invalidDate) {
        assertThrows(
                DateTimeParseException.class,
                () ->
                        new DrivingLicenceDocument(
                                FAMILY_NAME,
                                GIVEN_NAME,
                                PORTRAIT,
                                BIRTH_DATE,
                                BIRTH_PLACE,
                                invalidDate,
                                EXPIRY_DATE,
                                ISSUING_AUTHORITY,
                                ISSUING_COUNTRY,
                                DOCUMENT_NUMBER,
                                RESIDENT_ADDRESS,
                                RESIDENT_POSTAL_CODE,
                                RESIDENT_CITY,
                                DRIVING_PRIVILEGES,
                                UN_DISTINGUISHING_SIGN,
                                PROVISIONAL_DRIVING_PRIVILEGES));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2020-01-01", "01/01/2020", "01.01.2020", "2020", "Jan 1, 2020", ""})
    void Should_ThrowDateTimeParseException_When_ExpiryDateFormatIsInvalid(String invalidDate) {
        assertThrows(
                DateTimeParseException.class,
                () ->
                        new DrivingLicenceDocument(
                                FAMILY_NAME,
                                GIVEN_NAME,
                                PORTRAIT,
                                BIRTH_DATE,
                                BIRTH_PLACE,
                                ISSUE_DATE,
                                invalidDate,
                                ISSUING_AUTHORITY,
                                ISSUING_COUNTRY,
                                DOCUMENT_NUMBER,
                                RESIDENT_ADDRESS,
                                RESIDENT_POSTAL_CODE,
                                RESIDENT_CITY,
                                DRIVING_PRIVILEGES,
                                UN_DISTINGUISHING_SIGN,
                                PROVISIONAL_DRIVING_PRIVILEGES));
    }
}
