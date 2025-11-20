package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.model.valobj.RelationDTO;
import com.example.ddd.infrastructure.config.Neo4jDriverFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.neo4j.driver.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 知识图谱关系仓储
 * 负责在Neo4j中存储和管理实体之间的关系
 */
@Singleton
public class KgRelationRepository {
    
    @Inject
    private Neo4jDriverFactory neo4jDriverFactory;

    /**
     * 根据RAG ID查询文档ID列表（从Neo4j）
     * 
     * @param ragId RAG ID
     * @return 文档ID列表
     */
    public List<String> queryDocIdsByRagId(Long ragId) {
        return neo4jDriverFactory.callableRead(tx -> {
            var result = tx.run("""
                MATCH (d:Document)
                WHERE d.ragId = $ragId
                RETURN d.id as docId
                ORDER BY d.id
                """,
                Values.parameters("ragId", ragId)
            );
            
            var docIds = new ArrayList<String>();
            result.forEachRemaining(record -> {
                docIds.add(record.get("docId").asString());
            });
            return docIds;
        });
    }

    /**
     * 创建文档到实体的关系（在Neo4j中），并存储实体出现的句子
     * 
     * @param docId 文档ID
     * @param ragId RAG ID（可选，用于关联文档和RAG）
     * @param entityIds 实体ID列表
     * @param entitySentencesMap 实体ID到句子列表的映射
     * @param entityIdMap 实体名称到ID的映射（用于创建实体节点）
     * @param entityTypeMap 实体名称到类型的映射（用于创建实体节点）
     */
    public void createDocToEntitiesRelation(String docId, Long ragId, List<Long> entityIds, 
                                           Map<Long, List<String>> entitySentencesMap,
                                           Map<String, Long> entityIdMap, 
                                           Map<String, String> entityTypeMap) {
        if (entityIds == null || entityIds.isEmpty()) {
            return;
        }
        
        neo4jDriverFactory.execute(tx -> {
            // 创建或更新文档节点，存储ragId
            if (ragId != null) {
                tx.run("""
                    MERGE (d:Document {id: $docId})
                    SET d.type = 'DOCUMENT', d.ragId = $ragId
                    """,
                    Values.parameters("docId", docId, "ragId", ragId)
                );
            } else {
                tx.run("""
                    MERGE (d:Document {id: $docId})
                    SET d.type = 'DOCUMENT'
                    """,
                    Values.parameters("docId", docId)
                );
            }
            
            // 为每个实体创建文档->实体的关系，并存储句子
            // 先确保实体节点存在（使用MERGE）
            for (Long entityId : entityIds) {
                // 从entityIdMap中找到对应的实体名称和类型
                String entityName = null;
                String entityType = "Unknown";
                for (Map.Entry<String, Long> entry : entityIdMap.entrySet()) {
                    if (entry.getValue().equals(entityId)) {
                        entityName = entry.getKey();
                        entityType = entityTypeMap.getOrDefault(entityName, "Unknown");
                        break;
                    }
                }
                
                // 如果找到了实体名称，先创建或更新实体节点
                if (entityName != null) {
                    tx.run("""
                        MERGE (e:Entity {id: $entityId})
                        SET e.name = $name, e.type = $type
                        """,
                        Values.parameters(
                            "entityId", entityId,
                            "name", entityName,
                            "type", entityType
                        )
                    );
                } else {
                    // 如果找不到实体名称，至少确保实体节点存在
                    tx.run("""
                        MERGE (e:Entity {id: $entityId})
                        """,
                        Values.parameters("entityId", entityId)
                    );
                }
                
                // 创建文档->实体的关系
                List<String> sentences = entitySentencesMap.getOrDefault(entityId, List.of());
                String sentencesJson = sentences.isEmpty() ? "[]" : 
                    "[" + sentences.stream()
                        .map(s -> "\"" + s.replace("\"", "\\\"").replace("\n", "\\n") + "\"")
                        .collect(java.util.stream.Collectors.joining(",")) + "]";
                
                tx.run("""
                    MATCH (d:Document {id: $docId})
                    MATCH (e:Entity {id: $entityId})
                    MERGE (d)-[r:CONTAINS_ENTITY]->(e)
                    SET r.createdAt = timestamp(),
                        r.sentences = $sentences
                    """,
                    Values.parameters(
                        "docId", docId,
                        "entityId", entityId,
                        "sentences", sentencesJson
                    )
                );
            }
        });
    }

