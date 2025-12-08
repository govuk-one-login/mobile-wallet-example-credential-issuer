package uk.gov.di.mobile.wallet.cri.credential.mdoc.example_document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExampleDocumentTest {

    private static final String FAMILY_NAME = "Doe";
    private static final String GIVEN_NAME = "John";
    private static final String PORTRAIT = "base64EncodedPortraitString";
    private static final String BIRTH_DATE = "24-05-1985";
    private static final String ISSUE_DATE = "10-01-2020";
    private static final String EXPIRY_DATE = "09-01-2030";
    private static final String ISSUING_COUNTRY = "GB";
    private static final String DOCUMENT_NUMBER = "AB123456";
    private static final String TYPE_OF_FISH = "Salmon";
    private static final int NUMBER_OF_FISHING_RODS = 2;

    @Test
    void Should_CreateInstance_When_DataIsValid() {
        ExampleDocument document =
                new ExampleDocument(
                        FAMILY_NAME,
                        GIVEN_NAME,
                        PORTRAIT,
                        BIRTH_DATE,
                        ISSUE_DATE,
                        EXPIRY_DATE,
                        ISSUING_COUNTRY,
                        DOCUMENT_NUMBER,
                        TYPE_OF_FISH,
                        NUMBER_OF_FISHING_RODS);

        assertEquals(FAMILY_NAME, document.getFamilyName());
        assertEquals(GIVEN_NAME, document.getGivenName());
        assertArrayEquals(Base64.getDecoder().decode(PORTRAIT), document.getPortrait());
        assertEquals(LocalDate.of(1985, 5, 24), document.getBirthDate());
        assertEquals(LocalDate.of(2020, 1, 10), document.getIssueDate());
        assertEquals(LocalDate.of(2030, 1, 9), document.getExpiryDate());
        assertEquals(ISSUING_COUNTRY, document.getIssuingCountry());
        assertEquals(DOCUMENT_NUMBER, document.getDocumentNumber());
        assertEquals(TYPE_OF_FISH, document.getTypeOfFish());
        assertEquals(NUMBER_OF_FISHING_RODS, document.getNumberOfFishingRods());
    }

    @Test
    void Should_ThrowNullPointerException_When_FamilyNameIsNull() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new ExampleDocument(
                                null,
                                GIVEN_NAME,
                                PORTRAIT,
                                ISSUE_DATE,
                                ISSUE_DATE,
                                EXPIRY_DATE,
                                ISSUING_COUNTRY,
                                DOCUMENT_NUMBER,
                                TYPE_OF_FISH,
                                NUMBER_OF_FISHING_RODS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2020-01-01", "01/01/2020", "01.01.2020", "2020", "Jan 1, 2020", ""})
    void Should_ThrowDateTimeParseException_When_BirthDateFormatIsInvalid(String invalidDate) {
        assertThrows(
                DateTimeParseException.class,
                () ->
                        new ExampleDocument(
                                FAMILY_NAME,
                                GIVEN_NAME,
                                PORTRAIT,
                                invalidDate,
                                ISSUE_DATE,
                                EXPIRY_DATE,
                                ISSUING_COUNTRY,
                                DOCUMENT_NUMBER,
                                TYPE_OF_FISH,
                                NUMBER_OF_FISHING_RODS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2020-01-01", "01/01/2020", "01.01.2020", "2020", "Jan 1, 2020", ""})
    void Should_ThrowDateTimeParseException_When_IssueDateFormatIsInvalid(String invalidDate) {
        assertThrows(
                DateTimeParseException.class,
                () ->
                        new ExampleDocument(
                                FAMILY_NAME,
                                GIVEN_NAME,
                                PORTRAIT,
                                BIRTH_DATE,
                                invalidDate,
                                EXPIRY_DATE,
                                ISSUING_COUNTRY,
                                DOCUMENT_NUMBER,
                                TYPE_OF_FISH,
                                NUMBER_OF_FISHING_RODS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2020-01-01", "01/01/2020", "01.01.2020", "2020", "Jan 1, 2020", ""})
    void Should_ThrowDateTimeParseException_When_ExpiryDateFormatIsInvalid(String invalidDate) {
        assertThrows(
                DateTimeParseException.class,
                () ->
                        new ExampleDocument(
                                FAMILY_NAME,
                                GIVEN_NAME,
                                PORTRAIT,
                                BIRTH_DATE,
                                ISSUE_DATE,
                                invalidDate,
                                ISSUING_COUNTRY,
                                DOCUMENT_NUMBER,
                                TYPE_OF_FISH,
                                NUMBER_OF_FISHING_RODS));
    }
}
