# 用户画像支撑营销引擎技术方案 v1

## 一、背景与目标

当前平台已经按营销域拆分出 `growth-profile`、`growth-campaign`、`growth-message`、`growth-coupon`、`growth-incentive`、`growth-frequency` 等模块。

其中：

- `growth-profile` 已具备事件定义、事件接入、事件存储、标签、分群查询等基础能力
- `growth-campaign` 当前仅有工程骨架，尚未形成完整的营销触发与动作编排能力

本方案目标是明确：

1. 营销引擎应由哪个模块承载
2. 用户行为事件是否继续由 `growth-profile` 统一定义与接入
3. 指定人群是否继续使用 `growth-profile` 的分群能力
4. `growth-profile` 需要补哪些能力，才能稳定支撑营销引擎

---

## 二、结论

### 2.1 模块职责结论

| 模块 | 推荐职责 |
|------|---------|
| `growth-profile` | 用户事件定义、事件接入、事件标准化、事件存储、标签、人群分层、人群命中查询 |
| `growth-campaign` | 触发规则、活动配置、动作编排、执行记录、幂等、重试、审计 |
| `growth-message` | 消息类动作执行器（站内信、Push、短信、邮件等） |
| `growth-coupon` | 发券动作执行器 |
| `growth-incentive` | 积分/权益/奖励动作执行器 |
| `growth-frequency` | 频控、配额与节流能力 |

### 2.2 关键判断

1. 营销引擎应落在 `growth-campaign`，不应继续堆在 `growth-profile`
2. 用户行为事件继续使用 `growth-profile` 中的事件定义与接入能力，是合适的
3. 指定人群继续使用 `growth-profile` 中的人群分层能力，是合适的
4. `growth-profile` 还需要补两项关键输出能力：
   - 标准化行为事件广播
   - 单用户/批量用户分群命中查询

---

## 三、设计原则

### 3.1 单一事实来源

- `eventName` 的定义、属性、接入方式只在 `growth-profile` 维护
- `segmentId` 的定义、圈选规则、计算逻辑只在 `growth-profile` 维护
- `growth-campaign` 只引用 `eventName` 和 `segmentId`，不复制事件 schema 与分群规则

### 3.2 松耦合集成

- 营销触发使用 MQ 订阅标准化事件
- 人群命中判断使用 `growth-profile` API 或预计算结果
- `growth-campaign` 不直接查询 `growth-profile` 底层表

### 3.3 先闭环，后增强

MVP 先支持：

1. 发生指定事件
2. 判断用户是否属于指定分群
3. 命中后执行一组动作

后续再扩展：

- 实时分群成员变更广播
- 工作流节点编排
- 分群快照
- 多阶段 Journey

---

## 四、当前系统现状

### 4.1 `growth-profile` 已有能力

| 能力 | 现状 |
|------|------|
| 事件定义 | 已有 `gp_profile_event_definition` |
| 行为事件存储 | 已有 ClickHouse 表 `gp_profile_behavior_event` |
| 标签定义 | 已有 `gp_profile_tag_definition` |
| 标签值存储 | 已有 HBase 表 `gp_profile_tag_value` |
| 标签宽表 | 已有 ClickHouse 表 `gp_profile_tag_wide` |
| 分群定义 | 已有 `gp_profile_segment` |
| 分群查询 | 已支持按标签、行为事件、时间窗、次数查询用户 |
| MQ topic | 已有 `profile-tag-value-write`、`profile-tag-value-changed`、`profile-tag-definition-changed`、`profile-behavior-event` |

### 4.2 当前不足

| 不足项 | 影响 |
|--------|------|
| 没有统一的“标准化行为事件出口” | `growth-campaign` 无法稳定订阅营销触发源 |
| 没有“单用户是否命中分群”接口 | 实时触发时无法低成本判断用户是否属于目标人群 |
| 没有“分群成员变更事件” | 无法支撑高实时分群驱动的营销场景 |
| 分群结果主要依赖实时 SQL 查询 | 高并发实时触发时可能有性能压力 |

---

## 五、目标架构

### 5.1 总体边界

