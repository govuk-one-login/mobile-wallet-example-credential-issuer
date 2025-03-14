package uk.gov.di.mobile.wallet.cri.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NotificationRequestBody {

    @JsonProperty("notification_id")
    private String notification_id;

    @JsonProperty("event")
    private String event;

    @JsonProperty("event_description")
    private String event_description;

    @JsonCreator
    public NotificationRequestBody(
            @JsonProperty(value = "notification_id", required = true) String notification_id,
            @JsonProperty(value = "event", required = true) String event,
            @JsonProperty(value = "event_description") String event_description) {
        this.notification_id = notification_id;
        this.event = event;
        this.event_description = event_description;
    }
}
