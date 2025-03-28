package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CredentialResponse {
    private final String credential; // NOSONAR
    private final String notificationId; // NOSONAR

    public CredentialResponse(String credential, String notificationId) {
        this.credential = credential;
        this.notificationId = notificationId;
    }

    @JsonProperty("notification_id")
    public String getNotificationId() {
        return notificationId;
    }
}
