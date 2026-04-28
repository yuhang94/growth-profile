# 用户画像支撑营销引擎阶段一详细设计 v1

## 一、范围

阶段一目标是打通最小闭环：

1. 业务事件进入 `growth-profile`
2. `growth-profile` 完成校验、标准化、落库
3. `growth-profile` 广播标准化行为事件
4. `growth-campaign` 订阅事件并匹配触发规则
5. `growth-campaign` 调 `growth-profile` 判断用户是否命中指定分群
6. 命中后执行动作
7. 记录执行结果

本阶段不做：

- 多阶段 Journey
- 分群成员实时变更触发
- 可视化编排
- 复杂条件工作流
- 动作异步重试中心
- outbox 补偿任务

---

## 二、阶段一边界

### 2.1 `growth-profile` 交付范围

- 新增标准化行为事件广播能力
- 新增单用户分群命中接口
- 新增批量分群命中接口
- 为广播与命中接口定义稳定 DTO

### 2.2 `growth-campaign` 交付范围

- 活动主表、触发规则表、动作定义表、执行记录表
- 消费 `profile-behavior-event-normalized`
- 根据 `eventName` 匹配启用中的触发规则
- 调用 `growth-profile` 的 `segments/match`
- 调用 `growth-frequency` 做频控
- 下发动作到 message/coupon/incentive
- 记录执行结果

### 2.3 下游模块交付范围

#### `growth-frequency`

- 提供同步频控占位接口

#### `growth-message`

- 提供同步消息发送接口

#### `growth-coupon`

- 提供同步发券接口

#### `growth-incentive`

- 提供同步发激励接口

---

## 三、端到端时序

```text
1. order-service 发送 order_paid 到 growth-profile
2. growth-profile 查询 EventDefinition
3. growth-profile 校验 eventName / properties / sourceType
4. growth-profile 写 gp_profile_behavior_event
5. growth-profile 发布 profile-behavior-event-normalized
6. growth-campaign 订阅消息
7. growth-campaign 根据 eventName 找到在线 trigger_rule
8. growth-campaign 检查 campaign 时间窗与状态
9. growth-campaign 调 growth-frequency/check-and-acquire
10. growth-campaign 调 growth-profile/segments/match
11. 若 matched=true，则创建 gp_campaign_execution
12. growth-campaign 逐个执行 campaign action
13. 调 growth-message / growth-coupon / growth-incentive
14. 记录 gp_campaign_action_execution
15. 汇总更新 gp_campaign_execution
```

---

## 四、`growth-profile` 详细设计

### 4.1 标准化行为事件广播

#### 4.1.1 Topic

- Topic 名称：`profile-behavior-event-normalized`
- Producer：`growth-profile`
- Consumer：`growth-campaign`

#### 4.1.2 触发时机

以下两条链路都要广播：

1. HTTP 上报事件成功写入 `gp_profile_behavior_event` 后
2. 外部 MQ 消息经动态消费者解析成功并写入 `gp_profile_behavior_event` 后

#### 4.1.3 消息体 DTO

```java
public class NormalizedBehaviorEvent {
    String traceId;
    String eventId;
    String tenantId;
    String userId;
    String eventName;
    String eventType;
    LocalDateTime occurredAt;
    String sourceType;
    String sourceName;
    Map<String, String> properties;
}
```

字段说明：

| 字段 | 说明 |
|------|------|
| `traceId` | 链路追踪 ID；无则由 `growth-profile` 生成 |
| `eventId` | 行为事件唯一 ID，复用事件表主业务 ID |
| `tenantId` | 先固定为 `default` |
| `userId` | 用户 ID |
| `eventName` | 事件名称 |
| `eventType` | 事件大类 |
| `occurredAt` | 事件发生时间，使用 `eventTime` |
| `sourceType` | `SDK` / `MQ` |
| `sourceName` | 来源系统名，阶段一允许为空 |
| `properties` | 标准化属性 |

#### 4.1.4 发布组件建议

新增组件：

- `BehaviorEventPublisher`
- `NormalizedBehaviorEventConverter`

建议调用点：

- `BehaviorEventService.report()`
- `BehaviorEventService.batchReport()`
- `DynamicMqConsumerManager.register()` 内消息消费成功分支

阶段一可以直接在成功写库后同步发送 MQ。

注意：

- 阶段一接受“落库成功但广播失败”的已知风险
- 阶段二再引入 `gp_profile_behavior_event_outbox`

### 4.2 分群命中接口

#### 4.2.1 单用户命中接口

接口：

`POST /api/v1/profile/segments/match`

请求 DTO：