```text
业务系统 / SDK / 外部 MQ
    -> growth-profile
       -> 事件定义校验
       -> 标准化
       -> 事件落库
       -> 广播标准化事件
       -> 提供分群命中查询
    -> growth-campaign
       -> 根据 eventName 匹配触发规则
       -> 根据 segmentId 判断人群命中
       -> 调频控
       -> 编排动作
       -> 记录执行状态
    -> growth-message / growth-coupon / growth-incentive
       -> 执行动作
```

### 5.2 关键职责拆分

#### `growth-profile`

- 定义事件
- 接收 HTTP / MQ / 后续 Webhook 事件
- 将事件转换为标准模型
- 将标准事件写入 ClickHouse
- 将标准事件发布到营销触发 topic
- 提供分群预览、分群用户查询、分群命中判断

#### `growth-campaign`

- 维护 campaign、trigger、action
- 根据标准化事件触发营销规则
- 判断用户是否命中 segment
- 做幂等、频控、执行编排、失败重试
- 下发动作执行命令给 message/coupon/incentive

---

## 六、Topic 设计

### 6.1 沿用现有 topic

| Topic | 说明 |
|------|------|
| `profile-tag-value-write` | 标签值写入入口 |
| `profile-tag-value-changed` | 标签值变化广播 |
| `profile-tag-definition-changed` | 标签定义变化广播 |
| `profile-behavior-event` | 固定格式行为事件写入入口 |

### 6.2 新增 topic

| Topic | 生产者 | 消费者 | 作用 |
|------|--------|--------|------|
| `profile-behavior-event-normalized` | `growth-profile` | `growth-campaign`、分析下游 | 标准化行为事件统一出口 |
| `profile-segment-membership-changed` | `growth-profile` | `growth-campaign` | 可选，分群成员变更广播 |
| `campaign-triggered` | `growth-campaign` | 审计/看板 | 记录一次命中触发 |
| `campaign-action-execute` | `growth-campaign` | message/coupon/incentive adapter | 动作执行命令 |
| `campaign-action-result` | 动作执行器 | `growth-campaign` | 动作执行结果回传 |
| `campaign-dead-letter` | 各模块 | 运维补偿 | 无法正常处理的失败消息 |

### 6.3 标准化行为事件消息体

```json
{
  "traceId": "e8fd3b9c7b7442a9989d5d8898e1f001",
  "eventId": "9d7d2db1-f31a-4b82-8d7e-0c4206f7d4fa",
  "tenantId": "default",
  "userId": "u10001",
  "eventName": "order_paid",
  "eventType": "ORDER",
  "occurredAt": "2026-03-23T12:00:00",
  "sourceType": "SDK",
  "sourceName": "order-service",
  "properties": {
    "orderId": "o20260323001",
    "amount": "188.00",
    "channel": "app"
  }
}
```

### 6.4 分群成员变更消息体

```json
{
  "traceId": "42d0dfb2f2c64d2484e85d1478dcb61f",
  "tenantId": "default",
  "segmentId": 1001,
  "userId": "u10001",
  "changeType": "ENTER",
  "version": 12,
  "occurredAt": "2026-03-23T12:00:00"
}
```

---

## 七、表结构设计

### 7.1 `growth-profile` 现有核心表

| 表名 | 作用 |
|------|------|
| `gp_profile_event_definition` | 事件定义 |
| `gp_profile_behavior_event` | 标准化行为事件 |
| `gp_profile_tag_definition` | 标签定义 |
| `gp_profile_tag_value` | 标签值 HBase 存储 |
| `gp_profile_tag_wide` | 标签宽表 |
| `gp_profile_segment` | 分群定义 |

### 7.2 `growth-profile` 新增建议表

#### 7.2.1 分群成员表

表名：`gp_profile_segment_membership`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `segment_id` | bigint | 分群 ID |
| `user_id` | varchar(64) | 用户 ID |
| `membership_status` | tinyint | 1-命中 0-未命中 |
| `version` | bigint | 分群计算版本 |
| `source` | varchar(32) | `BATCH` / `STREAM` |
| `updated_time` | datetime | 更新时间 |
| `created_time` | datetime | 创建时间 |

用途：

- 支撑 `segments/match`
- 降低实时活动触发时对 ClickHouse 动态 SQL 的依赖
- 为 `profile-segment-membership-changed` 提供可比对基线

