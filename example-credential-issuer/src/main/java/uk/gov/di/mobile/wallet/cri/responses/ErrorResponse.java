package uk.gov.di.mobile.wallet.cri.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorResponse {

    private String error;

    // Default constructor (required for JSON serialization)
    public ErrorResponse() {}

    public ErrorResponse(String error) {
        this.error = error;
    }
}