```java
public class SegmentMatchRequest {
    Long segmentId;
    String userId;
    LocalDateTime contextTime;
}
```

响应 DTO：

```java
public class SegmentMatchResult {
    Long segmentId;
    String userId;
    Boolean matched;
    Long version;
    String reason;
}
```

阶段一实现策略：

1. 根据 `segmentId` 查询 `gp_profile_segment`
2. 读取 `conditionJson`
3. 在原有 `SegmentSqlBuilder` 基础上增加“限定 userId”的判断 SQL
4. 返回 `matched=true/false`

阶段一 `version` 先固定返回 `0`

#### 4.2.2 批量命中接口

接口：

`POST /api/v1/profile/segments/batch-match`

请求 DTO：

```java
public class SegmentBatchMatchRequest {
    Long segmentId;
    List<String> userIds;
}
```

响应 DTO：

```java
public class SegmentBatchMatchResult {
    Long segmentId;
    List<SegmentMatchResult> results;
}
```

阶段一实现策略：

- 内部循环调用单用户判定逻辑即可
- 不在阶段一做复杂批量 SQL 优化

### 4.3 代码结构建议

#### `growth-profile-api`

新增包：

- `io.growth.platform.profile.api.dto`
  - `NormalizedBehaviorEvent`
  - `SegmentMatchRequest`
  - `SegmentMatchResult`
  - `SegmentBatchMatchRequest`
  - `SegmentBatchMatchResult`

#### `growth-profile-service`

新增或调整组件：

- `controller/SegmentController`
  - 新增 `/match`
  - 新增 `/batch-match`
- `service/SegmentService`
  - 新增 `match()`
  - 新增 `batchMatch()`
- `infrastructure/clickhouse/SegmentSqlBuilder`
  - 新增按 `userId` 命中判断的 SQL 构造方法
- `infrastructure/mq/BehaviorEventPublisher`
  - 广播标准化事件
- `converter/NormalizedBehaviorEventConverter`
  - 行为事件领域对象转 MQ DTO

### 4.4 接口示例

#### 命中请求

```json
{
  "segmentId": 1001,
  "userId": "u10001",
  "contextTime": "2026-03-23T12:00:00"
}
```

#### 命中响应

```json
{
  "segmentId": 1001,
  "userId": "u10001",
  "matched": true,
  "version": 0,
  "reason": "matched_by_realtime_query"
}
```

---

## 五、`growth-campaign` 详细设计

### 5.1 阶段一支持的活动模型

一个活动只支持：

- 一个触发事件 `eventName`
- 一个目标分群 `segmentId`
- 多个串行动作 `actions`

不支持：

- 多触发器
- 分支判断
- 延迟等待
- 互斥实验

#### 配置示例

```json
{
  "campaignName": "支付成功发券",
  "triggerType": "EVENT",
  "triggerRule": {
    "eventName": "order_paid",
    "segmentId": 1001,
    "dedupWindowSec": 86400
  },
  "actions": [
    {
      "stepNo": 1,
      "actionType": "ISSUE_COUPON",
      "actionProvider": "growth-coupon",
      "actionConfig": {
        "couponTemplateId": 20001,
        "expireDays": 7
      }
    }
  ]
}
```

### 5.2 表结构草案

#### 5.2.1 `gp_campaign_definition`

```sql
CREATE TABLE gp_campaign_definition (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  campaign_code VARCHAR(64) NOT NULL,
  campaign_name VARCHAR(128) NOT NULL,
  description VARCHAR(512) NULL,
  trigger_type VARCHAR(32) NOT NULL,
  status TINYINT NOT NULL,
  priority INT NOT NULL DEFAULT 0,
  start_time DATETIME NULL,
  end_time DATETIME NULL,
  created_by VARCHAR(64) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_campaign_code (campaign_code)
);
```

#### 5.2.2 `gp_campaign_trigger_rule`

```sql
CREATE TABLE gp_campaign_trigger_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  campaign_id BIGINT NOT NULL,
  event_name VARCHAR(128) NOT NULL,
  segment_id BIGINT NOT NULL,
  dedup_window_sec INT NOT NULL DEFAULT 0,
  trigger_condition_json TEXT NULL,
  status TINYINT NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_event_name_status (event_name, status),
  KEY idx_campaign_id (campaign_id)
);
```

#### 5.2.3 `gp_campaign_action`

```sql
CREATE TABLE gp_campaign_action (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  campaign_id BIGINT NOT NULL,
  step_no INT NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  action_provider VARCHAR(64) NOT NULL,
  action_config_json TEXT NOT NULL,
  status TINYINT NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_campaign_id_step_no (campaign_id, step_no)
);
```

