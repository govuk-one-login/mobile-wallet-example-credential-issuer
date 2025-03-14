package uk.gov.di.mobile.wallet.cri.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NotificationRequestBody {

    @JsonProperty("notification_id")
    private String notificationId;

    @JsonProperty("event")
    private String event;

    @JsonProperty("event_description")
    private String eventDescription;

    @JsonCreator
    public NotificationRequestBody(
            @JsonProperty(value = "notification_id") String notification_id,
            @JsonProperty(value = "event") String event,
            @JsonProperty(value = "event_description") String event_description) {
        this.notificationId = notification_id;
        this.event = event;
        this.eventDescription = event_description;
    }
}
