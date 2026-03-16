package io.growth.platform.profile.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TagValueChanged extends ApplicationEvent {

    private final String userId;
    private final String tagKey;
    private final String tagValue;
    private final ChangeType changeType;

    public TagValueChanged(Object source, String userId, String tagKey, String tagValue, ChangeType changeType) {
        super(source);
        this.userId = userId;
        this.tagKey = tagKey;
        this.tagValue = tagValue;
        this.changeType = changeType;
    }

    public enum ChangeType {
        PUT, DELETE
    }
}
