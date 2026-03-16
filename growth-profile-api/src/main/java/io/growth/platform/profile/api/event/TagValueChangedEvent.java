package io.growth.platform.profile.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagValueChangedEvent {

    private String userId;
    private String tagKey;
    private String tagValue;
    private ChangeType changeType;
    private LocalDateTime timestamp;

    public enum ChangeType {
        PUT, DELETE
    }
}
