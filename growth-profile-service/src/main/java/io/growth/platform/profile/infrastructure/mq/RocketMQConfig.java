package io.growth.platform.profile.infrastructure.mq;

public final class RocketMQConfig {

    private RocketMQConfig() {
    }

    public static final String TOPIC_TAG_VALUE_WRITE = "profile-tag-value-write";
    public static final String TOPIC_TAG_VALUE_CHANGED = "profile-tag-value-changed";
    public static final String TOPIC_TAG_DEFINITION_CHANGED = "profile-tag-definition-changed";
    public static final String TOPIC_BEHAVIOR_EVENT = "profile-behavior-event";
    public static final String TOPIC_BEHAVIOR_EVENT_NORMALIZED = "profile-behavior-event-normalized";

    public static final String CONSUMER_GROUP_TAG_VALUE_WRITE = "profile-tag-value-write-cg";
    public static final String CONSUMER_GROUP_BEHAVIOR_EVENT = "profile-behavior-event-cg";
}
