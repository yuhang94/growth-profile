package io.growth.platform.profile.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDefinitionChangedEvent {

    private Long tagDefinitionId;
    private String tagKey;
    private ChangeType changeType;
    private LocalDateTime timestamp;

    public enum ChangeType {
        CREATED, UPDATED, STATUS_CHANGED
    }
}