    /**
     * 批量创建实体节点和关系
     * 
     * @param entityIdMap 实体名称到ID的映射
     * @param entityTypeMap 实体名称到类型的映射
     * @param relations 关系列表
     */
    public void batchCreateRelations(Map<String, Long> entityIdMap, 
                                     Map<String, String> entityTypeMap,
                                     List<RelationDTO> relations) {
        if (relations == null || relations.isEmpty()) {
            return;
        }
        neo4jDriverFactory.execute(tx -> {
            for (Map.Entry<String, Long> entry : entityIdMap.entrySet()) {
                String entityName = entry.getKey();
                Long entityId = entry.getValue();
                String entityType = entityTypeMap.getOrDefault(entityName, "Unknown");
                tx.run("""
                    MERGE (e:Entity {id: $id, name: $name})
                    SET e.type = $type
                    """,
                    Values.parameters(
                        "id", entityId,
                        "name", entityName,
                        "type", entityType
                    )
                );
            }
            for (RelationDTO relation : relations) {
                Long subjectId = entityIdMap.get(relation.getSubjectName());
                Long objectId = entityIdMap.get(relation.getObjectName());
                if (subjectId != null && objectId != null) {
                    String relationType = relation.getRelationType();
                    String evidence = relation.getEvidenceText() != null ? relation.getEvidenceText() : "";
                    tx.run("""
                        MATCH (s:Entity {id: $subjectId})
                        MATCH (o:Entity {id: $objectId})
                        MERGE (s)-[r:RELATION {type: $relationType, evidence: $evidence}]->(o)
                        SET r.createdAt = timestamp()
                        """,
                        Values.parameters(
                            "subjectId", subjectId,
                            "objectId", objectId,
                            "relationType", relationType,
                            "evidence", evidence
                        )
                    );
                }
            }
        });
    }

