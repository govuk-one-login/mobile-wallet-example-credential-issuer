package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DrivingPrivilegeTest {

    @Mock private Logger mockLogger;

    private static final String VEHICLE_CATEGORY_CODE = "B";
    private static final String ISSUE_DATE = "02-01-2025";
    private static final String EXPIRY_DATE = "01-01-2035";
    private static final List<Code> CODES = List.of(new Code("01"), new Code("22(7)"));
    private static final LocalDate EXPECTED_ISSUE_DATE = LocalDate.of(2025, 1, 2);
    private static final LocalDate EXPECTED_EXPIRY_DATE = LocalDate.of(2035, 1, 1);

    @Test
    void Should_CreateInstance_When_DataIsValid() {
        DrivingPrivilege privilege =
                new DrivingPrivilege(VEHICLE_CATEGORY_CODE, ISSUE_DATE, EXPIRY_DATE, CODES);

        assertEquals(VEHICLE_CATEGORY_CODE, privilege.getVehicleCategoryCode());
        assertTrue(privilege.getIssueDate().isPresent());
        assertEquals(EXPECTED_ISSUE_DATE, privilege.getIssueDate().get());
        assertTrue(privilege.getExpiryDate().isPresent());
        assertEquals(EXPECTED_EXPIRY_DATE, privilege.getExpiryDate().get());
        assertEquals(CODES, privilege.getCodes().get());
    }

    @Test
    void Should_ThrowNullPointerException_When_VehicleCategoryCodeIsNull() {
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new DrivingPrivilege(null, ISSUE_DATE, EXPIRY_DATE, CODES));
        assertEquals("vehicle_category_code is required", exception.getMessage());
    }

    @Test
    void Should_CreateInstanceWithEmptyOptionals_When_DatesAreNull() {
        DrivingPrivilege privilege = new DrivingPrivilege(VEHICLE_CATEGORY_CODE, null, null, CODES);

        assertEquals(VEHICLE_CATEGORY_CODE, privilege.getVehicleCategoryCode());
        assertEquals(Optional.empty(), privilege.getIssueDate());
        assertEquals(Optional.empty(), privilege.getExpiryDate());
        assertEquals(CODES, privilege.getCodes().get());
    }

    @Test
    void Should_CreateInstanceWithCodesAsOptionalEmpty_When_CodesIsNull() {
        DrivingPrivilege privilege =
                new DrivingPrivilege(VEHICLE_CATEGORY_CODE, ISSUE_DATE, EXPIRY_DATE, null);

        assertEquals(VEHICLE_CATEGORY_CODE, privilege.getVehicleCategoryCode());
        assertEquals(EXPECTED_ISSUE_DATE, privilege.getIssueDate().get());
        assertEquals(EXPECTED_EXPIRY_DATE, privilege.getExpiryDate().get());
        assertEquals(Optional.empty(), privilege.getCodes());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "2025-01-01",
                "01/01/2025",
                "01.01.2025",
                "2025",
                "Jan 1, 2025",
                "",
                "A-B-C"
            })
    void Should_CreateInstanceWithIssueDateAsOptionalEmpty_When_IssueDateFormatIsInvalid(
            String invalidDate) {
        DrivingPrivilege privilege =
                new DrivingPrivilege(VEHICLE_CATEGORY_CODE, invalidDate, EXPIRY_DATE, CODES) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };

        assertEquals(Optional.empty(), privilege.getIssueDate());
        verify(mockLogger).error("Date string {} is invalid", invalidDate);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "2025-01-01",
                "01/01/2025",
                "01.01.2025",
                "2025",
                "Jan 1, 2025",
                "",
                "A-B-C"
            })
    void Should_CreateInstanceWithExpiryDateAsOptionalEmpty_When_ExpiryDateFormatIsInvalid(
            String invalidDate) {
        DrivingPrivilege privilege =
                new DrivingPrivilege(VEHICLE_CATEGORY_CODE, ISSUE_DATE, invalidDate, CODES) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };

        assertEquals(Optional.empty(), privilege.getExpiryDate());
        verify(mockLogger).error("Date string {} is invalid", invalidDate);
    }
}
