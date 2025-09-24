package org.icao4.eqasbackend2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.entity.vocab.AvVocab;
import org.icao4.eqasbackend2.entity.vocab.AvVocabTopic;
import org.icao4.eqasbackend2.entity.vocab.AvVocabTopicMap;
import org.icao4.eqasbackend2.service.vocab.AvVocabService;
import org.icao4.eqasbackend2.service.vocab.AvVocabTopicService;
import org.icao4.eqasbackend2.service.vocab.AvVocabTopicMapService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 航空词汇控制器
 * 提供词汇、词汇主题、词汇主题映射的完整API接口
 */
@RestController
@RequestMapping("/vocab")
@RequiredArgsConstructor
@Slf4j
public class AvVocabController {

    private final AvVocabService avVocabService;
    private final AvVocabTopicService avVocabTopicService;
    private final AvVocabTopicMapService avVocabTopicMapService;

    // ==================== 词汇（AvVocab）CRUD操作 ====================

    /**
     * 创建词汇
     */
    @PostMapping
    @CacheEvict(value = {"avVocabs", "avVocabStats"}, allEntries = true)
    public ResponseEntity<AvVocab> createVocab(@Valid @RequestBody AvVocab vocab) {
        log.info("创建词汇: {}", vocab.getHeadword());
        
        try {
            vocab.setCreatedAt(LocalDateTime.now());
            AvVocab savedVocab = avVocabService.createVocab(vocab);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVocab);
        } catch (DataIntegrityViolationException e) {
            log.error("创建词汇失败，数据完整性约束违反: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("创建词汇失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据ID获取词汇详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AvVocab> getVocabById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") Boolean includeTopics) {
        log.info("获取词汇详情: ID={}, includeTopics={}", id, includeTopics);
        
        Optional<AvVocab> vocabOpt = avVocabService.getVocabById(id);
        if (vocabOpt.isPresent()) {
            AvVocab vocab = vocabOpt.get();
            
            // 如果需要包含主题信息，可以在这里添加主题数据
            if (includeTopics) {
                // TODO: 添加主题信息到vocab对象中
            }
            
            return ResponseEntity.ok(vocab);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 分页查询词汇
     */
    @GetMapping
    public ResponseEntity<Page<AvVocab>> getVocabs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("分页查询词汇: page={}, size={}, sort={}, direction={}", page, size, sort, direction);
        
        // 处理排序参数，支持 "field,direction" 格式
        String sortField = sort;
        String sortDir = direction;
        
        if (sort.contains(",")) {
            String[] sortParts = sort.split(",");
            sortField = sortParts[0];
            if (sortParts.length > 1) {
                sortDir = sortParts[1];
            }
        }
        
        // 映射前端字段名到实体字段名
        sortField = mapVocabSortField(sortField);
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        
        Page<AvVocab> vocabPage = avVocabService.getAllVocabs(pageable);
        return ResponseEntity.ok(vocabPage);
    }
    
    /**
     * 映射前端排序字段名到实体字段名（词汇）
     */
    private String mapVocabSortField(String frontendField) {
        switch (frontendField.toLowerCase()) {
            case "word":
            case "headword":
                return "headword";
            case "definition":
                return "definitionZh";
            case "pos":
                return "pos";
            case "cefrlevel":
            case "cefr_level":
                return "cefrLevel";
            case "difficulty":
            case "difficultylevel":
                return "difficultyLevel";
            case "frequency":
            case "frequencylevel":
                return "frequencyLevel";
            case "createdat":
            case "created_at":
                return "createdAt";
            case "updatedat":
            case "updated_at":
                return "updatedAt";
            default:
                return "id"; // 默认排序字段
        }
    }

    /**
     * 更新词汇
     */
    @PutMapping("/{id}")
    @CacheEvict(value = {"avVocabs", "avVocabStats"}, allEntries = true)
    public ResponseEntity<AvVocab> updateVocab(
            @PathVariable Long id,
            @Valid @RequestBody AvVocab vocab) {
        log.info("更新词汇: ID={}, word={}", id, vocab.getHeadword());
        
        try {
            if (!avVocabService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            vocab.setId(id);
            AvVocab updatedVocab = avVocabService.updateVocab(vocab);
            return ResponseEntity.ok(updatedVocab);
        } catch (DataIntegrityViolationException e) {
            log.error("更新词汇失败，数据完整性约束违反: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("更新词汇失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除词汇
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"avVocabs", "avVocabStats", "avVocabTopicMaps"}, allEntries = true)
    public ResponseEntity<Void> deleteVocab(@PathVariable Long id) {
        log.info("删除词汇: ID={}", id);
        
        try {
            if (!avVocabService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            // 先删除相关的主题映射
            avVocabTopicMapService.deleteMappingsByVocabId(id);
            
            // 删除词汇
            avVocabService.deleteVocab(id);
            
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            log.error("删除词汇失败，存在关联数据: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("删除词汇失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== 词汇扩展功能 ====================

    /**
     * 根据单词查找词汇
     */
    @GetMapping("/by-word/{word}")
    @Cacheable(value = "avVocabs", key = "'word_' + #word")
    public ResponseEntity<List<AvVocab>> getVocabsByWord(@PathVariable String word) {
        log.info("根据单词查找词汇: {}", word);
        
        List<AvVocab> vocabs = avVocabService.getVocabsByWord(word);
        return ResponseEntity.ok(vocabs);
    }

    /**
     * 模糊搜索词汇
     */
    @GetMapping("/search")
    @Cacheable(value = "avVocabs", key = "'search_' + #keyword + '_' + #page + '_' + #size")
    public ResponseEntity<Page<AvVocab>> searchVocabs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("模糊搜索词汇: keyword={}", keyword);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "word"));
        Page<AvVocab> vocabPage = avVocabService.searchVocabsByHeadword(keyword, pageable);
        return ResponseEntity.ok(vocabPage);
    }

    /**
     * 综合搜索词汇
     */
    @GetMapping("/comprehensive-search")
    public ResponseEntity<Page<AvVocab>> comprehensiveSearchVocabs(
            @RequestParam(required = false) String word,
            @RequestParam(required = false) String definition,
            @RequestParam(required = false) String example,
            @RequestParam(required = false) String pos,
            @RequestParam(required = false) String cefrLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("综合搜索词汇: word={}, definition={}, example={}, pos={}, cefrLevel={}", 
                 word, definition, example, pos, cefrLevel);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "word"));
        Page<AvVocab> vocabPage = avVocabService.comprehensiveSearch(word, definition, example, pos, cefrLevel, pageable);
        return ResponseEntity.ok(vocabPage);
    }

    /**
     * 批量创建词汇
     */
    @PostMapping("/batch")
    @CacheEvict(value = {"avVocabs", "avVocabStats"}, allEntries = true)
    public ResponseEntity<List<AvVocab>> createVocabsBatch(@Valid @RequestBody List<AvVocab> vocabs) {
        log.info("批量创建词汇: count={}", vocabs.size());
        
        try {
            vocabs.forEach(vocab -> vocab.setCreatedAt(LocalDateTime.now()));
            List<AvVocab> savedVocabs = avVocabService.createVocabsBatch(vocabs);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVocabs);
        } catch (Exception e) {
            log.error("批量创建词汇失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量删除词汇
     */
    @DeleteMapping("/batch")
    @CacheEvict(value = {"avVocabs", "avVocabStats", "avVocabTopicMaps"}, allEntries = true)
    public ResponseEntity<Void> deleteVocabsBatch(@RequestBody List<Long> ids) {
        log.info("批量删除词汇: count={}", ids.size());
        
        try {
            // 先删除相关的主题映射
            avVocabTopicMapService.deleteMappingsByVocabIds(ids);
            
            // 批量删除词汇
            avVocabService.deleteVocabs(ids);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("批量删除词汇失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取词汇统计信息
     */
    @GetMapping("/statistics")
    @Cacheable(value = "avVocabStats", key = "'general'")
    public ResponseEntity<Map<String, Object>> getVocabStatistics() {
        log.info("获取词汇统计信息");
        
        Map<String, Object> stats = avVocabService.getVocabStatistics();
        return ResponseEntity.ok(stats);
    }

    // ==================== 词汇主题（AvVocabTopic）CRUD操作 ====================

    /**
     * 创建词汇主题
     */
    @PostMapping("/topics")
    @CacheEvict(value = {"avVocabTopics", "avVocabTopicStats"}, allEntries = true)
    public ResponseEntity<AvVocabTopic> createTopic(@Valid @RequestBody AvVocabTopic topic) {
        log.info("创建词汇主题: {}", topic.getNameZh());
        
        try {
            topic.setCreatedAt(LocalDateTime.now());
            AvVocabTopic savedTopic = avVocabTopicService.createTopic(topic);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTopic);
        } catch (DataIntegrityViolationException e) {
            log.error("创建词汇主题失败，数据完整性约束违反: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("创建词汇主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据ID获取词汇主题详情
     */
    @GetMapping("/topics/{id}")
    @Cacheable(value = "avVocabTopics", key = "#id")
    public ResponseEntity<AvVocabTopic> getTopicById(@PathVariable Long id) {
        log.info("获取词汇主题详情: ID={}", id);
        
        Optional<AvVocabTopic> topicOpt = avVocabTopicService.getTopicById(id);
        return topicOpt.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 分页查询词汇主题
     */
    @GetMapping("/topics")
    @Cacheable(value = "avVocabTopics", key = "'page_' + #page + '_' + #size + '_' + #sort + '_' + #direction")
    public ResponseEntity<Page<AvVocabTopic>> getTopics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("分页查询词汇主题: page={}, size={}, sort={}, direction={}", page, size, sort, direction);
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<AvVocabTopic> topicPage = avVocabTopicService.getAllTopics(pageable);
        return ResponseEntity.ok(topicPage);
    }

    /**
     * 更新词汇主题
     */
    @PutMapping("/topics/{id}")
    @CacheEvict(value = {"avVocabTopics", "avVocabTopicStats"}, allEntries = true)
    public ResponseEntity<AvVocabTopic> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody AvVocabTopic topic) {
        log.info("更新词汇主题: ID={}, name={}", id, topic.getNameZh());
        
        try {
            if (!avVocabTopicService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            topic.setId(id);
            AvVocabTopic updatedTopic = avVocabTopicService.updateTopic(topic);
            return ResponseEntity.ok(updatedTopic);
        } catch (DataIntegrityViolationException e) {
            log.error("更新词汇主题失败，数据完整性约束违反: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("更新词汇主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除词汇主题
     */
    @DeleteMapping("/topics/{id}")
    @CacheEvict(value = {"avVocabTopics", "avVocabTopicStats", "avVocabTopicMaps"}, allEntries = true)
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        log.info("删除词汇主题: ID={}", id);
        
        try {
            if (!avVocabTopicService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            // 先删除相关的主题映射
            avVocabTopicMapService.deleteMappingsByTopicId(id);
            
            // 删除主题
            avVocabTopicService.deleteTopic(id);
            
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            log.error("删除词汇主题失败，存在关联数据: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("删除词汇主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== 词汇主题扩展功能 ====================

    /**
     * 根据代码查找主题
     */
    @GetMapping("/topics/by-code/{code}")
    @Cacheable(value = "avVocabTopics", key = "'code_' + #code")
    public ResponseEntity<AvVocabTopic> getTopicByCode(@PathVariable String code) {
        log.info("根据代码查找主题: {}", code);
        
        Optional<AvVocabTopic> topicOpt = avVocabTopicService.getTopicByCode(code);
        return topicOpt.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据父主题ID获取子主题
     */
    @GetMapping("/topics/by-parent/{parentId}")
    @Cacheable(value = "avVocabTopics", key = "'children_' + #parentId")
    public ResponseEntity<List<AvVocabTopic>> getTopicsByParentId(@PathVariable Long parentId) {
        log.info("根据父主题ID获取子主题: {}", parentId);
        
        List<AvVocabTopic> topics = avVocabTopicService.getTopicsByParentId(parentId);
        return ResponseEntity.ok(topics);
    }

    /**
     * 获取根主题列表
     */
    @GetMapping("/topics/roots")
    @Cacheable(value = "avVocabTopics", key = "'roots'")
    public ResponseEntity<List<AvVocabTopic>> getRootTopics() {
        log.info("获取根主题列表");
        
        List<AvVocabTopic> topics = avVocabTopicService.getRootTopics();
        return ResponseEntity.ok(topics);
    }

    /**
     * 获取主题层级结构
     */
    @GetMapping("/topics/hierarchy")
    @Cacheable(value = "avVocabTopics", key = "'hierarchy'")
    public ResponseEntity<List<Map<String, Object>>> getTopicHierarchy() {
        log.info("获取主题层级结构");
        
        List<Map<String, Object>> hierarchy = avVocabTopicService.getTopicHierarchy();
        return ResponseEntity.ok(hierarchy);
    }

    /**
     * 搜索主题
     */
    @GetMapping("/topics/search")
    @Cacheable(value = "avVocabTopics", key = "'search_' + #keyword + '_' + #page + '_' + #size")
    public ResponseEntity<Page<AvVocabTopic>> searchTopics(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("搜索主题: keyword={}", keyword);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        Page<AvVocabTopic> topicPage = avVocabTopicService.searchTopics(keyword, pageable);
        return ResponseEntity.ok(topicPage);
    }

    /**
     * 批量创建主题
     */
    @PostMapping("/topics/batch")
    @CacheEvict(value = {"avVocabTopics", "avVocabTopicStats"}, allEntries = true)
    public ResponseEntity<List<AvVocabTopic>> createTopicsBatch(@Valid @RequestBody List<AvVocabTopic> topics) {
        log.info("批量创建主题: count={}", topics.size());
        
        try {
            topics.forEach(topic -> topic.setCreatedAt(LocalDateTime.now()));
            List<AvVocabTopic> savedTopics = avVocabTopicService.createTopicsBatch(topics);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTopics);
        } catch (Exception e) {
            log.error("批量创建主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量删除主题
     */
    @DeleteMapping("/topics/batch")
    @CacheEvict(value = {"avVocabTopics", "avVocabTopicStats", "avVocabTopicMaps"}, allEntries = true)
    public ResponseEntity<Void> deleteTopicsBatch(@RequestBody List<Long> ids) {
        log.info("批量删除主题: count={}", ids.size());
        
        try {
            // 先删除相关的主题映射
            avVocabTopicMapService.deleteMappingsByTopicIds(ids);
            
            // 批量删除主题
            avVocabTopicService.deleteTopicsBatch(ids);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("批量删除主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取主题统计信息
     */
    @GetMapping("/topics/statistics")
    @Cacheable(value = "avVocabTopicStats", key = "'general'")
    public ResponseEntity<Map<String, Object>> getTopicStatistics() {
        log.info("获取主题统计信息");
        
        Map<String, Object> stats = avVocabTopicService.getTopicStatistics();
        return ResponseEntity.ok(stats);
    }

    // ==================== 词汇主题映射（AvVocabTopicMap）CRUD操作 ====================

    /**
     * 创建词汇主题映射
     */
    @PostMapping("/mappings")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<AvVocabTopicMap> createMapping(@Valid @RequestBody AvVocabTopicMap mapping) {
        log.info("创建词汇主题映射: vocabId={}, topicId={}", mapping.getVocabId(), mapping.getTopicId());
        
        try {
            AvVocabTopicMap savedMapping = avVocabTopicMapService.createMapping(mapping);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMapping);
        } catch (IllegalArgumentException e) {
            log.error("创建映射失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("创建映射失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据ID获取映射详情
     */
    @GetMapping("/mappings/{id}")
    @Cacheable(value = "avVocabTopicMaps", key = "#id")
    public ResponseEntity<AvVocabTopicMap> getMappingById(@PathVariable Long id) {
        log.info("获取映射详情: ID={}", id);
        
        Optional<AvVocabTopicMap> mappingOpt = avVocabTopicMapService.getMappingById(id);
        return mappingOpt.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 分页查询映射
     */
    @GetMapping("/mappings")
    @Cacheable(value = "avVocabTopicMaps", key = "'page_' + #page + '_' + #size + '_' + #sort + '_' + #direction")
    public ResponseEntity<Page<AvVocabTopicMap>> getMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("分页查询映射: page={}, size={}, sort={}, direction={}", page, size, sort, direction);
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<AvVocabTopicMap> mappingPage = avVocabTopicMapService.getAllMappings(pageable);
        return ResponseEntity.ok(mappingPage);
    }

    /**
     * 更新映射
     */
    @PutMapping("/mappings/{id}")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<AvVocabTopicMap> updateMapping(
            @PathVariable Long id,
            @Valid @RequestBody AvVocabTopicMap mapping) {
        log.info("更新映射: ID={}", id);
        
        try {
            if (!avVocabTopicMapService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            mapping.setId(id);
            AvVocabTopicMap updatedMapping = avVocabTopicMapService.updateMapping(mapping);
            return ResponseEntity.ok(updatedMapping);
        } catch (IllegalArgumentException e) {
            log.error("更新映射失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("更新映射失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除映射
     */
    @DeleteMapping("/mappings/{id}")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<Void> deleteMapping(@PathVariable Long id) {
        log.info("删除映射: ID={}", id);
        
        try {
            if (!avVocabTopicMapService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            avVocabTopicMapService.deleteMapping(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("删除映射失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== 词汇主题映射扩展功能 ====================

    /**
     * 根据词汇ID获取映射
     */
    @GetMapping("/mappings/by-vocab/{vocabId}")
    @Cacheable(value = "avVocabTopicMaps", key = "'vocab_' + #vocabId")
    public ResponseEntity<List<AvVocabTopicMap>> getMappingsByVocabId(@PathVariable Long vocabId) {
        log.info("根据词汇ID获取映射: {}", vocabId);
        
        List<AvVocabTopicMap> mappings = avVocabTopicMapService.getMappingsByVocabId(vocabId);
        return ResponseEntity.ok(mappings);
    }

    /**
     * 根据主题ID获取映射
     */
    @GetMapping("/mappings/by-topic/{topicId}")
    @Cacheable(value = "avVocabTopicMaps", key = "'topic_' + #topicId")
    public ResponseEntity<List<AvVocabTopicMap>> getMappingsByTopicId(@PathVariable Long topicId) {
        log.info("根据主题ID获取映射: {}", topicId);
        
        List<AvVocabTopicMap> mappings = avVocabTopicMapService.getMappingsByTopicId(topicId);
        return ResponseEntity.ok(mappings);
    }

    /**
     * 获取词汇的主归属主题
     */
    @GetMapping("/mappings/primary-topic/{vocabId}")
    @Cacheable(value = "avVocabTopicMaps", key = "'primary_' + #vocabId")
    public ResponseEntity<AvVocabTopicMap> getPrimaryTopicByVocabId(@PathVariable Long vocabId) {
        log.info("获取词汇的主归属主题: {}", vocabId);
        
        Optional<AvVocabTopicMap> mappingOpt = avVocabTopicMapService.getPrimaryMappingByVocabId(vocabId);
        return mappingOpt.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 设置词汇的主归属主题
     */
    @PostMapping("/mappings/set-primary/{vocabId}/{topicId}")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<Void> setPrimaryTopic(@PathVariable Long vocabId, @PathVariable Long topicId) {
        log.info("设置词汇的主归属主题: vocabId={}, topicId={}", vocabId, topicId);
        
        try {
            avVocabTopicMapService.setPrimaryTopic(vocabId, topicId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("设置主归属主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("设置主归属主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量添加词汇到主题
     */
    @PostMapping("/mappings/add-vocabs-to-topic/{topicId}")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<List<AvVocabTopicMap>> addVocabsToTopic(
            @PathVariable Long topicId,
            @RequestBody List<Long> vocabIds,
            @RequestParam(defaultValue = "false") boolean isPrimary) {
        log.info("批量添加词汇到主题: topicId={}, vocabCount={}, isPrimary={}", topicId, vocabIds.size(), isPrimary);
        
        try {
            List<AvVocabTopicMap> mappings = avVocabTopicMapService.addVocabsToTopic(topicId, vocabIds, isPrimary);
            return ResponseEntity.ok(mappings);
        } catch (IllegalArgumentException e) {
            log.error("批量添加词汇到主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("批量添加词汇到主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量从主题移除词汇
     */
    @DeleteMapping("/mappings/remove-vocabs-from-topic/{topicId}")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<Void> removeVocabsFromTopic(
            @PathVariable Long topicId,
            @RequestBody List<Long> vocabIds) {
        log.info("批量从主题移除词汇: topicId={}, vocabCount={}", topicId, vocabIds.size());
        
        try {
            avVocabTopicMapService.removeVocabsFromTopic(topicId, vocabIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("批量从主题移除词汇失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量添加主题到词汇
     */
    @PostMapping("/mappings/add-topics-to-vocab/{vocabId}")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<List<AvVocabTopicMap>> addTopicsToVocab(
            @PathVariable Long vocabId,
            @RequestBody List<Long> topicIds,
            @RequestParam(required = false) Long primaryTopicId) {
        log.info("批量添加主题到词汇: vocabId={}, topicCount={}, primaryTopicId={}", 
                 vocabId, topicIds.size(), primaryTopicId);
        
        try {
            List<AvVocabTopicMap> mappings = avVocabTopicMapService.addTopicsToVocab(vocabId, topicIds, primaryTopicId);
            return ResponseEntity.ok(mappings);
        } catch (IllegalArgumentException e) {
            log.error("批量添加主题到词汇失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("批量添加主题到词汇失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量从词汇移除主题
     */
    @DeleteMapping("/mappings/remove-topics-from-vocab/{vocabId}")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<Void> removeTopicsFromVocab(
            @PathVariable Long vocabId,
            @RequestBody List<Long> topicIds) {
        log.info("批量从词汇移除主题: vocabId={}, topicCount={}", vocabId, topicIds.size());
        
        try {
            avVocabTopicMapService.removeTopicsFromVocab(vocabId, topicIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("批量从词汇移除主题失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取映射统计信息
     */
    @GetMapping("/mappings/statistics")
    @Cacheable(value = "avVocabMappingStats", key = "'general'")
    public ResponseEntity<Map<String, Object>> getMappingStatistics() {
        log.info("获取映射统计信息");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMappingCount", avVocabTopicMapService.getTotalMappingCount());
        stats.put("primaryMappingCount", avVocabTopicMapService.getPrimaryMappingCount());
        stats.put("secondaryMappingCount", avVocabTopicMapService.getSecondaryMappingCount());
        stats.put("vocabsWithoutPrimaryCount", avVocabTopicMapService.getVocabsWithoutPrimaryTopic().size());
        stats.put("vocabsWithoutAnyTopicCount", avVocabTopicMapService.getVocabsWithoutAnyTopic().size());
        stats.put("topicsWithoutVocabsCount", avVocabTopicMapService.getTopicsWithoutVocabs().size());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 检查数据完整性
     */
    @GetMapping("/mappings/integrity-check")
    public ResponseEntity<Map<String, Object>> checkDataIntegrity() {
        log.info("检查数据完整性");
        
        Map<String, Object> result = avVocabTopicMapService.checkDataIntegrity();
        return ResponseEntity.ok(result);
    }

    /**
     * 修复数据完整性
     */
    @PostMapping("/mappings/repair-integrity")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> repairDataIntegrity() {
        log.info("修复数据完整性");
        
        try {
            Map<String, Object> result = avVocabTopicMapService.repairDataIntegrity();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("修复数据完整性失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== 缓存管理操作 ====================

    /**
     * 清除所有缓存
     */
    @PostMapping("/cache/clear-all")
    @CacheEvict(value = {"avVocabs", "avVocabTopics", "avVocabTopicMaps", "avVocabStats", "avVocabTopicStats", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<Void> clearAllCache() {
        log.info("清除所有缓存");
        
        avVocabService.evictAllVocabCache();
        avVocabTopicService.evictAllTopicCache();
        avVocabTopicMapService.evictAllMappingCache();
        
        return ResponseEntity.ok().build();
    }

    /**
     * 清除词汇缓存
     */
    @PostMapping("/cache/clear-vocabs")
    @CacheEvict(value = {"avVocabs", "avVocabStats"}, allEntries = true)
    public ResponseEntity<Void> clearVocabCache() {
        log.info("清除词汇缓存");
        
        avVocabService.evictAllVocabCache();
        return ResponseEntity.ok().build();
    }

    /**
     * 清除主题缓存
     */
    @PostMapping("/cache/clear-topics")
    @CacheEvict(value = {"avVocabTopics", "avVocabTopicStats"}, allEntries = true)
    public ResponseEntity<Void> clearTopicCache() {
        log.info("清除主题缓存");
        
        avVocabTopicService.evictAllTopicCache();
        return ResponseEntity.ok().build();
    }

    /**
     * 清除映射缓存
     */
    @PostMapping("/cache/clear-mappings")
    @CacheEvict(value = {"avVocabTopicMaps", "avVocabMappingStats"}, allEntries = true)
    public ResponseEntity<Void> clearMappingCache() {
        log.info("清除映射缓存");
        
        avVocabTopicMapService.evictAllMappingCache();
        return ResponseEntity.ok().build();
    }

    // ==================== Additional API Endpoints to match vocab.js ====================



    // ==================== Utility Methods API ====================

    /**
     * 获取CEFR等级选项
     */
    @GetMapping("/cefr-levels")
    public ResponseEntity<List<String>> getCEFRLevels() {
        List<String> levels = List.of("A1", "A2", "B1", "B2", "C1", "C2");
        return ResponseEntity.ok(levels);
    }

    /**
     * 获取词性选项
     */
    @GetMapping("/pos-options")
    public ResponseEntity<List<String>> getPOSOptions() {
        List<String> posOptions = List.of(
            "noun", "verb", "adjective", "adverb", 
            "pronoun", "preposition", "conjunction", 
            "interjection", "article", "phrase"
        );
        return ResponseEntity.ok(posOptions);
    }

    /**
     * 获取频率等级选项
     */
    @GetMapping("/frequency-levels")
    public ResponseEntity<List<Map<String, Object>>> getFrequencyLevels() {
        List<Map<String, Object>> levels = new ArrayList<>();
        
        Map<String, Object> high = new HashMap<>();
        high.put("value", 1);
        high.put("label", "高频");
        levels.add(high);
        
        Map<String, Object> medium = new HashMap<>();
        medium.put("value", 2);
        medium.put("label", "中频");
        levels.add(medium);
        
        Map<String, Object> low = new HashMap<>();
        low.put("value", 3);
        low.put("label", "低频");
        levels.add(low);
        
        return ResponseEntity.ok(levels);
    }

    /**
     * 获取难度等级选项
     */
    @GetMapping("/difficulty-levels")
    public ResponseEntity<List<Map<String, Object>>> getDifficultyLevels() {
        List<Map<String, Object>> levels = new ArrayList<>();
        
        String[] labels = {"非常简单", "简单", "中等", "困难", "非常困难"};
        for (int i = 0; i < labels.length; i++) {
            Map<String, Object> level = new HashMap<>();
            level.put("value", i + 1);
            level.put("label", labels[i]);
            levels.add(level);
        }
        
        return ResponseEntity.ok(levels);
    }
}