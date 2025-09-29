package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CredentialResponse {
    private final ArrayList<Credential> credentials;

    @JsonProperty("notification_id")
    private final String notificationId;

    @JsonCreator
    public CredentialResponse(
            @JsonProperty("credential") String credential,
            @JsonProperty("notification_id") String notificationId) {
        this.notificationId = notificationId;
        this.credentials = new ArrayList<>(List.of(new Credential(credential)));
    }
}