#### 7.2.2 行为事件发布 outbox 表

表名：`gp_profile_behavior_event_outbox`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `event_id` | varchar(64) | 行为事件 ID |
| `topic` | varchar(128) | 目标 topic |
| `payload_json` | text | 消息体 |
| `publish_status` | tinyint | 0-待发布 1-成功 2-失败 |
| `retry_count` | int | 重试次数 |
| `next_retry_time` | datetime | 下次重试时间 |
| `created_time` | datetime | 创建时间 |
| `updated_time` | datetime | 更新时间 |

用途：

- 保证“事件落库 + 事件广播”的最终一致性
- 避免仅靠内存事件导致消息丢失

### 7.3 `growth-campaign` 新增核心表

#### 7.3.1 活动定义

表名：`gp_campaign_definition`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `campaign_code` | varchar(64) | 活动编码 |
| `campaign_name` | varchar(128) | 活动名称 |
| `description` | varchar(512) | 描述 |
| `trigger_type` | varchar(32) | `EVENT` / `SCHEDULE` |
| `status` | tinyint | `DRAFT` / `ONLINE` / `OFFLINE` |
| `priority` | int | 优先级 |
| `start_time` | datetime | 生效开始时间 |
| `end_time` | datetime | 生效结束时间 |
| `created_by` | varchar(64) | 创建人 |
| `created_time` | datetime | 创建时间 |
| `updated_time` | datetime | 更新时间 |

#### 7.3.2 触发规则

表名：`gp_campaign_trigger_rule`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `campaign_id` | bigint | 活动 ID |
| `event_name` | varchar(128) | 触发事件名，引用 profile event |
| `segment_id` | bigint | 目标分群 ID，引用 profile segment |
| `dedup_window_sec` | int | 幂等时间窗 |
| `trigger_condition_json` | text | 额外触发条件 |
| `status` | tinyint | 状态 |
| `created_time` | datetime | 创建时间 |
| `updated_time` | datetime | 更新时间 |

#### 7.3.3 动作定义

表名：`gp_campaign_action`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `campaign_id` | bigint | 活动 ID |
| `step_no` | int | 步骤序号 |
| `action_type` | varchar(32) | `SEND_MESSAGE` / `ISSUE_COUPON` / `GRANT_INCENTIVE` |
| `action_provider` | varchar(64) | 下游执行器 |
| `action_config_json` | text | 动作配置 |
| `status` | tinyint | 状态 |
| `created_time` | datetime | 创建时间 |
| `updated_time` | datetime | 更新时间 |

#### 7.3.4 触发执行实例

表名：`gp_campaign_execution`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `campaign_id` | bigint | 活动 ID |
| `trigger_rule_id` | bigint | 触发规则 ID |
| `trigger_event_id` | varchar(64) | 触发事件 ID |
| `user_id` | varchar(64) | 用户 ID |
| `segment_id` | bigint | 命中的目标分群 |
| `execution_status` | varchar(32) | `INIT` / `RUNNING` / `SUCCESS` / `PARTIAL_FAIL` / `FAIL` |
| `trace_id` | varchar(64) | 链路追踪 ID |
| `trigger_time` | datetime | 触发时间 |
| `finished_time` | datetime | 完成时间 |
| `fail_reason` | varchar(1024) | 失败原因 |
| `created_time` | datetime | 创建时间 |
| `updated_time` | datetime | 更新时间 |

#### 7.3.5 动作执行记录

表名：`gp_campaign_action_execution`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `execution_id` | bigint | 执行实例 ID |
| `campaign_action_id` | bigint | 动作定义 ID |
| `action_type` | varchar(32) | 动作类型 |
| `provider_request_id` | varchar(64) | 下游请求号 |
| `action_status` | varchar(32) | `INIT` / `SUCCESS` / `FAIL` |
| `request_payload` | text | 请求报文 |
| `response_payload` | text | 返回报文 |
| `retry_count` | int | 重试次数 |
| `executed_time` | datetime | 执行时间 |
| `created_time` | datetime | 创建时间 |
| `updated_time` | datetime | 更新时间 |

#### 7.3.6 幂等记录