#### 5.2.4 `gp_campaign_execution`

```sql
CREATE TABLE gp_campaign_execution (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  campaign_id BIGINT NOT NULL,
  trigger_rule_id BIGINT NOT NULL,
  trigger_event_id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  segment_id BIGINT NOT NULL,
  execution_status VARCHAR(32) NOT NULL,
  trace_id VARCHAR(64) NOT NULL,
  trigger_time DATETIME NOT NULL,
  finished_time DATETIME NULL,
  fail_reason VARCHAR(1024) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_campaign_user_time (campaign_id, user_id, trigger_time),
  KEY idx_trigger_event_id (trigger_event_id)
);
```

#### 5.2.5 `gp_campaign_action_execution`

```sql
CREATE TABLE gp_campaign_action_execution (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  execution_id BIGINT NOT NULL,
  campaign_action_id BIGINT NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  provider_request_id VARCHAR(64) NULL,
  action_status VARCHAR(32) NOT NULL,
  request_payload TEXT NULL,
  response_payload TEXT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  executed_time DATETIME NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_execution_id (execution_id)
);
```

### 5.3 状态机

#### 活动状态

| 状态 | 说明 |
|------|------|
| `0-DRAFT` | 草稿 |
| `1-ONLINE` | 上线 |
| `2-OFFLINE` | 下线 |

#### 执行状态

| 状态 | 说明 |
|------|------|
| `INIT` | 已创建 |
| `RUNNING` | 动作执行中 |
| `SUCCESS` | 全部成功 |
| `PARTIAL_FAIL` | 部分动作失败 |
| `FAIL` | 全部失败 |

#### 动作状态

| 状态 | 说明 |
|------|------|
| `INIT` | 待执行 |
| `SUCCESS` | 成功 |
| `FAIL` | 失败 |

### 5.4 事件消费逻辑

消费者：

- `NormalizedBehaviorEventConsumer`

处理步骤：

1. 反序列化 `NormalizedBehaviorEvent`
2. 按 `eventName` 查询在线 `gp_campaign_trigger_rule`
3. 过滤已下线活动、无效时间窗活动
4. 对每条规则生成幂等键
5. 先调用频控接口
6. 再调用 `growth-profile` 命中判断接口
7. 命中则创建 `gp_campaign_execution`
8. 顺序执行活动动作
9. 更新执行结果

### 5.5 幂等策略

阶段一不单独建幂等表，先用 `gp_campaign_execution` 做弱幂等检查：

- 条件：`campaign_id + trigger_event_id + user_id`
- 若已存在成功或运行中记录，则跳过

说明：

- 该方案足够支持阶段一闭环
- 阶段二再独立 `gp_campaign_idempotent_record`

### 5.6 频控接口契约

接口：

`POST /api/v1/frequency/check-and-acquire`

请求：

```json
{
  "bizType": "CAMPAIGN",
  "bizKey": "campaign:1001:user:u10001",
  "ruleKey": "campaign:1001",
  "windowSeconds": 86400,
  "maxAcquireCount": 1
}
```

响应：

```json
{
  "allowed": true,
  "currentCount": 1
}
```

### 5.7 动作执行契约

#### 发消息

`POST /api/v1/message/send`

```json
{
  "traceId": "trace001",
  "campaignId": 1001,
  "executionId": 9001,
  "userId": "u10001",
  "idempotentKey": "1001_u10001_evt001_SEND_MESSAGE",
  "templateCode": "order_paid_notice",
  "templateParams": {
    "amount": "188.00"
  }
}
```

#### 发券

`POST /api/v1/coupon/issue`

```json
{
  "traceId": "trace001",
  "campaignId": 1001,
  "executionId": 9001,
  "userId": "u10001",
  "idempotentKey": "1001_u10001_evt001_ISSUE_COUPON",
  "couponTemplateId": 20001,
  "expireDays": 7
}
```

#### 发激励

`POST /api/v1/incentive/grant`

```json
{
  "traceId": "trace001",
  "campaignId": 1001,
  "executionId": 9001,
  "userId": "u10001",
  "idempotentKey": "1001_u10001_evt001_GRANT_INCENTIVE",
  "incentiveType": "POINT",
  "amount": 100
}
```

---

## 六、接口与 DTO 草案

### 6.1 `growth-profile` 新增 API

