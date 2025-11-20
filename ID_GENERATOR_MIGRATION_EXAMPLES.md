# ID生成器迁移示例

## 需要迁移的代码位置

### 1. RagService.java - 替换 System.currentTimeMillis()

**位置**: `src/main/java/com/example/ddd/domain/agent/service/RagService.java:270`

**之前**:
```java
kgEntityPO.setId(System.currentTimeMillis());
```

**之后**:
```java
@Inject
private IdGenerator idGenerator;

// 在方法中使用
kgEntityPO.setId(idGenerator.nextSnowflakeId());
```

### 2. TaskExecuteController.java - 替换 UUID.randomUUID()

**位置**: `src/main/java/com/example/ddd/trigger/controller/TaskExecuteController.java:56`

**之前**:
```java
String sessionId = request.getSessionId() != null ? request.getSessionId() : 
    java.util.UUID.randomUUID().toString();
```

**之后**:
```java
@Inject
private IdGenerator idGenerator;

String sessionId = request.getSessionId() != null 
    ? request.getSessionId() 
    : idGenerator.nextSessionId();
```

### 3. DocService.java - 替换 UUID生成

**位置**: `src/main/java/com/example/ddd/domain/doc/service/DocService.java:37`

**之前**:
```java
docPO.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
```

**之后**:
```java
@Inject
private IdGenerator idGenerator;

docPO.setId(idGenerator.nextDocId());
// 或者如果只需要ID部分（不带前缀）
docPO.setId(idGenerator.nextUlid());
```

## 完整迁移示例

### 示例1: RagService.java

```java
package com.example.ddd.domain.agent.service;

import com.example.ddd.common.utils.IdGenerator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
// ... 其他导入

@Singleton
@Slf4j
public class RagService {
    
    @Inject
    private IdGenerator idGenerator; // 添加注入
    
    // ... 其他字段
    
    public void importDocument(Long ragId, String docId) {
        dslContextFactory.execute((dsl) -> {
            // ... 现有代码 ...
            
            List<KgEntityPO> list = entities.stream().map(entity -> {
                KgEntityPO kgEntityPO = new KgEntityPO();
                kgEntityPO.setName(entity.getName());
                kgEntityPO.setType(entity.getType());
                kgEntityPO.setDescription(entity.getDescription());
                kgEntityPO.setEmbedding(embeddingModel.embed(TextSegment.from(entity.getName())).content().vector());
                
                // 替换这里
                kgEntityPO.setId(idGenerator.nextSnowflakeId()); // ✅ 使用Snowflake ID
                
                return kgEntityPO;
            }).toList();
            
            // ... 其他代码 ...
        });
    }
}
```

### 示例2: TaskExecuteController.java

```java
package com.example.ddd.trigger.controller;

import com.example.ddd.common.utils.IdGenerator;
import jakarta.inject.Inject;
// ... 其他导入

@Slf4j
@Controller("/task")
public class TaskExecuteController {
    
    @Inject
    private IdGenerator idGenerator; // 添加注入
    
    @Inject
    private BeanUtil beanUtil;
    
    // ... 其他字段
    
    @Post(value = "/execute", produces = MediaType.TEXT_EVENT_STREAM)
    public Flux<UserContext.TaskStatusEvent> execute(@Body TaskExecuteRequest request) {
        // ... 现有代码 ...
        
        // 设置用户ID和会话ID
        String userId = request.getUserId() != null 
            ? request.getUserId() 
            : idGenerator.nextUserId(); // ✅ 使用业务ID
        
        String sessionId = request.getSessionId() != null 
            ? request.getSessionId() 
            : idGenerator.nextSessionId(); // ✅ 使用业务ID
        
        userContext.setUserId(userId);
        userContext.setSessionId(sessionId);
        
        // ... 其他代码 ...
    }
}
```

### 示例3: DocService.java

```java
package com.example.ddd.domain.doc.service;

import com.example.ddd.common.utils.IdGenerator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
// ... 其他导入

@Singleton
public class DocService {
    
    @Inject
    private IdGenerator idGenerator; // 添加注入
    
    // ... 其他字段
    
    public DocEntity createDoc(String text) {
        DocPO docPO = new DocPO();
        
        // 替换这里
        docPO.setId(idGenerator.nextDocId()); // ✅ 使用业务ID
        // 或者如果只需要ID部分
        // docPO.setId(idGenerator.nextUlid());
        
        docPO.setText(text);
        // ... 其他设置 ...
        
        return convertToEntity(docPO);
    }
}
```

## 批量替换建议

### 使用IDE的查找替换功能

1. **查找**: `System.currentTimeMillis()`
   **替换为**: `idGenerator.nextSnowflakeId()`
   **注意**: 需要先注入 `IdGenerator`

2. **查找**: `UUID.randomUUID().toString()`
   **替换为**: `idGenerator.nextUlid()` 或相应的业务ID方法

3. **查找**: `UUID.randomUUID().toString().replace("-", "")`
   **替换为**: `idGenerator.nextUuidWithoutHyphens()`

## 验证步骤

1. **编译检查**: 确保所有代码编译通过
2. **单元测试**: 运行相关单元测试
3. **集成测试**: 测试ID生成是否正常工作
4. **性能测试**: 验证ID生成性能是否满足要求

## 注意事项

1. **依赖注入**: 确保所有使用 `IdGenerator` 的类都正确注入了依赖
2. **ID类型选择**: 根据业务场景选择合适的ID类型
3. **向后兼容**: 如果现有ID是Long类型，继续使用 `nextSnowflakeId()`
4. **向后兼容**: 如果现有ID是String类型，可以使用 `nextUlid()` 或业务ID方法

