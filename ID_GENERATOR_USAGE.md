# ID 生成工具使用指南

## 概述

`IdGenerator` 是一个功能强大的统一ID生成工具，支持多种ID生成策略，适用于不同的业务场景。

## 支持的ID类型

### 1. **Snowflake ID** (推荐用于数据库主键)
- **类型**: `Long`
- **特点**: 分布式唯一ID，64位，包含时间戳、机器ID、序列号
- **优势**: 高性能、全局唯一、可排序、适合分布式环境
- **示例**: `1234567890123456789`

### 2. **ULID** (推荐用于需要排序的String ID)
- **类型**: `String`
- **特点**: 26个字符，可排序的UUID替代品
- **优势**: 可排序、URL安全、时间戳包含
- **示例**: `01ARZ3NDEKTSV4RRFFQ69G5FAV`

### 3. **NanoID** (推荐用于URL友好场景)
- **类型**: `String`
- **特点**: 21个字符（可自定义长度），URL友好
- **优势**: 短小精悍、URL安全、性能好
- **示例**: `V1StGXR8_Z5jdHi6B-myT`

### 4. **UUID** (标准UUID)
- **类型**: `String`
- **特点**: 标准UUID格式
- **优势**: 标准、广泛支持
- **示例**: `550e8400-e29b-41d4-a716-446655440000`

### 5. **时间戳ID** (简单场景)
- **类型**: `Long`
- **特点**: 时间戳 + 序列号
- **优势**: 简单、包含时间信息
- **示例**: `17040672000001234`

### 6. **业务ID** (带前缀)
- **类型**: `String`
- **特点**: 带业务前缀的ULID
- **优势**: 语义清晰、便于识别
- **示例**: `task_01ARZ3NDEKTSV4RRFFQ69G5FAV`

## 使用方法

### 基本使用

```java
@Inject
private IdGenerator idGenerator;

// 生成 Snowflake ID（推荐用于数据库主键）
Long agentId = idGenerator.nextSnowflakeId();

// 生成 ULID（推荐用于需要排序的String ID）
String taskId = idGenerator.nextUlid();

// 生成 NanoID（URL友好）
String docId = idGenerator.nextNanoId();

// 生成标准UUID
String uuid = idGenerator.nextUuid();

// 生成不带连字符的UUID
String uuidSimple = idGenerator.nextUuidWithoutHyphens();

// 生成时间戳ID
Long timestampId = idGenerator.nextTimestampId();
```

### 业务ID生成

```java
// 生成任务ID（格式：task_ + ULID）
String taskId = idGenerator.nextTaskId();
// 输出: task_01ARZ3NDEKTSV4RRFFQ69G5FAV

// 生成会话ID（格式：session_ + ULID）
String sessionId = idGenerator.nextSessionId();
// 输出: session_01ARZ3NDEKTSV4RRFFQ69G5FAV

// 生成用户ID（格式：user_ + ULID）
String userId = idGenerator.nextUserId();
// 输出: user_01ARZ3NDEKTSV4RRFFQ69G5FAV

// 生成文档ID（格式：doc_ + ULID）
String docId = idGenerator.nextDocId();
// 输出: doc_01ARZ3NDEKTSV4RRFFQ69G5FAV
```

### 使用枚举类型

```java
// 根据类型生成ID
Long snowflakeId = (Long) idGenerator.nextId(IdGenerator.IdType.SNOWFLAKE);
String ulid = (String) idGenerator.nextId(IdGenerator.IdType.ULID);
String nanoid = (String) idGenerator.nextId(IdGenerator.IdType.NANOID);
String uuid = (String) idGenerator.nextId(IdGenerator.IdType.UUID);
Long timestampId = (Long) idGenerator.nextId(IdGenerator.IdType.TIMESTAMP);
```

### 自定义NanoID长度

```java
// 生成10个字符的NanoID
String shortId = idGenerator.nextNanoId(10);

// 生成32个字符的NanoID
String longId = idGenerator.nextNanoId(32);
```

## 实际应用示例

### 1. 在实体类中使用

```java
@Entity
public class AgentEntity {
    @Id
    private Long id;
    
    // 在创建时自动生成ID
    public static AgentEntity create(String name) {
        AgentEntity entity = new AgentEntity();
        entity.setId(idGenerator.nextSnowflakeId()); // 使用Snowflake ID
        entity.setName(name);
        return entity;
    }
}
```

