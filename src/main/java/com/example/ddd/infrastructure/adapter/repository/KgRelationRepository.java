package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.model.valobj.RelationDTO;
import com.example.ddd.infrastructure.config.Neo4jDriverFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.neo4j.driver.Values;

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
            // 先确保所有实体节点存在
            for (Map.Entry<String, Long> entry : entityIdMap.entrySet()) {
                String entityName = entry.getKey();
                Long entityId = entry.getValue();
                String entityType = entityTypeMap.getOrDefault(entityName, "Unknown");
                
                // 使用MERGE确保节点存在，如果不存在则创建
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

            // 创建关系
            for (RelationDTO relation : relations) {
                Long subjectId = entityIdMap.get(relation.getSubjectName());
                Long objectId = entityIdMap.get(relation.getObjectName());

                if (subjectId != null && objectId != null) {
                    // 使用MERGE确保关系存在，如果不存在则创建
                    // 关系类型作为关系的标签
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
     * 删除指定文档相关的所有关系和实体节点
     * 
     * @param docId 文档ID
     */
    public void deleteByDocId(String docId) {
        neo4jDriverFactory.execute(tx -> {
            tx.run("""
                MATCH (e:Entity)-[r]->()
                WHERE e.docId = $docId
                DELETE r, e
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
            // 查询这些实体及其关系
            // 使用更简单的查询，先获取所有相关节点，再获取边
            var nodeResult = tx.run("""
                MATCH (e:Entity)
                WHERE e.id IN $entityIds
                RETURN e.id as id, e.name as name, e.type as type
                """,
                Values.parameters("entityIds", entityIds)
            );
            
            var nodesList = new java.util.ArrayList<Map<String, Object>>();
            var nodeIdSet = new java.util.HashSet<Long>();
            
            nodeResult.forEachRemaining(record -> {
                Long nodeId = record.get("id").asLong();
                nodeIdSet.add(nodeId);
                nodesList.add(Map.of(
                    "id", nodeId,
                    "name", record.get("name").asString(),
                    "type", record.get("type").asString()
                ));
            });
            
            // 查询这些节点之间的关系
            var edgeResult = tx.run("""
                MATCH (s:Entity)-[r:RELATION]->(t:Entity)
                WHERE s.id IN $entityIds AND t.id IN $entityIds
                RETURN id(r) as id, r.type as type, s.id as source, t.id as target, 
                       COALESCE(r.evidence, '') as evidence
                """,
                Values.parameters("entityIds", entityIds)
            );
            
            var edgesList = new java.util.ArrayList<Map<String, Object>>();
            edgeResult.forEachRemaining(record -> {
                edgesList.add(Map.of(
                    "id", record.get("id").asLong(),
                    "type", record.get("type").asString(),
                    "source", record.get("source").asLong(),
                    "target", record.get("target").asLong(),
                    "evidence", record.get("evidence").asString()
                ));
            });
            
            return Map.of(
                "nodes", nodesList,
                "edges", edgesList
            );
        });
    }
}