表名：`gp_campaign_idempotent_record`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `idempotent_key` | varchar(256) | 幂等键 |
| `campaign_id` | bigint | 活动 ID |
| `user_id` | varchar(64) | 用户 ID |
| `event_id` | varchar(64) | 事件 ID |
| `expire_at` | datetime | 过期时间 |
| `created_time` | datetime | 创建时间 |

推荐幂等键：

`campaignId + userId + triggerEventId + actionType`

---

## 八、接口设计

### 8.1 `growth-profile` 保留/增强接口

#### 8.1.1 现有接口继续保留

- `POST /api/v1/profile/behavior-events`
- `POST /api/v1/profile/behavior-events/batch`
- `GET /api/v1/profile/behavior-events`
- `GET /api/v1/profile/event-definitions/{eventName}`
- `POST /api/v1/profile/segments`
- `POST /api/v1/profile/segments/preview`
- `GET /api/v1/profile/segments/{id}/users`

#### 8.1.2 新增接口：单用户分群命中判断

`POST /api/v1/profile/segments/match`

请求：

```json
{
  "segmentId": 1001,
  "userId": "u10001",
  "contextTime": "2026-03-23T12:00:00"
}
```

响应：

```json
{
  "matched": true,
  "segmentId": 1001,
  "userId": "u10001",
  "version": 12
}
```

#### 8.1.3 新增接口：批量分群命中判断

`POST /api/v1/profile/segments/batch-match`

请求：

```json
{
  "segmentId": 1001,
  "userIds": ["u10001", "u10002", "u10003"]
}
```

响应：

```json
{
  "segmentId": 1001,
  "results": [
    { "userId": "u10001", "matched": true, "version": 12 },
    { "userId": "u10002", "matched": false, "version": 12 },
    { "userId": "u10003", "matched": true, "version": 12 }
  ]
}
```

#### 8.1.4 新增接口：用户所属分群查询

`GET /api/v1/profile/users/{userId}/segments`

响应：

```json
{
  "userId": "u10001",
  "segments": [
    { "segmentId": 1001, "segmentName": "近30天下单用户" },
    { "segmentId": 1008, "segmentName": "VIP用户" }
  ]
}
```

### 8.2 `growth-campaign` 接口

#### 8.2.1 活动配置接口

- `POST /api/v1/campaigns`
- `PUT /api/v1/campaigns/{id}`
- `GET /api/v1/campaigns/{id}`
- `PUT /api/v1/campaigns/{id}/status`

#### 8.2.2 规则与测试接口

- `POST /api/v1/campaigns/{id}/trigger-rules`
- `POST /api/v1/campaigns/{id}/actions`
- `POST /api/v1/campaigns/{id}/test-trigger`

#### 8.2.3 执行查询接口

- `GET /api/v1/campaign-executions/{id}`
- `GET /api/v1/campaign-executions`
- `POST /api/v1/campaign-executions/{id}/retry`

### 8.3 动作执行接口

#### `growth-message`

- `POST /api/v1/message/send`

#### `growth-coupon`

- `POST /api/v1/coupon/issue`

#### `growth-incentive`

- `POST /api/v1/incentive/grant`

#### `growth-frequency`

- `POST /api/v1/frequency/check-and-acquire`

统一请求头或请求体应包含：

- `traceId`
- `campaignId`
- `executionId`
- `userId`
- `idempotentKey`

---

## 九、核心时序设计

### 9.1 实时事件触发营销

```text
1. 业务系统发送事件到 growth-profile
2. growth-profile 根据 EventDefinition 校验并标准化
3. growth-profile 写入 gp_profile_behavior_event
4. growth-profile 发布 profile-behavior-event-normalized
5. growth-campaign 订阅该 topic
6. growth-campaign 按 eventName 找到启用中的 trigger rule
7. growth-campaign 调 growth-frequency 做频控
8. growth-campaign 调 growth-profile /segments/match 判断 user 是否命中 segment
9. 命中则创建 gp_campaign_execution
10. growth-campaign 发送 campaign-action-execute
11. message/coupon/incentive 执行动作
12. 动作执行器回传 campaign-action-result
13. growth-campaign 更新 execution 与 action_execution 状态
```

### 9.2 定时分群营销

