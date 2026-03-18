# 用户画像-事件模块 需求迭代v1

## 一、市面主流系统事件模块调研

### 1.1 事件 Schema 定义与管理

| 系统 | 方式 | 特点 | 参考资料 |
|------|------|------|---------|
| **Segment Protocols** | Tracking Plan + JSON Schema | 定义事件结构、属性类型、必填项；支持 schema 推断自动生成；violation 追踪 | [Tracking Plan 文档](https://segment.com/docs/protocols/tracking-plan/create/) / [Protocols 概述](https://segment.com/docs/protocols/) |
| **mParticle Data Master** | Data Plan | 编码化的数据期望集合；支持 API 管理，可纳入 SDLC 审批流程 | [Data Master](https://www.mparticle.com/platform/detail/data-master/) / [Data Governance](https://www.mparticle.com/platform/data-governance/) |
| **神策** | 元数据管理 > 事件表 | 事件 + 用户模型；支持自定义事件和公共属性；全端 SDK 数据采集 | [事件公共属性](https://docs.sensorsdata.com/sa/docs/events_common_property/v0300) / [埋点方案设计](https://manual.sensorsdata.cn/sa/docs/schema-design) |
| **RudderStack** | Data Governance API | 事件元数据 + schema + 版本管理；在采集入口做校验 | [Data Governance API](https://www.rudderstack.com/blog/rudderstacks-data-governance-api/) / [架构文档](https://www.rudderstack.com/docs/resources/rudderstack-architecture/) |
| **Tracardi** | JSON Schema + Dotted Notation | 验证器使用 JSON Schema 定义事件/Profile 模型；支持事件 reshape | [Event Tracking](https://docs.tracardi.com/events/event_tracking/) / [Event Validation](https://docs.tracardi.com/events/event_validation/) |

**关键结论**：业界统一使用 **JSON Schema** 作为事件属性定义和校验的标准方式。

### 1.2 外部事件接入方式

| 接入方式 | 代表系统 | 说明 | 参考资料 |
|----------|---------|------|---------|
| **HTTP Tracking API** | Segment、mParticle、RudderStack | 客户端/服务端 SDK 通过 HTTP 上报事件；Segment 限制单事件 32KB、批量 500KB | [Segment HTTP API](https://segment.com/docs/connections/sources/catalog/libraries/server/http-api/) / [Track Spec](https://segment.com/docs/connections/spec/track/) |
| **MQ 消费外部消息** | RudderStack(Kafka)、Segment(Kafka Connect) | 平台主动订阅外部 Kafka/MQ topic，消费并解析业务消息 | [Confluent Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html) |
| **CDC / Webhook** | 通用模式 | 监听数据库变更或接收 Webhook 回调 | [Event-Driven Architecture Patterns](https://solace.com/event-driven-architecture-patterns/) |

### 1.3 消息模板与字段映射

| 系统 | 映射方式 | 说明 | 参考资料 |
|------|---------|------|---------|
| **RudderStack** | JSON Template Engine / JavaScript/Python 转换 | 自定义 DSL 简化 JSON 格式转换；支持 JSONPath 语法（`$.a`）；预置模板库 | [Transformations 模板](https://www.rudderstack.com/docs/transformations/templates/) / [JSON Template Engine](https://www.npmjs.com/package/@rudderstack/json-template-engine) |
| **mParticle** | Field Transformations API | JSON 格式的字段映射数组；指定外部字段 → 内部字段的对应关系 | [Field Transformations API](https://docs.mparticle.com/developers/apis/platform/field-transformations/) |
| **AWS Step Functions** | JSONPath / JSONata | 5 个 JSONPath 字段(InputPath, Parameters 等)或 JSONata 表达式 | [AWS Lambda Kafka Avro/Protobuf](https://aws.amazon.com/blogs/compute/introducing-aws-lambda-native-support-for-avro-and-protobuf-formatted-apache-kafka-events/) |
| **Tracardi** | Event Mapping + Reshaping | 事件属性 → event traits（可索引/可搜索）的映射；支持 schema reshape | [Tracardi 文档](https://docs.tracardi.com/) |

**关键结论**：
- **JSONPath** 是最通用的字段提取方式，Java 生态有成熟库（Jayway JsonPath）
- 对于简单场景，JSONPath 提取即可；复杂场景可扩展为 Groovy 脚本
- 主流系统均支持**多种解析策略**共存，按字段粒度选择

### 1.4 Schema 治理

| 维度 | 业界做法 | 参考资料 |
|------|---------|---------|
| **校验模式** | Strict（拒绝不符合 schema 的事件） vs Permissive（接收但记录 violation） | [Segment Schema 配置](https://segment.com/docs/protocols/enforce/schema-configuration/) |
| **版本管理** | Confluent Schema Registry 的 BACKWARD/FORWARD/FULL 兼容性检查 | [Schema Evolution](https://docs.confluent.io/platform/current/schema-registry/fundamentals/schema-evolution.html) |
| **质量监控** | 违规事件 → Dead Letter Queue（DLQ）；violation 仪表盘；实时告警 | [数据质量实时校验](https://www.confluent.io/blog/making-data-quality-scalable-with-real-time-streaming-architectures/) |

### 1.5 事件处理流水线（典型架构）

```
事件输入（HTTP / MQ）
  ↓
Gateway（接收 & ACK）
  ↓
Schema 校验（对照事件定义校验属性）
  ↓
字段映射 & 转换（JSONPath 提取 / 模板转换）
  ↓
标准化事件（统一格式写入存储）
  ↓
下游分发（ClickHouse 存储 / MQ 广播 / 触发标签计算）
```

---

### 1.6 事件模块各功能重要程度

| 优先级 | 功能 | 说明 | 本期是否实现 |
|--------|------|------|-------------|
| **P0 必做** | 事件属性校验 | HTTP 上报时根据 PropertyDefinition 校验必填项和类型，防止脏数据入库。所有主流系统均具备 | 是 |
| **P0 必做** | 外部 MQ 消息接入 | 平台订阅外部业务系统 MQ topic，消费并解析消息为标准事件。扩大事件数据来源是核心需求 | 是 |
| **P0 必做** | 字段映射 & 解析引擎 | JSONPath / Groovy / 本地函数等多策略字段提取，将外部消息转为标准事件。MQ 接入的前置依赖 | 是 |
| **P0 必做** | 动态消费者管理 | 事件定义创建/启停时动态注册/注销 MQ 消费者。MQ 接入的运行时基础 | 是 |
| **P1 重要** | 消息模板在线测试 | 提供 API：输入样例消息 + fieldMappings → 返回解析结果，方便配置调试 | 是 |
| **P1 重要** | MQ 消费者状态查询 | 查看各 MQ 事件的消费者运行状态，便于运维排障 | 是 |
| **P2 增强** | Dead Letter Queue | 解析/校验失败的消息投递 DLQ，支持人工排查和重试 | 否（后续迭代） |
| **P2 增强** | Schema 版本管理 | PropertyDefinition 变更历史，支持回溯和兼容性检查 | 否（后续迭代） |
| **P2 增强** | Webhook 接入 | 新增 sourceType=WEBHOOK，平台生成回调 URL 供外部推送 | 否（后续迭代） |
| **P3 锦上添花** | 事件质量看板 | 上报量、校验通过率、失败原因分布的可视化统计 | 否（后续迭代） |

---

## 二、当前系统现状

### 2.1 已有能力

| 模块 | 现状 |
|------|------|
| 事件定义管理 | CRUD、7 种 EventType、PropertyDefinition（name/type/displayName/required）、启停 |
| HTTP 事件上报 | 单条/批量上报接口，校验事件定义存在且启用 |
| MQ 事件上报 | `profile-behavior-event` topic → `BehaviorEventMQConsumer` → ClickHouse |
| 事件存储 | ClickHouse `gp_profile_behavior_event` 表，按月分区 |
| 事件查询 | 按 userId/eventName/时间范围分页查询 |

### 2.2 缺失能力

| # | 缺失项 | 说明 |
|---|--------|------|
| 1 | **HTTP 上报时的属性校验** | 当前只校验事件定义是否存在/启用，不校验上报属性是否符合 PropertyDefinition |
| 2 | **外部 MQ 消息接入** | 当前只消费固定格式的 `BehaviorEventMQMessage`，不支持订阅外部业务系统的任意 topic 并解析其自定义消息格式 |
| 3 | **消息模板 & 字段映射** | 无法将外部消息的字段映射为标准事件属性 |
| 4 | **事件接入方式元数据** | EventDefinition 缺少接入方式（SDK/MQ）、MQ topic/tag、消息模板等元数据 |
| 5 | **动态 MQ 消费者管理** | 无法根据事件定义动态创建/销毁 RocketMQ 消费者 |

---

## 三、需求定义

### 3.1 丰富事件接入方式

#### 方式一：外部用户调用平台 HTTP API（SDK 上报）

- **事件定义**：定义事件类型、属性（名称、类型、是否必填）、接入方式=SDK
- **事件消费**：接收 HTTP 请求 → **根据 PropertyDefinition 校验上报数据** → 存储

#### 方式二：平台消费外部用户 MQ 消息

- **事件定义**：定义事件类型、属性、接入方式=MQ；指定 topic、tag（可选）；定义消息模板和字段解析规则
- **事件消费**：平台动态订阅指定 topic/tag → 接收外部消息 → **根据模板和解析规则提取字段** → 转换为标准事件 → 校验 → 存储

---

## 四、技术方案设计

### 4.1 事件定义模型扩展

在现有 `BehaviorEventDefinition` 基础上新增字段：

```java
public class BehaviorEventDefinition {
    // === 已有字段 ===
    Long id;
    String eventName;          // 唯一标识
    EventType eventType;       // PAGE_VIEW, CLICK, ORDER, LOGIN, SEARCH, SHARE, CUSTOM
    String displayName;
    String description;
    List<PropertyDefinition> properties;
    Integer status;            // 1-启用 0-禁用
    String createdBy;
    LocalDateTime createdTime;
    LocalDateTime updatedTime;

    // === 新增字段 ===
    SourceType sourceType;     // SDK / MQ — 事件接入方式
    MqSourceConfig mqSourceConfig;  // 仅 sourceType=MQ 时有值
}
```

#### 4.1.1 SourceType 枚举

```java
public enum SourceType {
    SDK,   // 外部用户通过 HTTP API 上报
    MQ     // 平台消费外部 MQ 消息
}
```

#### 4.1.2 MqSourceConfig（MQ 接入配置）

```java
public class MqSourceConfig {
    String topic;                          // MQ topic（必填）
    String tag;                            // MQ tag（可选，RocketMQ 支持 tag 过滤）
    String consumerGroup;                  // 消费者组名（必填）
    List<FieldMapping> fieldMappings;      // 字段映射规则
}
```

#### 4.1.3 FieldMapping（字段映射规则）

```java
public class FieldMapping {
    String targetField;         // 标准事件字段名，对应 BehaviorEvent 中的字段
                                // 保留字段：userId, eventTime
                                // 自定义属性字段写入 properties map
    ExtractStrategy strategy;   // 解析策略：JSON_PATH / GROOVY / LOCAL_FUNC
    String expression;          // 解析表达式（含义取决于 strategy）
                                //   JSON_PATH  → JSONPath 表达式，如 "$.data.user_id"
                                //   GROOVY     → Groovy 脚本，入参为 msg(Map)，返回提取值
                                //   LOCAL_FUNC → 本地函数名，如 "extractOrderId"
    String sourceType;          // 值类型：STRING, LONG, DOUBLE, BOOLEAN,
                                //         EPOCH_SECOND, EPOCH_MILLIS, DATETIME_STRING
    String defaultValue;        // 提取失败时的默认值（可选）
}
```

#### 4.1.4 ExtractStrategy 枚举（解析策略）

```java
public enum ExtractStrategy {
    JSON_PATH,    // JSONPath 表达式提取（默认，适合绝大多数场景）
    GROOVY,       // Groovy 脚本（复杂转换、条件判断、多字段拼接等）
    LOCAL_FUNC    // 本地预置函数（平台内置的常用提取逻辑，安全可控）
}
```

**三种策略对比**：

| 策略 | 表达式示例 | 适用场景 | 安全性 |
|------|-----------|---------|--------|
| **JSON_PATH**（默认） | `$.buyerInfo.uid` | 简单字段提取，90% 场景 | 高：只读取不执行 |
| **GROOVY** | `msg.amount / 100` | 值转换、条件判断、多字段拼接 | 中：需沙箱限制（见 4.4.3.2） |
| **LOCAL_FUNC** | `extractPhoneFromMask` | 平台预置的通用提取逻辑（脱敏手机号还原等） | 高：代码在平台内部 |

#### 4.1.5 时间字段类型与固定格式

时间字段的 `sourceType` 使用专用类型枚举，每种类型对应**固定的解析格式**，无需在 FieldMapping 中额外存储 `dateFormat`：

| sourceType | 含义 | 固定解析规则 | 示例值 |
|------------|------|-------------|--------|
| `EPOCH_SECOND` | Unix 秒级时间戳 | `Instant.ofEpochSecond(long)` | `1742198400` |
| `EPOCH_MILLIS` | Unix 毫秒级时间戳 | `Instant.ofEpochMilli(long)` | `1742198400000` |
| `DATETIME_STRING` | 字符串日期时间 | 按优先级依次尝试以下格式：<br>1. `yyyy-MM-dd'T'HH:mm:ss` (ISO-8601)<br>2. `yyyy-MM-dd HH:mm:ss`<br>3. `yyyy/MM/dd HH:mm:ss`<br>4. `yyyyMMddHHmmss` | `"2026-03-17 14:30:00"` |

**解析代码**：

```java
public class DateTimeParser {

    private static final DateTimeFormatter[] DATETIME_FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,                          // yyyy-MM-dd'T'HH:mm:ss
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
    };

    public static LocalDateTime parse(Object rawValue, String sourceType) {
        return switch (sourceType) {
            case "EPOCH_SECOND" -> LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(Long.parseLong(rawValue.toString())), ZoneId.systemDefault());
            case "EPOCH_MILLIS" -> LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(Long.parseLong(rawValue.toString())), ZoneId.systemDefault());
            case "DATETIME_STRING" -> parseWithFormatters(rawValue.toString());
            default -> throw new BizException("Unsupported datetime sourceType: " + sourceType);
        };
    }

    private static LocalDateTime parseWithFormatters(String text) {
        for (DateTimeFormatter fmt : DATETIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(text.trim(), fmt);
            } catch (DateTimeParseException ignored) {}
        }
        throw new BizException("Cannot parse datetime: " + text);
    }
}
```

#### 4.1.6 解析策略对比与选型依据

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|---------|
| **JSONPath** | Java 生态成熟（Jayway JsonPath）；语法简单直观；零学习成本 | 只能提取，不能做复杂转换 | 字段提取、简单映射（默认选择） |
| **Groovy 脚本** | 最灵活，支持任意计算逻辑；JVM 原生运行 | 需沙箱限制（白名单类/方法）；调试较复杂 | 值转换、条件判断、多字段拼接 |
| **本地函数** | 安全可控、性能最优；平台统一维护 | 扩展需发版；数量有限 | 通用提取逻辑（脱敏还原、ID 格式转换等） |

**依赖引入**（pom.xml）：
```xml
<!-- JSONPath -->
<dependency>
    <groupId>com.jayway.jsonpath</groupId>
    <artifactId>json-path</artifactId>
    <version>2.9.0</version>
</dependency>

<!-- Groovy（仅需 groovy-jsr223 即可，轻量） -->
<dependency>
    <groupId>org.apache.groovy</groupId>
    <artifactId>groovy-jsr223</artifactId>
    <version>4.0.24</version>
</dependency>
```

#### 4.1.7 配置示例

假设外部系统发送如下订单消息到 RocketMQ topic `order-paid`：

```json
{
  "orderId": "ORD_20260317_001",
  "buyerInfo": {
    "uid": "U123456",
    "name": "张三"
  },
  "payment": {
    "amountCent": 29900,
    "method": "alipay"
  },
  "paidAt": "2026-03-17 14:30:00"
}
```

对应的事件定义：

```json
{
  "eventName": "order_paid",
  "eventType": "ORDER",
  "displayName": "订单支付成功",
  "sourceType": "MQ",
  "properties": [
    { "propertyName": "orderId", "propertyType": "STRING", "displayName": "订单号", "required": true },
    { "propertyName": "amount", "propertyType": "DOUBLE", "displayName": "支付金额(元)", "required": true },
    { "propertyName": "payMethod", "propertyType": "STRING", "displayName": "支付方式", "required": false }
  ],
  "mqSourceConfig": {
    "topic": "order-paid",
    "tag": "",
    "consumerGroup": "profile-event-order-paid-cg",
    "fieldMappings": [
      {
        "targetField": "userId",
        "strategy": "JSON_PATH",
        "expression": "$.buyerInfo.uid",
        "sourceType": "STRING"
      },
      {
        "targetField": "eventTime",
        "strategy": "JSON_PATH",
        "expression": "$.paidAt",
        "sourceType": "DATETIME_STRING"
      },
      {
        "targetField": "orderId",
        "strategy": "JSON_PATH",
        "expression": "$.orderId",
        "sourceType": "STRING"
      },
      {
        "targetField": "amount",
        "strategy": "GROOVY",
        "expression": "msg.payment.amountCent / 100.0",
        "sourceType": "DOUBLE"
      },
      {
        "targetField": "payMethod",
        "strategy": "JSON_PATH",
        "expression": "$.payment.method",
        "sourceType": "STRING"
      }
    ]
  }
}
```

> **说明**：`amount` 字段使用 Groovy 策略，将外部消息的分单位（29900）转为元单位（299.00）。其他字段使用 JSONPath 直接提取。

### 4.2 数据库变更

#### 4.2.1 MySQL DDL

```sql
ALTER TABLE `gp_profile_event_definition`
  ADD COLUMN `source_type` VARCHAR(16) NOT NULL DEFAULT 'SDK' COMMENT '接入方式: SDK / MQ' AFTER `properties_json`,
  ADD COLUMN `mq_source_config_json` TEXT DEFAULT NULL COMMENT 'MQ接入配置JSON(仅source_type=MQ时有值)' AFTER `source_type`;
```

#### 4.2.2 修改清单

**必改项（持久层）：**
- `EventDefinitionDO.java` — 新增 `sourceType`、`mqSourceConfigJson` 字段
- `BehaviorEventDefinition.java` — 新增 `sourceType`(SourceType 枚举)、`mqSourceConfig`(MqSourceConfig 对象)
- `EventDefinitionConverter.java`（infrastructure 层）— 新增 sourceType ↔ String、mqSourceConfig ↔ JSON 的转换方法

**可选项：**
- `EventDefinitionDTO.java` — 新增字段暴露给 API 调用方
- `EventDefinitionCreateRequest.java` — 新增 sourceType、mqSourceConfig 入参
- `EventDefinitionUpdateRequest.java` — 新增 sourceType、mqSourceConfig 入参
- `EventDefinitionDTOConverter.java`（converter 层）— MapStruct 映射

### 4.3 HTTP 上报属性校验

#### 4.3.1 校验逻辑

在 `BehaviorEventService.report()` 中增加属性校验步骤：

```
1. 查询事件定义（已有）
2. 校验事件启用（已有）
3. 【新增】校验 sourceType == SDK（MQ 类型事件不允许通过 HTTP 上报）
4. 【新增】校验上报属性是否符合 PropertyDefinition：
   a. 必填属性（required=true）是否都有值
   b. 属性值类型是否匹配 propertyType
   c. 未定义的属性 → 忽略（Permissive 模式，记录日志）
5. 生成事件 ID、存储（已有）
```

#### 4.3.2 属性类型校验规则

| propertyType | 校验规则 |
|-------------|---------|
| STRING | 任意字符串 |
| LONG | `Long.parseLong()` 成功 |
| DOUBLE | `Double.parseDouble()` 成功 |
| BOOLEAN | `"true"` 或 `"false"`（忽略大小写） |
| DATETIME | 符合 ISO-8601 或 `yyyy-MM-dd HH:mm:ss` 格式 |

#### 4.3.3 新增领域服务

```java
// domain/service/EventPropertyValidator.java
public class EventPropertyValidator {

    /**
     * 校验上报属性是否符合事件定义
     * @throws BizException 必填属性缺失或类型不匹配时抛出
     */
    public void validate(Map<String, String> reportedProperties,
                         List<PropertyDefinition> definitions) {
        // 1. 检查必填属性
        // 2. 检查类型匹配
    }
}
```

### 4.4 外部 MQ 消息接入

#### 4.4.1 整体流程

```
事件定义创建（sourceType=MQ）
  ↓
动态注册 RocketMQ 消费者（订阅 topic + tag）
  ↓
接收外部消息（原始 JSON 字符串）
  ↓
根据 FieldMapping.strategy 选择提取器（JSONPath / Groovy / 本地函数）
  ↓
按 fieldMappings 逐字段提取 → 构建标准 BehaviorEvent
  ↓
时间字段根据 sourceType（EPOCH_SECOND / EPOCH_MILLIS / DATETIME_STRING）自动解析
  ↓
属性校验（根据 PropertyDefinition）
  ↓
存储到 ClickHouse
```

#### 4.4.2 动态消费者管理器

核心组件 `DynamicMqConsumerManager`，负责根据事件定义动态创建/销毁 RocketMQ 消费者：

```java
// infrastructure/mq/DynamicMqConsumerManager.java
@Component
public class DynamicMqConsumerManager {

    // 活跃消费者注册表：eventName → DefaultMQPushConsumer
    private final ConcurrentHashMap<String, DefaultMQPushConsumer> activeConsumers = new ConcurrentHashMap<>();

    /**
     * 注册消费者：创建 DefaultMQPushConsumer，订阅 topic + tag，设置消息监听器
     */
    public void register(String eventName, MqSourceConfig config) { ... }

    /**
     * 注销消费者：shutdown 并移除
     */
    public void unregister(String eventName) { ... }

    /**
     * 应用启动时：加载所有 sourceType=MQ 且 status=启用 的事件定义，批量注册
     */
    @PostConstruct
    public void initOnStartup() { ... }

    /**
     * 应用关闭时：shutdown 所有消费者
     */
    @PreDestroy
    public void shutdownAll() { ... }
}
```

**生命周期联动**：

| 事件定义操作 | 消费者动作 |
|-------------|-----------|
| 创建 MQ 类型事件（status=启用） | `register()` |
| 启用 MQ 类型事件 | `register()` |
| 禁用 MQ 类型事件 | `unregister()` |
| 删除 MQ 类型事件 | `unregister()` |
| 更新 MQ 配置（topic/tag 变更） | `unregister()` → `register()` |
| 应用启动 | 加载所有启用的 MQ 事件定义 → 批量 `register()` |
| 应用关闭 | 批量 `shutdown()` |

#### 4.4.3 消息解析引擎（多策略）

##### 4.4.3.1 策略接口与实现

```java
// infrastructure/mq/extract/FieldExtractor.java — 策略接口
public interface FieldExtractor {
    Object extract(String rawJson, String expression);
}

// infrastructure/mq/extract/JsonPathExtractor.java
public class JsonPathExtractor implements FieldExtractor {
    @Override
    public Object extract(String rawJson, String expression) {
        return JsonPath.parse(rawJson).read(expression);
    }
}

// infrastructure/mq/extract/GroovyExtractor.java
public class GroovyExtractor implements FieldExtractor {
    private final GroovyShell shell; // 沙箱配置见 4.4.3.2

    @Override
    public Object extract(String rawJson, String expression) {
        Map<String, Object> msg = JsonPath.parse(rawJson).json();
        Binding binding = new Binding();
        binding.setVariable("msg", msg);
        return shell.evaluate(expression);
    }
}

// infrastructure/mq/extract/LocalFuncExtractor.java
public class LocalFuncExtractor implements FieldExtractor {
    // 注册表：函数名 → Function<String, Object>
    private final Map<String, Function<String, Object>> registry = new HashMap<>();

    @PostConstruct
    public void init() {
        registry.put("extractPhoneFromMask", raw -> { /* 脱敏手机号还原 */ });
        // 按需扩展更多本地函数
    }

    @Override
    public Object extract(String rawJson, String expression) {
        Function<String, Object> func = registry.get(expression);
        if (func == null) throw new BizException("Unknown local function: " + expression);
        return func.apply(rawJson);
    }
}
```

##### 4.4.3.2 Groovy 沙箱安全

```java
// Groovy 沙箱配置：限制可调用的类和方法
CompilerConfiguration config = new CompilerConfiguration();
config.addCompilationCustomizers(
    new SandboxTransformer(),        // groovy-sandbox
    new ImportCustomizer()           // 禁止 import
        .addStarImports("java.lang")
);
// 超时限制：单次执行 ≤ 500ms
// 禁止：文件 IO、网络、反射、System.exit、Thread
```

##### 4.4.3.3 EventMessageParser（重构）

```java
// infrastructure/mq/EventMessageParser.java
@Component
public class EventMessageParser {

    private final Map<ExtractStrategy, FieldExtractor> extractors;

    public EventMessageParser(JsonPathExtractor jsonPath,
                              GroovyExtractor groovy,
                              LocalFuncExtractor localFunc) {
        this.extractors = Map.of(
            ExtractStrategy.JSON_PATH, jsonPath,
            ExtractStrategy.GROOVY, groovy,
            ExtractStrategy.LOCAL_FUNC, localFunc
        );
    }

    public BehaviorEvent parse(String rawJson, String eventName,
                               List<FieldMapping> fieldMappings) {
        String userId = null;
        LocalDateTime eventTime = null;
        Map<String, String> properties = new HashMap<>();

        for (FieldMapping mapping : fieldMappings) {
            // 1. 根据策略选择提取器
            FieldExtractor extractor = extractors.get(mapping.getStrategy());

            // 2. 提取值
            Object rawValue;
            try {
                rawValue = extractor.extract(rawJson, mapping.getExpression());
            } catch (Exception e) {
                rawValue = null;
            }

            // 3. 提取失败使用默认值
            if (rawValue == null && mapping.getDefaultValue() != null) {
                rawValue = mapping.getDefaultValue();
            }

            // 4. 分类写入
            switch (mapping.getTargetField()) {
                case "userId" -> userId = rawValue != null ? rawValue.toString() : null;
                case "eventTime" -> eventTime = DateTimeParser.parse(rawValue, mapping.getSourceType());
                default -> {
                    if (rawValue != null) {
                        properties.put(mapping.getTargetField(), rawValue.toString());
                    }
                }
            }
        }

        // 5. 校验必填保留字段
        if (userId == null) throw new BizException("userId mapping is missing or extracted null");
        if (eventTime == null) eventTime = LocalDateTime.now();

        return BehaviorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .eventName(eventName)
                .properties(properties)
                .eventTime(eventTime)
                .createdTime(LocalDateTime.now())
                .build();
    }
}
```

#### 4.4.4 动态消费者的消息监听器

```java
// DynamicMqConsumerManager 内部创建的 MessageListener
MessageListenerConcurrently listener = (msgs, context) -> {
    for (MessageExt msg : msgs) {
        try {
            String rawJson = new String(msg.getBody(), StandardCharsets.UTF_8);

            // 1. 解析消息
            BehaviorEvent event = eventMessageParser.parse(rawJson, eventName, config.getFieldMappings());

            // 2. 查询事件定义，补充 eventType
            BehaviorEventDefinition definition = eventDefinitionRepository.findByEventName(eventName)
                    .orElseThrow(() -> new BizException("Event definition not found: " + eventName));
            event.setEventType(definition.getEventType().name());

            // 3. 属性校验
            eventPropertyValidator.validate(event.getProperties(), definition.getProperties());

            // 4. 存储
            behaviorEventRepository.insert(event);
        } catch (Exception e) {
            log.error("Failed to process external MQ message for event [{}], msgId={}", eventName, msg.getMsgId(), e);
            // TODO: 后续可投递到 DLQ（Dead Letter Queue）
        }
    }
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
};
```

### 4.5 EventDefinitionService 改造

事件定义的创建/更新/状态变更需联动消费者管理：

```java
// service/EventDefinitionService.java

public EventDefinitionDTO create(EventDefinitionCreateRequest request) {
    // ... 已有校验逻辑 ...

    // 【新增】MQ 类型校验
    if (request.getSourceType() == SourceType.MQ) {
        validateMqSourceConfig(request.getMqSourceConfig());
    }

    // 保存到 DB
    eventDefinitionRepository.insert(definition);

    // 【新增】如果是 MQ 类型且启用，注册消费者
    if (definition.getSourceType() == SourceType.MQ && definition.isEnabled()) {
        dynamicMqConsumerManager.register(definition.getEventName(), definition.getMqSourceConfig());
    }

    return dto;
}

public void updateStatus(String eventName, Integer status) {
    // ... 已有逻辑 ...

    // 【新增】联动消费者
    if (definition.getSourceType() == SourceType.MQ) {
        if (status == 1) {
            dynamicMqConsumerManager.register(eventName, definition.getMqSourceConfig());
        } else {
            dynamicMqConsumerManager.unregister(eventName);
        }
    }
}
```

### 4.6 MQ 配置校验

创建 MQ 类型事件定义时需校验配置完整性：

```java
private void validateMqSourceConfig(MqSourceConfig config) {
    // 1. topic 不能为空
    // 2. consumerGroup 不能为空
    // 3. consumerGroup 不能与已有事件定义重复（避免消费者组冲突）
    // 4. fieldMappings 不能为空
    // 5. fieldMappings 中必须包含 targetField="userId" 的映射
    // 6. 校验 expression：
    //    - JSON_PATH → 合法的 JSONPath 表达式（以 $ 开头）
    //    - GROOVY    → 非空字符串（创建时可选做语法检查）
    //    - LOCAL_FUNC → 必须是已注册的本地函数名
    // 7. 时间字段的 sourceType 必须为 EPOCH_SECOND / EPOCH_MILLIS / DATETIME_STRING 之一
}
```

---

## 五、API 接口设计

### 5.1 创建事件定义（扩展）

**POST** `/api/v1/profile/event-definitions`

请求体（SDK 类型 — 与当前兼容）：
```json
{
  "eventName": "page_view",
  "eventType": "PAGE_VIEW",
  "displayName": "页面浏览",
  "sourceType": "SDK",
  "properties": [
    { "propertyName": "pageUrl", "propertyType": "STRING", "displayName": "页面URL", "required": true },
    { "propertyName": "duration", "propertyType": "LONG", "displayName": "停留时长(ms)", "required": false }
  ]
}
```

请求体（MQ 类型 — 新增）：
```json
{
  "eventName": "order_paid",
  "eventType": "ORDER",
  "displayName": "订单支付成功",
  "sourceType": "MQ",
  "properties": [
    { "propertyName": "orderId", "propertyType": "STRING", "displayName": "订单号", "required": true },
    { "propertyName": "amount", "propertyType": "DOUBLE", "displayName": "支付金额(元)", "required": true }
  ],
  "mqSourceConfig": {
    "topic": "order-paid",
    "tag": "",
    "consumerGroup": "profile-event-order-paid-cg",
    "fieldMappings": [
      { "targetField": "userId", "strategy": "JSON_PATH", "expression": "$.buyerInfo.uid", "sourceType": "STRING" },
      { "targetField": "eventTime", "strategy": "JSON_PATH", "expression": "$.paidAt", "sourceType": "DATETIME_STRING" },
      { "targetField": "orderId", "strategy": "JSON_PATH", "expression": "$.orderId", "sourceType": "STRING" },
      { "targetField": "amount", "strategy": "GROOVY", "expression": "msg.payment.amountCent / 100.0", "sourceType": "DOUBLE" }
    ]
  }
}
```

### 5.2 事件上报（SDK 方式，增强校验）

**POST** `/api/v1/profile/behavior-events`

请求体不变，增强服务端校验：
```json
{
  "userId": "U123456",
  "eventName": "page_view",
  "properties": {
    "pageUrl": "https://example.com/home",
    "duration": "3500"
  },
  "eventTime": "2026-03-17T14:30:00"
}
```

校验失败响应示例：
```json
{
  "code": "PARAM_ERROR",
  "message": "事件属性校验失败: 必填属性 [pageUrl] 缺失; 属性 [duration] 类型不匹配, 期望 LONG 实际值 'abc'"
}
```

### 5.3 消息模板在线测试（新增）

**POST** `/api/v1/profile/event-definitions/mq-mapping/test`

用于在创建 MQ 事件定义前，调试 fieldMappings 是否能正确解析样例消息。

请求体：
```json
{
  "sampleMessage": "{\"orderId\":\"ORD_001\",\"buyerInfo\":{\"uid\":\"U123\"},\"payment\":{\"amountCent\":29900},\"paidAt\":\"2026-03-17 14:30:00\"}",
  "fieldMappings": [
    { "targetField": "userId", "strategy": "JSON_PATH", "expression": "$.buyerInfo.uid", "sourceType": "STRING" },
    { "targetField": "eventTime", "strategy": "JSON_PATH", "expression": "$.paidAt", "sourceType": "DATETIME_STRING" },
    { "targetField": "amount", "strategy": "GROOVY", "expression": "msg.payment.amountCent / 100.0", "sourceType": "DOUBLE" }
  ]
}
```

响应：
```json
{
  "code": "0",
  "data": {
    "success": true,
    "extractedFields": {
      "userId": { "value": "U123", "sourceType": "STRING" },
      "eventTime": { "value": "2026-03-17T14:30:00", "sourceType": "DATETIME_STRING" },
      "amount": { "value": "299.0", "sourceType": "DOUBLE" }
    },
    "errors": []
  }
}
```

解析失败时：
```json
{
  "code": "0",
  "data": {
    "success": false,
    "extractedFields": {
      "userId": { "value": "U123", "sourceType": "STRING" }
    },
    "errors": [
      { "targetField": "eventTime", "error": "JSONPath $.paidAtX returned null" },
      { "targetField": "amount", "error": "Groovy execution failed: No such property: amountCent2" }
    ]
  }
}
```

### 5.4 MQ 消费者状态查询（新增）

**GET** `/api/v1/profile/event-definitions/mq-consumers/status`

响应：
```json
{
  "code": "0",
  "data": [
    {
      "eventName": "order_paid",
      "topic": "order-paid",
      "consumerGroup": "profile-event-order-paid-cg",
      "status": "RUNNING"
    }
  ]
}
```

---

## 六、新增文件清单

| 文件 | 层 | 说明 |
|------|---|------|
| `SourceType.java` | api/enums | 接入方式枚举（SDK / MQ） |
| `ExtractStrategy.java` | api/enums | 解析策略枚举（JSON_PATH / GROOVY / LOCAL_FUNC） |
| `MqSourceConfig.java` | domain/model | MQ 接入配置 |
| `FieldMapping.java` | domain/model | 字段映射规则（含策略、表达式、sourceType） |
| `DateTimeParser.java` | domain/service | 时间字段统一解析（EPOCH_SECOND / EPOCH_MILLIS / DATETIME_STRING） |
| `EventPropertyValidator.java` | domain/service | 事件属性校验器 |
| `FieldExtractor.java` | infrastructure/mq/extract | 字段提取策略接口 |
| `JsonPathExtractor.java` | infrastructure/mq/extract | JSONPath 提取实现 |
| `GroovyExtractor.java` | infrastructure/mq/extract | Groovy 脚本提取实现（含沙箱） |
| `LocalFuncExtractor.java` | infrastructure/mq/extract | 本地函数提取实现（注册表模式） |
| `DynamicMqConsumerManager.java` | infrastructure/mq | 动态消费者管理器 |
| `EventMessageParser.java` | infrastructure/mq | 外部消息解析引擎（调度多策略提取器） |

---

## 七、兼容性与风险

| 项目 | 说明 |
|------|------|
| **向后兼容** | `source_type` 默认值 `SDK`，已有事件定义无需修改；已有 HTTP 上报接口行为不变（仅增加属性校验，Permissive 模式可配置） |
| **消费者组冲突** | 每个 MQ 事件定义的 consumerGroup 必须唯一，创建时校验 |
| **消费者异常恢复** | `@PostConstruct` 启动时重建所有消费者；消费失败记录日志，不阻塞其他消息 |
| **RocketMQ 连接数** | 每个 MQ 事件定义创建一个 `DefaultMQPushConsumer`，需监控连接数；大量 MQ 事件定义时考虑合并消费者 |
| **JSONPath 安全** | Jayway JsonPath 不执行代码，无注入风险；异常提取返回 null |
| **Groovy 沙箱** | 必须限制可调用类/方法，禁止文件 IO、网络、反射、System.exit；单次执行超时 ≤ 500ms |
| **时间解析** | DATETIME_STRING 按固定格式列表依次尝试，覆盖 ISO-8601 / `yyyy-MM-dd HH:mm:ss` / `yyyy/MM/dd HH:mm:ss` / `yyyyMMddHHmmss` |

---

## 八、后续迭代方向（不在本期范围）

| 方向 | 说明 |
|------|------|
| Dead Letter Queue | 解析/校验失败的消息投递 DLQ，支持人工排查和重试 |
| Schema 版本管理 | PropertyDefinition 变更历史记录，支持回溯 |
| Webhook 接入 | 新增 sourceType=WEBHOOK，平台生成回调 URL 供外部系统推送 |
| 事件质量看板 | 统计各事件的上报量、校验通过率、失败原因分布 |
| 本地函数扩展 | 按业务需求持续扩充 LocalFuncExtractor 的预置函数库 |