package uk.gov.di.mobile.wallet.cri.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class NotificationRequestBody {

    @JsonProperty("notification_id")
    private String notificationId;

    @JsonProperty("event")
    private EventType event;

    @JsonProperty("event_description")
    @Size(max = 1000)
    private String eventDescription;

    @JsonCreator
    public NotificationRequestBody(
            @JsonProperty(value = "notification_id") String notificationId,
            @JsonProperty(value = "event") EventType event,
            @JsonProperty(value = "event_description") String eventDescription) {
        this.notificationId = notificationId;
        this.event = event;
        this.eventDescription = eventDescription;
    }
}