```text
1. 运营配置 schedule + segmentId + actions
2. 定时任务到点触发 growth-campaign
3. growth-campaign 获取 segment 用户列表或快照
4. 分批创建 gp_campaign_execution
5. 下发 campaign-action-execute
6. 下游执行并回传结果
```

### 9.3 分群变更驱动营销（后续增强）

```text
1. 标签值变化 / 行为事件变化
2. growth-profile 增量更新 gp_profile_segment_membership
3. growth-profile 发布 profile-segment-membership-changed
4. growth-campaign 按 segment enter/leave 规则触发活动
```

---

## 十、`growth-profile` 侧落地建议

### 10.1 P0 必做

#### 10.1.1 标准化行为事件广播

在 `BehaviorEventService` 与动态 MQ 消费链路写入事件后，统一走 outbox 发布：

- 目标 topic：`profile-behavior-event-normalized`
- 发布内容：标准化事件 DTO
- 发布时机：事件持久化成功后

#### 10.1.2 分群命中接口

为营销引擎补齐：

- `segments/match`
- `segments/batch-match`
- `users/{userId}/segments`

首版可直接复用现有 `SegmentSqlBuilder` 做判断；如果性能不足，再引入 `gp_profile_segment_membership`。

#### 10.1.3 分群计算版本化

在分群计算结果中引入 `version`，方便：

- 返回命中结果版本
- 做成员变更 diff
- 诊断“事件触发时命中的是哪一版人群”

### 10.2 P1 建议

#### 10.2.1 成员表物化

将热点分群的计算结果物化为 `gp_profile_segment_membership`，用于高频触发活动。

#### 10.2.2 分群成员变更事件

当成员表发生 enter/leave 变化时，广播 `profile-segment-membership-changed`。

#### 10.2.3 事件广播可靠性增强

引入 outbox 定时补偿任务，保证消息最终可达。

---

## 十一、`growth-campaign` MVP 范围建议

首期只做最小闭环：

1. 支持 `eventName + segmentId + actions`
2. 支持单事件触发
3. 支持动作串行执行
4. 支持消息、发券、发激励三类动作
5. 支持频控与幂等
6. 支持执行记录与失败重试

首期不做：

- 多分支工作流
- 等待节点
- 多阶段 Journey
- A/B 实验
- 可视化编排画布

---

## 十二、风险与权衡

### 12.1 实时命中性能

如果所有活动都在触发时直接查 ClickHouse 动态 SQL，活动量上来后会成为瓶颈。

建议：

- 初期先用 SQL 方案验证闭环
- 高频场景再对热点分群做 `segment_membership` 物化

### 12.2 数据一致性

“事件已落库，但营销未触发”是关键风险。

建议：

- 使用 outbox 表发布标准化事件
- 对 `campaign-action-execute` 与 `campaign-action-result` 做重试和幂等

### 12.3 边界失控

如果 `growth-campaign` 再次维护事件定义或分群规则，会出现两套真相源。

必须坚持：

- 事件只认 `growth-profile`
- 分群只认 `growth-profile`
- 活动只认 `growth-campaign`

---

## 十三、实施顺序

### 阶段一：打通最小闭环

1. `growth-profile` 发布 `profile-behavior-event-normalized`
2. `growth-profile` 提供 `segments/match`
3. `growth-campaign` 建表并实现事件触发活动
4. `growth-campaign` 接入 message/coupon/incentive/frequency

阶段一详细设计见：

- `docs/profile-campaign-phase1-design-v1.md`

### 阶段二：增强稳定性

1. `growth-profile` 引入 outbox 补偿
2. `growth-campaign` 引入执行重试和死信处理
3. 完善监控、告警与审计日志

### 阶段三：增强实时性

1. `growth-profile` 物化热点分群成员
2. 发布 `profile-segment-membership-changed`
3. `growth-campaign` 支持“进群即触发 / 出群即触发”

---

## 十四、最终建议

推荐采用以下落地路线：

1. 营销引擎主模块放在 `growth-campaign`
2. 事件定义与人群分层继续放在 `growth-profile`
3. `growth-profile` 补“标准化事件广播 + 分群命中能力”
4. `growth-campaign` 只做触发、编排、执行、审计

这样可以保持：

- 领域边界清晰
- 模块职责稳定
- 迭代路径可控
- 对现有 `growth-profile` 资产复用最大化
