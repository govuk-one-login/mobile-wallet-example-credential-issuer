package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.di.mobile.wallet.cri.util.CredentialsContainer;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CredentialResponse {
    private final String credential;
    private final ArrayList<CredentialsContainer> credentials;
    private final String notificationId;

    public CredentialResponse(String credential, String notificationId) {
        this.credential = credential;
        this.notificationId = notificationId;
        this.credentials = new ArrayList<>(List.of(new CredentialsContainer(credential)));
    }

    @JsonProperty("notification_id")
    public String getNotificationId() {
        return notificationId;
    }
}

