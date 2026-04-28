CREATE TABLE IF NOT EXISTS `gp_profile_tag_definition` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `tag_key`      VARCHAR(128) NOT NULL COMMENT '标签键',
    `tag_name`     VARCHAR(128) NOT NULL COMMENT '标签名称',
    `tag_type`     VARCHAR(32)  NOT NULL COMMENT '标签类型',
    `category`     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '分类',
    `description`  VARCHAR(512)          DEFAULT NULL COMMENT '描述',
    `enum_values`  TEXT                  DEFAULT NULL COMMENT '枚举值JSON',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `created_by`   VARCHAR(64)           DEFAULT NULL COMMENT '创建人',
    `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_key` (`tag_key`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签定义表';

CREATE TABLE IF NOT EXISTS `gp_profile_segment` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `segment_name`       VARCHAR(128) NOT NULL COMMENT '分群名称',
    `description`        VARCHAR(512)          DEFAULT NULL COMMENT '描述',
    `condition_json`     TEXT         NOT NULL COMMENT '条件规则树JSON',
    `status`             TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `last_user_count`    BIGINT                DEFAULT NULL COMMENT '最近计算用户数',
    `last_computed_time` DATETIME              DEFAULT NULL COMMENT '最近计算时间',
    `created_by`         VARCHAR(64)           DEFAULT NULL COMMENT '创建人',
    `created_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_segment_name` (`segment_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户分群定义表';

CREATE TABLE IF NOT EXISTS `gp_profile_event_definition` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `event_name`      VARCHAR(128) NOT NULL COMMENT '事件名称(唯一标识)',
    `event_type`      VARCHAR(32)  NOT NULL COMMENT '事件类型',
    `display_name`    VARCHAR(128) NOT NULL COMMENT '显示名称',
    `description`     VARCHAR(512)          DEFAULT NULL COMMENT '描述',
    `properties_json` TEXT                  DEFAULT NULL COMMENT '属性定义JSON',
    `source_type`     VARCHAR(16)  NOT NULL DEFAULT 'SDK' COMMENT '来源类型 SDK/MQ',
    `mq_source_config_json` TEXT           DEFAULT NULL COMMENT 'MQ来源配置JSON',
    `usage_channels`  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '使用渠道，逗号分隔：PROFILE,CAMPAIGN',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `created_by`      VARCHAR(64)           DEFAULT NULL COMMENT '创建人',
    `created_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_name` (`event_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为事件定义表';
