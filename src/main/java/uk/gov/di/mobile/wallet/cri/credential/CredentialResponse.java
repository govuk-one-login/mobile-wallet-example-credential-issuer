package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CredentialResponse {
    private final String credential;

    @JsonProperty("notification_id")
    private final String notificationId;

    public CredentialResponse(String credential, String notificationId) {
        this.credential = credential;
        this.notificationId = notificationId;
    }
}
