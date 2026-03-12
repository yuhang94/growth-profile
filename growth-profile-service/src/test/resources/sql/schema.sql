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