#### `POST /api/v1/profile/segments/match`

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "segmentId": 1001,
    "userId": "u10001",
    "matched": true,
    "version": 0,
    "reason": "matched_by_realtime_query"
  }
}
```

#### `POST /api/v1/profile/segments/batch-match`

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "segmentId": 1001,
    "results": [
      {
        "segmentId": 1001,
        "userId": "u10001",
        "matched": true,
        "version": 0,
        "reason": "matched_by_realtime_query"
      },
      {
        "segmentId": 1001,
        "userId": "u10002",
        "matched": false,
        "version": 0,
        "reason": "not_matched_by_realtime_query"
      }
    ]
  }
}
```

### 6.2 `growth-campaign` 新增 API

#### 创建活动

`POST /api/v1/campaigns`

```json
{
  "campaignCode": "pay_success_coupon_001",
  "campaignName": "支付成功发券",
  "description": "支付成功后给近30天下单用户发券",
  "triggerType": "EVENT",
  "startTime": "2026-03-24T00:00:00",
  "endTime": "2026-04-24T00:00:00",
  "triggerRule": {
    "eventName": "order_paid",
    "segmentId": 1001,
    "dedupWindowSec": 86400
  },
  "actions": [
    {
      "stepNo": 1,
      "actionType": "ISSUE_COUPON",
      "actionProvider": "growth-coupon",
      "actionConfig": {
        "couponTemplateId": 20001,
        "expireDays": 7
      }
    }
  ]
}
```

#### 活动测试触发

`POST /api/v1/campaigns/{id}/test-trigger`

```json
{
  "userId": "u10001",
  "eventName": "order_paid",
  "eventId": "evt_test_001",
  "properties": {
    "amount": "188.00"
  }
}
```

---

## 七、失败处理策略

### 7.1 阶段一接受的限制

- 标准化事件 MQ 发送失败时，仅记录日志，不做补偿
- 动作执行失败时，仅记录状态，不做自动重试
- 下游接口失败时，执行状态更新为 `FAIL` 或 `PARTIAL_FAIL`

### 7.2 必须保证的底线

- 营销动作执行失败不能影响 `growth-profile` 事件落库
- 任一动作失败后不能重复执行已成功动作
- 同一事件对同一活动尽量避免重复触发

---

## 八、监控与日志

阶段一至少补这几类日志与指标：

### 8.1 `growth-profile`

- 行为事件广播成功次数
- 行为事件广播失败次数
- 分群命中接口耗时
- 分群命中接口失败次数

### 8.2 `growth-campaign`

- 触发消息消费量
- 活动命中次数
- 频控拦截次数
- 分群不命中次数
- 动作执行成功次数
- 动作执行失败次数

### 8.3 关键日志字段

统一打印：

- `traceId`
- `campaignId`
- `triggerRuleId`
- `executionId`
- `eventId`
- `userId`

---

## 九、实施任务拆解

### 9.1 `growth-profile`

1. 新增标准化行为事件 DTO
2. 新增行为事件发布器
3. 在 HTTP / MQ 事件写库成功后接入发布器
4. 新增 `SegmentMatchRequest` 等 DTO
5. 扩展 `SegmentController` 与 `SegmentService`
6. 扩展 `SegmentSqlBuilder` 支持单用户命中判断

### 9.2 `growth-campaign`

1. 新建 campaign 相关数据表
2. 新增 campaign domain / repository / service / controller
3. 新增标准化事件消费者
4. 新增频控 client
5. 新增 profile 分群命中 client
6. 新增消息/发券/激励 client
7. 新增 execution 与 action_execution 记录逻辑

### 9.3 联调顺序

1. 先联调 `growth-profile -> normalized topic`
2. 再联调 `growth-campaign -> profile segments/match`
3. 再联调 `growth-campaign -> frequency`
4. 最后联调 `growth-campaign -> message/coupon/incentive`

---

## 十、验收标准

阶段一完成后，应满足以下验收项：

1. 新建一个 `eventName=order_paid`、`segmentId=1001` 的活动
2. 向 `growth-profile` 上报一个 `order_paid` 事件
3. `growth-profile` 成功写入 `gp_profile_behavior_event`
4. `growth-profile` 成功广播 `profile-behavior-event-normalized`
5. `growth-campaign` 成功消费该事件
6. `growth-campaign` 成功调用 `segments/match`
7. 若命中，则成功创建 `gp_campaign_execution`
8. 至少一个动作执行成功，并记录 `gp_campaign_action_execution`
9. 对同一事件重复投递时，不应重复触发同一活动

---

## 十一、阶段一后续衔接

阶段一结束后，优先进入阶段二的事项：

1. `growth-profile` 增加 outbox 表与补偿任务
2. `growth-campaign` 增加独立幂等表
3. `growth-campaign` 增加动作失败自动重试
4. `growth-profile` 增加分群版本化与成员物化

这样可以从“可用”过渡到“稳定可运营”。