    /**
     * 根据文档ID查询关联的实体和句子（已废弃，改用从PostgreSQL查询实体信息）
     * 
     * @param docId 文档ID
     * @return 包含实体ID、实体名称、实体类型和句子列表的Map列表
     * @deprecated 使用RagService.getEntitiesByDocId替代，该方法从PostgreSQL查询实体信息
     */
    @Deprecated
    public List<Map<String, Object>> queryEntitiesByDocId(String docId) {
        return neo4jDriverFactory.callableRead(tx -> {
            var result = tx.run("""
                MATCH (d:Document {id: $docId})-[r:CONTAINS_ENTITY]->(e:Entity)
                RETURN e.id as entityId, e.name as entityName, e.type as entityType, 
                       COALESCE(r.sentences, '[]') as sentences
                ORDER BY e.name
                """,
                Values.parameters("docId", docId)
            );
            
            var entitiesList = new java.util.ArrayList<Map<String, Object>>();
            result.forEachRemaining(record -> {
                try {
                    String sentencesJson = record.get("sentences").asString();
                    List<String> sentences = new java.util.ArrayList<>();
                    if (sentencesJson != null && !sentencesJson.equals("[]")) {
                        // 解析JSON数组
                        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        sentences = mapper.readValue(sentencesJson, 
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                    }
                    
                    entitiesList.add(Map.of(
                        "entityId", record.get("entityId").asLong(),
                        "entityName", record.get("entityName").asString(),
                        "entityType", record.get("entityType").asString(),
                        "sentences", sentences
                    ));
                } catch (Exception e) {
                    // 如果解析失败，使用空列表
                    entitiesList.add(Map.of(
                        "entityId", record.get("entityId").asLong(),
                        "entityName", record.get("entityName").asString(),
                        "entityType", record.get("entityType").asString(),
                        "sentences", List.<String>of()
                    ));
                }
            });
            return entitiesList;
        });
    }

    /**
     * 根据文档ID和实体ID列表查询句子信息（仅从Neo4j查询句子）
     * 
     * @param docId 文档ID
     * @param entityIds 实体ID列表
     * @return 实体ID到句子列表的映射
     */
    public Map<Long, List<String>> querySentencesByDocIdAndEntityIds(String docId, List<Long> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            return Map.of();
        }
        
        return neo4jDriverFactory.callableRead(tx -> {
            var result = tx.run("""
                MATCH (d:Document {id: $docId})-[r:CONTAINS_ENTITY]->(e:Entity)
                WHERE e.id IN $entityIds
                RETURN e.id as entityId, COALESCE(r.sentences, '[]') as sentences
                """,
                Values.parameters("docId", docId, "entityIds", entityIds)
            );
            
            var sentencesMap = new java.util.HashMap<Long, List<String>>();
            result.forEachRemaining(record -> {
                try {
                    Long entityId = record.get("entityId").asLong();
                    String sentencesJson = record.get("sentences").asString();
                    List<String> sentences = new java.util.ArrayList<>();
                    if (sentencesJson != null && !sentencesJson.equals("[]")) {
                        // 解析JSON数组
                        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        sentences = mapper.readValue(sentencesJson, 
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                    }
                    sentencesMap.put(entityId, sentences);
                } catch (Exception e) {
                    // 如果解析失败，使用空列表
                    Long entityId = record.get("entityId").asLong();
                    sentencesMap.put(entityId, List.<String>of());
                }
            });
            return sentencesMap;
        });
    }

    /**
     * 根据实体ID和文档ID查询该实体在该文档中的实体关系图谱
     * 
     * @param entityId 实体ID
     * @param docId 文档ID
     * @return 包含节点和边的图谱数据
     */
    public Map<String, Object> queryEntityGraphInDoc(Long entityId, String docId) {
        return neo4jDriverFactory.callableRead(tx -> {
            // 先查询该文档中的所有实体ID
            var docEntitiesResult = tx.run("""
                MATCH (d:Document {id: $docId})-[:CONTAINS_ENTITY]->(e:Entity)
                RETURN e.id as id
                """,
                Values.parameters("docId", docId)
            );
            
            var docEntityIds = new ArrayList<Long>();
            docEntitiesResult.forEachRemaining(record -> {
                docEntityIds.add(record.get("id").asLong());
            });
            
            if (docEntityIds.isEmpty() || !docEntityIds.contains(entityId)) {
                return Map.of("nodes", List.of(), "edges", List.of());
            }
            
            // 查询该文档中与目标实体有直接关系的所有实体（通过RELATION关系）
            var relatedEntityIds = new ArrayList<Long>();
            relatedEntityIds.add(entityId); // 包含目标实体本身
            
            // 查找与目标实体有关系的实体（在该文档中的）
            var relatedResult = tx.run("""
                MATCH (d:Document {id: $docId})-[:CONTAINS_ENTITY]->(e1:Entity)-[r:RELATION]-(e2:Entity)
                WHERE (e1.id = $entityId AND EXISTS((d)-[:CONTAINS_ENTITY]->(e2)))
                    OR (e2.id = $entityId AND EXISTS((d)-[:CONTAINS_ENTITY]->(e1)))
                RETURN DISTINCT e1.id as id1, e2.id as id2
                """,
                Values.parameters("docId", docId, "entityId", entityId)
            );
            
            relatedResult.forEachRemaining(record -> {
                Long id1 = record.get("id1").asLong();
                Long id2 = record.get("id2").asLong();
                if (!relatedEntityIds.contains(id1)) relatedEntityIds.add(id1);
                if (!relatedEntityIds.contains(id2)) relatedEntityIds.add(id2);
            });
            
            if (relatedEntityIds.isEmpty()) {
                // 如果没有任何关系，只返回目标实体本身
                var entityResult = tx.run("""
                    MATCH (e:Entity {id: $entityId})
                    RETURN e.id as id, e.name as name, e.type as type
                    """,
                    Values.parameters("entityId", entityId)
                );
                if (entityResult.hasNext()) {
                    var record = entityResult.next();
                    return Map.of(
                        "nodes", List.of(Map.of(
                            "id", entityId,
                            "name", record.get("name").asString(),
                            "type", record.get("type").asString(),
                            "nodeType", "entity"
                        )),
                        "edges", List.of()
                    );
                }
                return Map.of("nodes", List.of(), "edges", List.of());
            }
            
            // 查询这些实体的详细信息
            var nodeResult = tx.run("""
                MATCH (e:Entity)
                WHERE e.id IN $entityIds
                RETURN e.id as id, e.name as name, e.type as type
                """,
                Values.parameters("entityIds", relatedEntityIds)
            );
            
            var nodesList = new ArrayList<Map<String, Object>>();
            var nodeIdSet = new java.util.HashSet<Long>();
            
            nodeResult.forEachRemaining(record -> {
                Long nodeId = record.get("id").asLong();
                nodeIdSet.add(nodeId);
                nodesList.add(Map.of(
                    "id", nodeId,
                    "name", record.get("name").asString(),
                    "type", record.get("type").asString(),
                    "nodeType", "entity"
                ));
            });
            
            // 查询这些实体之间的关系（仅限该文档中的实体）
            var edgesList = new ArrayList<Map<String, Object>>();
            if (!nodeIdSet.isEmpty()) {
                var edgeResult = tx.run("""
                    MATCH (d:Document {id: $docId})-[:CONTAINS_ENTITY]->(s:Entity)-[r:RELATION]->(t:Entity)
                    WHERE EXISTS((d)-[:CONTAINS_ENTITY]->(t))
                    AND s.id IN $entityIds AND t.id IN $entityIds
                    RETURN id(r) as id, r.type as type, s.id as source, t.id as target, 
                           COALESCE(r.evidence, '') as evidence
                    """,
                    Values.parameters("docId", docId, "entityIds", new ArrayList<>(nodeIdSet))
                );
                
                edgeResult.forEachRemaining(record -> {
                    edgesList.add(Map.of(
                        "id", "er-" + record.get("id").asLong(),
                        "type", record.get("type").asString(),
                        "source", record.get("source").asLong(),
                        "target", record.get("target").asLong(),
                        "evidence", record.get("evidence").asString(),
                        "edgeType", "entity_relation"
                    ));
                });
            }
            
            return Map.of(
                "nodes", nodesList,
                "edges", edgesList
            );
        });
    }

    /**
     * 删除指定文档相关的所有关系和实体节点
     * 
     * @param docId 文档ID
     */
    public void deleteByDocId(String docId) {
        neo4jDriverFactory.execute(tx -> {
            tx.run("""
                MATCH (d:Document {id: $docId})-[r:CONTAINS_ENTITY]->(e:Entity)
                DELETE r
                """,
                Values.parameters("docId", docId)
            );
            tx.run("""
                MATCH (d:Document {id: $docId})
                DELETE d
                """,
                Values.parameters("docId", docId)
            );
        });
    }

    /**
     * 查询两个实体之间的路径
     * 
     * @param startEntityId 起始实体ID
     * @param endEntityId 结束实体ID
     * @param maxDepth 最大深度
     * @return 路径列表
     */
    public List<Map<String, Object>> findPath(Long startEntityId, Long endEntityId, int maxDepth) {
        return neo4jDriverFactory.callableRead(tx -> {
            var result = tx.run("""
                MATCH path = shortestPath((start:Entity {id: $startId})-[*1..%d]-(end:Entity {id: $endId}))
                RETURN path
                """.formatted(maxDepth),
                Values.parameters("startId", startEntityId, "endId", endEntityId)
            );
            return result.list(record -> record.get("path").asMap());
        });
    }

    /**
     * 查询实体及其所有关系（用于知识图谱可视化）
     * 包括文档节点和文档->实体的关系
     * 
     * @param entityIds 实体ID列表
     * @param maxDepth 最大深度（默认1，只查询直接关系）
     * @return 包含节点和边的图谱数据
     */
    public Map<String, Object> queryGraphByEntityIds(List<Long> entityIds, int maxDepth) {
        if (entityIds == null || entityIds.isEmpty()) {
            return Map.of("nodes", List.of(), "edges", List.of());
        }

        return neo4jDriverFactory.callableRead(tx -> {
            var nodesList = new java.util.ArrayList<Map<String, Object>>();
            var edgesList = new java.util.ArrayList<Map<String, Object>>();
            var nodeIdSet = new java.util.HashSet<Object>(); // 使用Object以支持Long和String类型的ID
            var entityNodeResult = tx.run("""
                MATCH (e:Entity)
                WHERE e.id IN $entityIds
                RETURN e.id as id, e.name as name, e.type as type, 'entity' as nodeType
                """,
                Values.parameters("entityIds", entityIds)
            );
            entityNodeResult.forEachRemaining(record -> {
                Long nodeId = record.get("id").asLong();
                nodeIdSet.add(nodeId);
                nodesList.add(Map.of(
                    "id", nodeId,
                    "name", record.get("name").asString(),
                    "type", record.get("type").asString(),
                    "nodeType", "entity"
                ));
            });
            
            // 查询文档节点（通过CONTAINS_ENTITY关系）
            var docNodeResult = tx.run("""
                MATCH (d:Document)-[:CONTAINS_ENTITY]->(e:Entity)
                WHERE e.id IN $entityIds
                RETURN DISTINCT d.id as id, d.id as name, 'DOCUMENT' as type, 'document' as nodeType
                """,
                Values.parameters("entityIds", entityIds)
            );
            
            docNodeResult.forEachRemaining(record -> {
                String docId = record.get("id").asString();
                if (!nodeIdSet.contains(docId)) {
                    nodeIdSet.add(docId);
                    nodesList.add(Map.of(
                        "id", docId,
                        "name", docId,
                        "type", "DOCUMENT",
                        "nodeType", "document"
                    ));
                }
            });
            
            // 查询实体之间的关系
            var entityEdgeResult = tx.run("""
                MATCH (s:Entity)-[r:RELATION]->(t:Entity)
                WHERE s.id IN $entityIds AND t.id IN $entityIds
                RETURN id(r) as id, r.type as type, s.id as source, t.id as target, 
                       COALESCE(r.evidence, '') as evidence, 'entity_relation' as edgeType
                """,
                Values.parameters("entityIds", entityIds)
            );
            
            entityEdgeResult.forEachRemaining(record -> {
                edgesList.add(Map.of(
                    "id", "er-" + record.get("id").asLong(),
                    "type", record.get("type").asString(),
                    "source", record.get("source").asLong(),
                    "target", record.get("target").asLong(),
                    "evidence", record.get("evidence").asString(),
                    "edgeType", "entity_relation"
                ));
            });
            
            // 查询文档->实体的关系
            var docEdgeResult = tx.run("""
                MATCH (d:Document)-[r:CONTAINS_ENTITY]->(e:Entity)
                WHERE e.id IN $entityIds
                RETURN d.id as source, e.id as target, 'CONTAINS_ENTITY' as type, 'doc_entity' as edgeType
                """,
                Values.parameters("entityIds", entityIds)
            );
            
            // 使用数组来存储计数器，因为数组引用是final的，但数组内容可以修改
            long[] docEdgeIdCounter = {1000000L}; // 使用大数字避免与实体关系ID冲突
            docEdgeResult.forEachRemaining(record -> {
                edgesList.add(Map.of(
                    "id", "de-" + (docEdgeIdCounter[0]++),
                    "type", "CONTAINS_ENTITY",
                    "source", record.get("source").asString(),
                    "target", record.get("target").asLong(),
                    "edgeType", "doc_entity"
                ));
            });
            
            return Map.of(
                "nodes", nodesList,
                "edges", edgesList
            );
        });
    }
}

