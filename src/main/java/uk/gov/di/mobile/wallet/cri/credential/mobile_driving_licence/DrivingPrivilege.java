package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.CredentialService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class DrivingPrivilege {

    private String vehicleCategoryCode;
    private Optional<LocalDate> issueDate;
    private Optional<LocalDate> expiryDate;

    private static final Logger LOGGER = LoggerFactory.getLogger(DrivingPrivilege.class);

    @JsonCreator
    public DrivingPrivilege(
            @JsonProperty("vehicle_category_code") String vehicleCategoryCode,
            @JsonProperty("issue_date") String issueDate,
            @JsonProperty("expiry_date") String expiryDate) {
        this.vehicleCategoryCode =
                Objects.requireNonNull(vehicleCategoryCode, "vehicle_category_code is required");
        this.issueDate = Optional.ofNullable(parseDate(issueDate));
        this.expiryDate = Optional.ofNullable(parseDate(expiryDate));
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null) {
            return null;
        }

        try {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(dateString, dateFormat);
        } catch (DateTimeParseException exception) {
            getLogger().error("Date string {} is invalid", dateString);
            return null;
        }
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
