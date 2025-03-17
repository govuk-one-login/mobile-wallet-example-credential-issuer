package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CredentialResponse {

    private final Credential credential;
    @Getter private final String notificationId;

    public CredentialResponse(
            Credential credential, @JsonProperty("notification_id") String notificationId) {

        this.credential = credential;
        this.notificationId = notificationId;
    }
}
