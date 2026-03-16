package io.growth.platform.profile.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TagDefinitionChanged extends ApplicationEvent {

    private final Long tagDefinitionId;
    private final String tagKey;
    private final ChangeType changeType;

    public TagDefinitionChanged(Object source, Long tagDefinitionId, String tagKey, ChangeType changeType) {
        super(source);
        this.tagDefinitionId = tagDefinitionId;
        this.tagKey = tagKey;
        this.changeType = changeType;
    }

    public enum ChangeType {
        CREATED, UPDATED, STATUS_CHANGED
    }
}
