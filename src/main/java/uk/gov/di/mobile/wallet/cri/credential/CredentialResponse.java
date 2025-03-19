package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CredentialResponse {
    private final String credential;
    private final String notificationId;

    public CredentialResponse(
            String credential, @JsonProperty("notification_id") String notificationId) {
        this.credential = credential;
        this.notificationId = notificationId;
    }
}