### 2. 在Service中使用

```java
@Singleton
public class TaskService {
    @Inject
    private IdGenerator idGenerator;
    
    public Task createTask(String title) {
        Task task = new Task();
        task.setId(idGenerator.nextTaskId()); // 使用业务ID
        task.setTitle(title);
        return task;
    }
}
```

### 3. 在Controller中使用

```java
@Controller("/task")
public class TaskController {
    @Inject
    private IdGenerator idGenerator;
    
    @Post("/execute")
    public Flux<Event> execute(@Body TaskExecuteRequest request) {
        // 如果没有sessionId，自动生成
        String sessionId = request.getSessionId() != null 
            ? request.getSessionId() 
            : idGenerator.nextSessionId();
        
        // 执行任务...
    }
}
```

### 4. 替换现有的ID生成方式

#### 替换 System.currentTimeMillis()

**之前**:
```java
kgEntityPO.setId(System.currentTimeMillis());
```

**之后**:
```java
@Inject
private IdGenerator idGenerator;

kgEntityPO.setId(idGenerator.nextSnowflakeId());
```

#### 替换 UUID.randomUUID()

**之前**:
```java
String sessionId = UUID.randomUUID().toString();
```

**之后**:
```java
@Inject
private IdGenerator idGenerator;

String sessionId = idGenerator.nextSessionId();
// 或者
String sessionId = idGenerator.nextUlid();
```

## 选择建议

### 数据库主键（Long类型）
- ✅ **推荐**: `nextSnowflakeId()`
- 原因: 高性能、全局唯一、可排序、适合索引

### 业务ID（String类型，需要排序）
- ✅ **推荐**: `nextUlid()` 或 `nextTaskId()` 等业务ID
- 原因: 可排序、包含时间信息、URL安全

### URL友好ID
- ✅ **推荐**: `nextNanoId()`
- 原因: 短小精悍、URL安全、性能好

### 标准UUID场景
- ✅ **推荐**: `nextUuid()` 或 `nextUuidWithoutHyphens()`
- 原因: 标准格式、广泛支持

### 简单时间戳ID
- ✅ **推荐**: `nextTimestampId()`
- 原因: 简单、包含时间信息（但可能冲突，不推荐高并发场景）

## 性能对比

| ID类型 | 生成速度 | 长度 | 排序性 | 分布式支持 |
|--------|---------|------|--------|-----------|
| Snowflake | ⭐⭐⭐⭐⭐ | 19位数字 | ✅ | ✅ |
| ULID | ⭐⭐⭐⭐ | 26字符 | ✅ | ✅ |
| NanoID | ⭐⭐⭐⭐⭐ | 21字符（可调） | ❌ | ✅ |
| UUID | ⭐⭐⭐⭐ | 36字符 | ❌ | ✅ |
| 时间戳ID | ⭐⭐⭐⭐⭐ | 17位数字 | ✅ | ❌ |

## 注意事项

1. **Snowflake ID**: 
   - 需要确保系统时钟同步
   - 机器ID和数据中心ID会自动生成（基于MAC地址和主机名）
   - 如果需要在多机器间协调，建议配置固定的workerId和datacenterId

2. **ULID**:
   - 可排序，适合需要按时间顺序查询的场景
   - URL安全，可以直接用于URL

3. **NanoID**:
   - 默认21个字符，可以根据需要调整长度
   - URL友好，适合用于短链接等场景

4. **UUID**:
   - 标准格式，但不可排序
   - 如果不需要排序，ULID是更好的选择

## 迁移指南

### 步骤1: 注入IdGenerator

```java
@Singleton
public class YourService {
    @Inject
    private IdGenerator idGenerator;
}
```

### 步骤2: 替换现有ID生成代码

查找并替换：
- `System.currentTimeMillis()` → `idGenerator.nextSnowflakeId()`
- `UUID.randomUUID().toString()` → `idGenerator.nextUlid()` 或业务ID方法

### 步骤3: 测试验证

确保生成的ID符合预期，特别是：
- 唯一性
- 格式正确
- 性能满足要求

## 总结

`IdGenerator` 提供了统一的ID生成接口，支持多种ID生成策略，可以根据不同的业务场景选择最合适的ID类型。推荐使用：

- **数据库主键**: Snowflake ID
- **业务ID**: ULID 或带前缀的业务ID
- **URL友好**: NanoID
- **标准场景**: UUID

