package org.icao4.eqasbackend2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.dto.request.*;
import org.icao4.eqasbackend2.dto.response.AvTermResponse;
import org.icao4.eqasbackend2.dto.response.AvTermSimpleResponse;
import org.icao4.eqasbackend2.dto.response.AvTermTopicMapResponse;
import org.icao4.eqasbackend2.entity.term.AvTerm;
import org.icao4.eqasbackend2.entity.term.AvTermTopicMap;
import org.icao4.eqasbackend2.entity.term.AvTermsTopic;
import org.icao4.eqasbackend2.repository.term.AvTermsTopicRepository;
import org.icao4.eqasbackend2.service.term.AvTermService;
import org.icao4.eqasbackend2.service.term.AvTermTopicMapService;
import org.icao4.eqasbackend2.service.term.AvTermsTopicService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 航空术语控制器
 */
@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
@Slf4j
public class AvTermController {

    private final AvTermService avTermService;
    private final AvTermTopicMapService avTermTopicMapService;
    private final AvTermsTopicService avTermsTopicService;
    private final AvTermsTopicRepository avTermsTopicRepository;

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建航空术语
     */
    @PostMapping
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<AvTermResponse> createAvTerm(@Valid @RequestBody AvTermCreateRequest request) {
        log.info("创建航空术语: {}", request.getHeadword());
        
        AvTerm avTerm = AvTerm.builder()
                .headword(request.getHeadword())
                .lemma(request.getLemma())
                .pos(request.getPos())
                .ipa(request.getIpa())
                .definitionEn(request.getDefinitionEn())
                .definitionZh(request.getDefinitionZh())
                .exampleEn(request.getExampleEn())
                .exampleZh(request.getExampleZh())
                .cefrLevel(request.getCefrLevel())
                .freqRank(request.getFreqRank())
                .source(request.getSource())
                .audioAssetId(request.getAudioAssetId())
                .extraJson(request.getExtraJson())
                .createdAt(LocalDateTime.now())
                .build();
        
        AvTerm savedTerm = avTermService.save(avTerm);
        return ResponseEntity.ok(convertToResponse(savedTerm, false));
    }

    /**
     * 根据ID获取航空术语详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AvTermResponse> getAvTermById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") Boolean includeTopics) {
        log.info("获取航空术语详情: ID={}, includeTopics={}", id, includeTopics);
        
        Optional<AvTerm> avTermOpt = avTermService.findById(id);
        if (avTermOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(convertToResponse(avTermOpt.get(), includeTopics));
    }

    /**
     * 分页查询航空术语
     */
    @GetMapping
    public ResponseEntity<Page<AvTermSimpleResponse>> getAvTerms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            AvTermQueryRequest request) {
        log.info("分页查询航空术语: page={}, size={}, sort={}, direction={}", page, size, sort, direction);
        
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
        sortField = mapSortField(sortField);
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        
        Page<AvTerm> avTermPage = avTermService.findByQuery(request, pageable);
        Page<AvTermSimpleResponse> responsePage = avTermPage.map(this::convertToSimpleResponse);
        
        return ResponseEntity.ok(responsePage);
    }
    
    /**
     * 映射前端排序字段名到实体字段名
     */
    private String mapSortField(String frontendField) {
        switch (frontendField.toLowerCase()) {
            case "termtext":
            case "term_text":
                return "headword";
            case "definition":
                return "definitionZh";
            case "pos":
                return "pos";
            case "cefrlevel":
            case "cefr_level":
                return "cefrLevel";
            case "freqrank":
            case "freq_rank":
                return "freqRank";
            case "source":
                return "source";
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
     * 更新航空术语
     */
    @PutMapping("/{id}")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<AvTermResponse> updateAvTerm(
            @PathVariable Long id,
            @Valid @RequestBody AvTermUpdateRequest request) {
        log.info("更新航空术语: ID={}, headword={}", id, request.getHeadword());
        
        Optional<AvTerm> avTermOpt = avTermService.findById(id);
        if (avTermOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        AvTerm avTerm = avTermOpt.get();
        updateAvTermFromRequest(avTerm, request);
        
        AvTerm updatedTerm = avTermService.save(avTerm);
        return ResponseEntity.ok(convertToResponse(updatedTerm, false));
    }

    /**
     * 删除航空术语
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<Void> deleteAvTerm(@PathVariable Long id) {
        log.info("删除航空术语: ID={}", id);
        
        if (!avTermService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // 先删除相关的主题映射
        avTermTopicMapService.deleteByTermId(id);
        
        // 删除术语
        avTermService.deleteById(id);
        
        return ResponseEntity.noContent().build();
    }

    // ==================== 扩展功能 ====================

    /**
     * 根据词条获取术语
     */
    @GetMapping("/by-headword/{headword}")
    @Cacheable(value = "avTerms", key = "'headword_' + #headword")
    public ResponseEntity<List<AvTermSimpleResponse>> getAvTermsByHeadword(@PathVariable String headword) {
        log.info("根据词条获取术语: {}", headword);
        
        List<AvTerm> avTerms = avTermService.findByHeadword(headword);
        List<AvTermSimpleResponse> responses = avTerms.stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据CEFR等级获取术语列表
     */
    @GetMapping("/by-cefr-level/{cefrLevel}")
    @Cacheable(value = "avTerms", key = "'cefr_' + #cefrLevel + '_' + #page + '_' + #size")
    public ResponseEntity<Page<AvTermSimpleResponse>> getAvTermsByCefrLevel(
            @PathVariable AvTerm.CefrLevel cefrLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("根据CEFR等级获取术语: {}", cefrLevel);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "freqRank"));
        Page<AvTerm> avTermPage = avTermService.findByCefrLevel(cefrLevel, pageable);
        Page<AvTermSimpleResponse> responsePage = avTermPage.map(this::convertToSimpleResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    /**
     * 根据频次排名范围获取术语列表
     */
    @GetMapping("/by-freq-rank")
    @Cacheable(value = "avTerms", key = "'freq_' + #minRank + '_' + #maxRank + '_' + #page + '_' + #size")
    public ResponseEntity<Page<AvTermSimpleResponse>> getAvTermsByFreqRank(
            @RequestParam Integer minRank,
            @RequestParam Integer maxRank,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("根据频次排名范围获取术语: {}-{}", minRank, maxRank);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "freqRank"));
        Page<AvTerm> avTermPage = avTermService.findByFreqRankBetween(minRank, maxRank, pageable);
        Page<AvTermSimpleResponse> responsePage = avTermPage.map(this::convertToSimpleResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    /**
     * 检查词条是否可用
     */
    @GetMapping("/check-headword/{headword}")
    public ResponseEntity<Map<String, Object>> checkHeadwordAvailability(@PathVariable String headword) {
        log.info("检查词条可用性: {}", headword);
        
        boolean exists = avTermService.existsByHeadword(headword);
        Map<String, Object> result = new HashMap<>();
        result.put("headword", headword);
        result.put("available", !exists);
        result.put("exists", exists);
        
        if (exists) {
            List<AvTerm> existingTerms = avTermService.findByHeadword(headword);
            result.put("existingTerms", existingTerms.stream()
                    .map(this::convertToSimpleResponse)
                    .collect(Collectors.toList()));
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 复制术语
     */
    @PostMapping("/{id}/copy")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<AvTermResponse> copyAvTerm(@PathVariable Long id) {
        log.info("复制术语: ID={}", id);
        
        Optional<AvTerm> avTermOpt = avTermService.findById(id);
        if (avTermOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        AvTerm originalTerm = avTermOpt.get();
        AvTerm copiedTerm = AvTerm.builder()
                .headword(originalTerm.getHeadword() + "_copy")
                .lemma(originalTerm.getLemma())
                .pos(originalTerm.getPos())
                .ipa(originalTerm.getIpa())
                .definitionEn(originalTerm.getDefinitionEn())
                .definitionZh(originalTerm.getDefinitionZh())
                .exampleEn(originalTerm.getExampleEn())
                .exampleZh(originalTerm.getExampleZh())
                .cefrLevel(originalTerm.getCefrLevel())
                .freqRank(originalTerm.getFreqRank())
                .source(originalTerm.getSource())
                .audioAssetId(originalTerm.getAudioAssetId())
                .extraJson(originalTerm.getExtraJson())
                .createdAt(LocalDateTime.now())
                .build();
        
        AvTerm savedTerm = avTermService.save(copiedTerm);
        return ResponseEntity.ok(convertToResponse(savedTerm, false));
    }

    // ==================== 主题映射管理功能 ====================

    /**
     * 为术语添加主题映射
     */
    @PostMapping("/{termId}/topics")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<AvTermTopicMapResponse> addTopicMapping(
            @PathVariable Long termId,
            @Valid @RequestBody AvTermTopicMapRequest request) {
        log.info("为术语添加主题映射: termId={}, topicId={}, isPrimary={}", 
                termId, request.getTopicId(), request.getIsPrimary());
        
        // 检查术语是否存在
        if (!avTermService.existsById(termId)) {
            return ResponseEntity.notFound().build();
        }
        
        // 检查主题是否存在
        if (!avTermsTopicService.existsById(request.getTopicId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // 检查映射是否已存在
        if (avTermTopicMapService.existsByTermIdAndTopicId(termId, request.getTopicId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // 如果是主归属，需要先取消其他主归属
        if (request.getIsPrimary()) {
            avTermTopicMapService.clearPrimaryMappingForTerm(termId);
        }
        
        AvTermTopicMap topicMap = AvTermTopicMap.builder()
                .termId(termId)
                .topicId(request.getTopicId())
                .isPrimary(request.getIsPrimary())
                .build();
        
        AvTermTopicMap savedMapping = avTermTopicMapService.save(topicMap);
        return ResponseEntity.ok(convertToTopicMapResponse(savedMapping));
    }

    /**
     * 获取术语的主题映射列表
     */
    @GetMapping("/{termId}/topics")
    @Cacheable(value = "avTerms", key = "'topics_' + #termId")
    public ResponseEntity<List<AvTermTopicMapResponse>> getTermTopicMappings(@PathVariable Long termId) {
        log.info("获取术语的主题映射列表: termId={}", termId);
        
        if (!avTermService.existsById(termId)) {
            return ResponseEntity.notFound().build();
        }
        
        List<AvTermTopicMap> topicMaps = avTermTopicMapService.findByTermId(termId);
        List<AvTermTopicMapResponse> responses = topicMaps.stream()
                .map(this::convertToTopicMapResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * 更新术语的主题映射
     */
    @PutMapping("/{termId}/topics/{topicId}")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<AvTermTopicMapResponse> updateTopicMapping(
            @PathVariable Long termId,
            @PathVariable Long topicId,
            @Valid @RequestBody AvTermTopicMapRequest request) {
        log.info("更新术语的主题映射: termId={}, topicId={}, isPrimary={}", 
                termId, topicId, request.getIsPrimary());
        
        Optional<AvTermTopicMap> mappingOpt = avTermTopicMapService.findByTermIdAndTopicId(termId, topicId);
        if (mappingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        AvTermTopicMap mapping = mappingOpt.get();
        
        // 如果要设置为主归属，需要先取消其他主归属
        if (request.getIsPrimary() && !mapping.getIsPrimary()) {
            avTermTopicMapService.clearPrimaryMappingForTerm(termId);
        }
        
        mapping.setIsPrimary(request.getIsPrimary());
        AvTermTopicMap updatedMapping = avTermTopicMapService.save(mapping);
        
        return ResponseEntity.ok(convertToTopicMapResponse(updatedMapping));
    }

    /**
     * 删除术语的主题映射
     */
    @DeleteMapping("/{termId}/topics/{topicId}")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<Void> removeTopicMapping(
            @PathVariable Long termId,
            @PathVariable Long topicId) {
        log.info("删除术语的主题映射: termId={}, topicId={}", termId, topicId);
        
        Optional<AvTermTopicMap> mappingOpt = avTermTopicMapService.findByTermIdAndTopicId(termId, topicId);
        if (mappingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        avTermTopicMapService.deleteByTermIdAndTopicId(termId, topicId);
        return ResponseEntity.noContent().build();
    }

    // ==================== 主题（AvTermsTopic）CRUD ====================

    /**
     * 新建主题
     */
    @PostMapping("/topics")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<AvTermsTopic> createTopic(@Valid @RequestBody AvTermsTopic request) {
        log.info("创建主题: code={}, zh={}, en={}", request.getCode(), request.getNameZh(), request.getNameEn());
        if (request.getId() != null && avTermsTopicService.existsById(request.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Optional<AvTermsTopic> saved = avTermsTopicService.save(request);
        return saved
                .map(t -> ResponseEntity.status(201).body(t))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * 根据ID获取主题详情
     */
    @GetMapping("/topics/{id}")
    @Cacheable(value = "avTerms", key = "'topic_' + #id")
    public ResponseEntity<AvTermsTopic> getTopicById(@PathVariable Long id) {
        log.info("获取主题详情: id={}", id);
        return avTermsTopicService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 分页获取主题列表
     */
    @GetMapping("/topics")
    @Cacheable(value = "avTerms", key = "'topics_' + #page + '_' + #size + '_' + #sort + '_' + #direction")
    public ResponseEntity<Page<AvTermsTopic>> getTopics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nameZh") String sort,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        log.info("分页获取主题列表: page={}, size={}, sort={} {},", page, size, sort, direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<AvTermsTopic> topics = avTermsTopicRepository.findAll(pageable);
        return ResponseEntity.ok(topics);
    }

    /**
     * 更新主题
     */
    @PutMapping("/topics/{id}")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<AvTermsTopic> updateTopic(@PathVariable Long id, @Valid @RequestBody AvTermsTopic request) {
        log.info("更新主题: id={}, code={}, zh={}, en={}", id, request.getCode(), request.getNameZh(), request.getNameEn());
        Optional<AvTermsTopic> existingOpt = avTermsTopicService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AvTermsTopic existing = existingOpt.get();
        // 按字段覆盖更新
        existing.setParentId(request.getParentId());
        existing.setCode(request.getCode());
        existing.setNameZh(request.getNameZh());
        existing.setNameEn(request.getNameEn());
        existing.setDescription(request.getDescription());

        Optional<AvTermsTopic> saved = avTermsTopicService.save(existing);
        return saved
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * 删除主题
     */
    @DeleteMapping("/topics/{id}")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        log.info("删除主题: id={}", id);
        Optional<AvTermsTopic> existingOpt = avTermsTopicService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            avTermsTopicService.delete(existingOpt.get());
        } catch (DataIntegrityViolationException ex) {
            log.warn("删除主题失败，存在关联映射或外键约束: id={}", id, ex);
            return ResponseEntity.status(409).build();
        }
        return ResponseEntity.noContent().build();
    }

    // ==================== 高级查询、统计和批量操作功能 ====================
    /**
     * 根据主题ID获取术语列表
     */
    @GetMapping("/by-topic/{topicId}")
    @Cacheable(value = "avTerms", key = "'topic_' + #topicId + '_' + #primaryOnly + '_' + #page + '_' + #size")
    public ResponseEntity<Page<AvTermSimpleResponse>> getTermsByTopicId(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "false") Boolean primaryOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("根据主题ID获取术语列表: topicId={}, primaryOnly={}", topicId, primaryOnly);
        
        if (!avTermsTopicService.existsById(topicId)) {
            return ResponseEntity.notFound().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "freqRank"));
        Page<AvTerm> avTermPage = avTermService.findByTopicId(topicId, primaryOnly, pageable);
        Page<AvTermSimpleResponse> responsePage = avTermPage.map(this::convertToSimpleResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    /**
     * 设置术语的主归属主题
     */
    @PutMapping("/{termId}/primary-topic/{topicId}")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<AvTermTopicMapResponse> setPrimaryTopic(
            @PathVariable Long termId,
            @PathVariable Long topicId) {
        log.info("设置术语的主归属主题: termId={}, topicId={}", termId, topicId);
        
        // 检查术语和主题是否存在
        if (!avTermService.existsById(termId) || !avTermsTopicService.existsById(topicId)) {
            return ResponseEntity.notFound().build();
        }
        
        // 先取消当前的主归属
        avTermTopicMapService.clearPrimaryMappingForTerm(termId);
        
        // 检查映射是否存在
        Optional<AvTermTopicMap> mappingOpt = avTermTopicMapService.findByTermIdAndTopicId(termId, topicId);
        AvTermTopicMap mapping;
        
        if (mappingOpt.isPresent()) {
            // 更新现有映射
            mapping = mappingOpt.get();
            mapping.setIsPrimary(true);
        } else {
            // 创建新映射
            mapping = AvTermTopicMap.builder()
                    .termId(termId)
                    .topicId(topicId)
                    .isPrimary(true)
                    .build();
        }
        
        AvTermTopicMap savedMapping = avTermTopicMapService.save(mapping);
        return ResponseEntity.ok(convertToTopicMapResponse(savedMapping));
    }

    /**
     * 批量添加术语到主题
     */
    @PostMapping("/batch-add-to-topic")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> batchAddToTopic(
            @RequestParam List<Long> termIds,
            @RequestParam Long topicId,
            @RequestParam(defaultValue = "false") Boolean isPrimary) {
        log.info("批量添加术语到主题: termIds={}, topicId={}, isPrimary={}", termIds, topicId, isPrimary);
        
        // 检查主题是否存在
        if (!avTermsTopicService.existsById(topicId)) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> result = new HashMap<>();
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();
        
        for (Long termId : termIds) {
            try {
                // 检查术语是否存在
                if (!avTermService.existsById(termId)) {
                    failedIds.add(termId);
                    continue;
                }
                
                // 检查映射是否已存在
                if (avTermTopicMapService.existsByTermIdAndTopicId(termId, topicId)) {
                    failedIds.add(termId);
                    continue;
                }
                
                // 如果是主归属，先取消其他主归属
                if (isPrimary) {
                    avTermTopicMapService.clearPrimaryMappingForTerm(termId);
                }
                
                AvTermTopicMap topicMap = AvTermTopicMap.builder()
                        .termId(termId)
                        .topicId(topicId)
                        .isPrimary(isPrimary != null ? isPrimary : false)
                        .build();
                
                avTermTopicMapService.save(topicMap);
                successIds.add(termId);
                
            } catch (Exception e) {
                log.error("批量添加术语到主题失败: termId={}, error={}", termId, e.getMessage());
                failedIds.add(termId);
            }
        }
        
        result.put("successCount", successIds.size());
        result.put("failedCount", failedIds.size());
        result.put("successIds", successIds);
        result.put("failedIds", failedIds);
        result.put("topicId", topicId);
        result.put("isPrimary", isPrimary);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量从主题移除术语
     */
    @DeleteMapping("/batch-remove-from-topic")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> batchRemoveFromTopic(
            @RequestParam List<Long> termIds,
            @RequestParam Long topicId) {
        log.info("批量从主题移除术语: termIds={}, topicId={}", termIds, topicId);
        
        Map<String, Object> result = new HashMap<>();
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();
        
        for (Long termId : termIds) {
            try {
                Optional<AvTermTopicMap> mappingOpt = avTermTopicMapService.findByTermIdAndTopicId(termId, topicId);
                if (mappingOpt.isPresent()) {
                    avTermTopicMapService.deleteByTermIdAndTopicId(termId, topicId);
                    successIds.add(termId);
                } else {
                    failedIds.add(termId);
                }
            } catch (Exception e) {
                log.error("批量从主题移除术语失败: termId={}, error={}", termId, e.getMessage());
                failedIds.add(termId);
            }
        }
        
        result.put("successCount", successIds.size());
        result.put("failedCount", failedIds.size());
        result.put("successIds", successIds);
        result.put("failedIds", failedIds);
        result.put("topicId", topicId);
        
        return ResponseEntity.ok(result);
    }

    // ==================== 高级查询、统计和批量操作功能 ====================
    /**
     * 高级查询术语
     */
    @PostMapping("/advanced-search")
    @Cacheable(value = "avTerms", key = "'advanced_' + #request.hashCode() + '_' + #page + '_' + #size + '_' + #sort + '_' + #direction")
    public ResponseEntity<Page<AvTermResponse>> advancedSearch(
            @Valid @RequestBody AvTermQueryRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("高级查询术语: {}", request);
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<AvTerm> avTermPage = avTermService.advancedSearch(request, pageable);
        Page<AvTermResponse> responsePage = avTermPage.map(term -> convertToResponse(term, request.getIncludeTopics()));
        
        return ResponseEntity.ok(responsePage);
    }

    /**
     * 获取术语统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("获取术语统计信息");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        long totalTerms = avTermService.count();
        stats.put("totalTerms", totalTerms);
        
        // 按CEFR等级统计
        Map<String, Long> cefrStats = new HashMap<>();
        for (AvTerm.CefrLevel level : AvTerm.CefrLevel.values()) {
            long count = avTermService.countByCefrLevel(level);
            cefrStats.put(level.name(), count);
        }
        stats.put("cefrLevelStats", cefrStats);
        
        // 按词性统计
        Map<String, Long> posStats = avTermService.getStatsByPos();
        stats.put("posStats", posStats);
        
        // 按来源统计
        Map<String, Long> sourceStats = avTermService.getStatsBySource();
        stats.put("sourceStats", sourceStats);
        
        // 数据完整性统计
        Map<String, Long> completenessStats = new HashMap<>();
        completenessStats.put("withDefinitionEn", avTermService.countWithDefinitionEn());
        completenessStats.put("withDefinitionZh", avTermService.countWithDefinitionZh());
        completenessStats.put("withExampleEn", avTermService.countWithExampleEn());
        completenessStats.put("withExampleZh", avTermService.countWithExampleZh());
        completenessStats.put("withIpa", avTermService.countWithIpa());
        completenessStats.put("withAudio", avTermService.countWithAudio());
        completenessStats.put("withFreqRank", avTermService.countWithFreqRank());
        completenessStats.put("withCefrLevel", avTermService.countWithCefrLevel());
        stats.put("completenessStats", completenessStats);
        
        // 频次排名统计
        Map<String, Long> freqRankStats = new HashMap<>();
        freqRankStats.put("highFrequency", avTermService.countByFreqRankRange(1, 1000));
        freqRankStats.put("mediumFrequency", avTermService.countByFreqRankRange(1001, 5000));
        freqRankStats.put("lowFrequency", avTermService.countByFreqRankRange(5001, Integer.MAX_VALUE));
        stats.put("freqRankStats", freqRankStats);
        
        // 主题映射统计
        Map<String, Long> topicStats = new HashMap<>();
        topicStats.put("totalMappings", avTermTopicMapService.count());
        topicStats.put("primaryMappings", avTermTopicMapService.countByIsPrimary(true));
        topicStats.put("secondaryMappings", avTermTopicMapService.countByIsPrimary(false));
        topicStats.put("termsWithTopics", avTermService.countWithTopics());
        topicStats.put("termsWithoutTopics", totalTerms - avTermService.countWithTopics());
        stats.put("topicStats", topicStats);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取术语统计信息（按条件）
     */
    @PostMapping("/statistics/conditional")
    @Cacheable(value = "avTermStats", key = "'conditional_' + #request.hashCode()")
    public ResponseEntity<Map<String, Object>> getConditionalStatistics(@Valid @RequestBody AvTermQueryRequest request) {
        log.info("获取条件统计信息: {}", request);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 符合条件的术语总数
        long totalMatched = avTermService.countByQuery(request);
        stats.put("totalMatched", totalMatched);
        
        // 按CEFR等级分布
        Map<String, Long> cefrDistribution = avTermService.getCefrDistributionByQuery(request);
        stats.put("cefrDistribution", cefrDistribution);
        
        // 按词性分布
        Map<String, Long> posDistribution = avTermService.getPosDistributionByQuery(request);
        stats.put("posDistribution", posDistribution);
        
        // 按来源分布
        Map<String, Long> sourceDistribution = avTermService.getSourceDistributionByQuery(request);
        stats.put("sourceDistribution", sourceDistribution);
        
        // 频次排名分布
        Map<String, Long> freqRankDistribution = avTermService.getFreqRankDistributionByQuery(request);
        stats.put("freqRankDistribution", freqRankDistribution);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 批量操作术语
     */
    @PostMapping("/batch-operation")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> batchOperation(@Valid @RequestBody AvTermBatchRequest request) {
        log.info("批量操作术语: operationType={}, termIds={}", request.getOperationType(), request.getTermIds());
        
        Map<String, Object> result = new HashMap<>();
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();
        
        for (Long termId : request.getTermIds()) {
            try {
                boolean success = false;
                
                switch (request.getOperationType()) {
                    case "delete":
                        success = performBatchDelete(termId);
                        break;
                    case "updateCefrLevel":
                        success = performBatchUpdateCefrLevel(termId, request.getTargetCefrLevel());
                        break;
                    case "updateSource":
                        success = performBatchUpdateSource(termId, request.getTargetSource());
                        break;
                    case "addToTopics":
                        success = performBatchAddToTopics(termId, request.getTargetTopicIds(), request.getIsPrimary());
                        break;
                    case "removeFromTopics":
                        success = performBatchRemoveFromTopics(termId, request.getTargetTopicIds());
                        break;
                    case "updateFreqRank":
                        int newFreqRank = request.getStartFreqRank() + (successIds.size() * request.getFreqRankStep());
                        success = performBatchUpdateFreqRank(termId, newFreqRank);
                        break;
                    default:
                        log.warn("未知的批量操作类型: {}", request.getOperationType());
                        break;
                }
                
                if (success) {
                    successIds.add(termId);
                } else {
                    failedIds.add(termId);
                }
                
            } catch (Exception e) {
                log.error("批量操作失败: termId={}, operationType={}, error={}", 
                         termId, request.getOperationType(), e.getMessage());
                failedIds.add(termId);
            }
        }
        
        result.put("operationType", request.getOperationType());
        result.put("totalCount", request.getTermIds().size());
        result.put("successCount", successIds.size());
        result.put("failedCount", failedIds.size());
        result.put("successIds", successIds);
        result.put("failedIds", failedIds);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取最大频次排名
     */
    @GetMapping("/max-freq-rank")
    @Cacheable(value = "avTermStats", key = "'maxFreqRank'")
    public ResponseEntity<Map<String, Object>> getMaxFreqRank() {
        log.info("获取最大频次排名");
        
        Integer maxFreqRank = avTermService.getMaxFreqRank();
        Map<String, Object> result = new HashMap<>();
        result.put("maxFreqRank", maxFreqRank);
        result.put("nextAvailableRank", maxFreqRank != null ? maxFreqRank + 1 : 1);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 导出术语数据
     */
    @PostMapping("/export")
    public ResponseEntity<List<AvTermResponse>> exportTerms(@Valid @RequestBody AvTermQueryRequest request) {
        log.info("导出术语数据: {}", request);
        
        List<AvTerm> terms = avTermService.findAllByQuery(request);
        List<AvTermResponse> responses = terms.stream()
                .map(term -> convertToResponse(term, request.getIncludeTopics()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * 随机获取术语
     */
    @GetMapping("/random")
    @Cacheable(value = "avTerms", key = "'random_' + #count + '_' + #cefrLevel + '_' + #hasAudio")
    public ResponseEntity<List<AvTermSimpleResponse>> getRandomTerms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) AvTerm.CefrLevel cefrLevel,
            @RequestParam(required = false) Boolean hasAudio) {
        log.info("随机获取术语: count={}, cefrLevel={}, hasAudio={}", count, cefrLevel, hasAudio);
        
        List<AvTerm> randomTerms = avTermService.findRandomTerms(count, cefrLevel, hasAudio);
        List<AvTermSimpleResponse> responses = randomTerms.stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    // ==================== 批量操作辅助方法 ====================

    private boolean performBatchDelete(Long termId) {
        if (!avTermService.existsById(termId)) {
            return false;
        }
        
        // 删除相关的主题映射
        avTermTopicMapService.deleteByTermId(termId);
        
        // 删除术语
        avTermService.deleteById(termId);
        return true;
    }

    private boolean performBatchUpdateCefrLevel(Long termId, AvTerm.CefrLevel cefrLevel) {
        if (cefrLevel == null) {
            return false;
        }
        
        Optional<AvTerm> termOpt = avTermService.findById(termId);
        if (termOpt.isEmpty()) {
            return false;
        }
        
        AvTerm term = termOpt.get();
        term.setCefrLevel(cefrLevel);
        avTermService.save(term);
        return true;
    }

    private boolean performBatchUpdateSource(Long termId, String source) {
        if (source == null || source.trim().isEmpty()) {
            return false;
        }
        
        Optional<AvTerm> termOpt = avTermService.findById(termId);
        if (termOpt.isEmpty()) {
            return false;
        }
        
        AvTerm term = termOpt.get();
        term.setSource(source);
        avTermService.save(term);
        return true;
    }

    private boolean performBatchAddToTopics(Long termId, List<Long> topicIds, Boolean isPrimary) {
        if (topicIds == null || topicIds.isEmpty()) {
            return false;
        }
        
        if (!avTermService.existsById(termId)) {
            return false;
        }
        
        boolean allSuccess = true;
        for (Long topicId : topicIds) {
            try {
                if (!avTermsTopicService.existsById(topicId)) {
                    allSuccess = false;
                    continue;
                }
                
                if (avTermTopicMapService.existsByTermIdAndTopicId(termId, topicId)) {
                    continue; // 映射已存在，跳过
                }
                
                // 如果是主归属，先取消其他主归属
                if (isPrimary != null && isPrimary) {
                    avTermTopicMapService.clearPrimaryMappingForTerm(termId);
                }
                
                AvTermTopicMap topicMap = AvTermTopicMap.builder()
                        .termId(termId)
                        .topicId(topicId)
                        .isPrimary(isPrimary != null ? isPrimary : false)
                        .build();
                
                avTermTopicMapService.save(topicMap);
                
            } catch (Exception e) {
                log.error("批量添加到主题失败: termId={}, topicId={}, error={}", termId, topicId, e.getMessage());
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }

    private boolean performBatchRemoveFromTopics(Long termId, List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return false;
        }
        
        boolean allSuccess = true;
        for (Long topicId : topicIds) {
            try {
                if (avTermTopicMapService.existsByTermIdAndTopicId(termId, topicId)) {
                    avTermTopicMapService.deleteByTermIdAndTopicId(termId, topicId);
                }
            } catch (Exception e) {
                log.error("批量从主题移除失败: termId={}, topicId={}, error={}", termId, topicId, e.getMessage());
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }

    private boolean performBatchUpdateFreqRank(Long termId, Integer freqRank) {
        if (freqRank == null || freqRank < 1) {
            return false;
        }
        
        Optional<AvTerm> termOpt = avTermService.findById(termId);
        if (termOpt.isEmpty()) {
            return false;
        }
        
        AvTerm term = termOpt.get();
        term.setFreqRank(freqRank);
        avTermService.save(term);
        return true;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将AvTerm实体转换为AvTermResponse
     */
    private AvTermResponse convertToResponse(AvTerm avTerm, boolean includeTopics) {
        AvTermResponse response = AvTermResponse.builder()
                .id(avTerm.getId())
                .headword(avTerm.getHeadword())
                .lemma(avTerm.getLemma())
                .pos(avTerm.getPos())
                .ipa(avTerm.getIpa())
                .definitionEn(avTerm.getDefinitionEn())
                .definitionZh(avTerm.getDefinitionZh())
                .exampleEn(avTerm.getExampleEn())
                .exampleZh(avTerm.getExampleZh())
                .cefrLevel(avTerm.getCefrLevel())
                .freqRank(avTerm.getFreqRank())
                .source(avTerm.getSource())
                .audioAssetId(avTerm.getAudioAssetId())
                .extraJson(avTerm.getExtraJson())
                .createdAt(avTerm.getCreatedAt())
                .build();
        
        // 设置扩展字段
        setExtendedFields(response, avTerm);
        
        // 如果需要包含主题信息
        if (includeTopics) {
            List<AvTermTopicMap> topicMaps = avTermTopicMapService.findByTermId(avTerm.getId());
            List<AvTermTopicMapResponse> topicResponses = topicMaps.stream()
                    .map(this::convertToTopicMapResponse)
                    .collect(Collectors.toList());
            response.setTopics(topicResponses);
        }
        
        return response;
    }

    /**
     * 将AvTerm实体转换为AvTermSimpleResponse
     */
    private AvTermSimpleResponse convertToSimpleResponse(AvTerm avTerm) {
        AvTermSimpleResponse response = AvTermSimpleResponse.builder()
                .id(avTerm.getId())
                .headword(avTerm.getHeadword())
                .pos(avTerm.getPos())
                .ipa(avTerm.getIpa())
                .definitionZh(avTerm.getDefinitionZh())
                .cefrLevel(avTerm.getCefrLevel())
                .freqRank(avTerm.getFreqRank())
                .source(avTerm.getSource())
                .build();
        
        // 设置扩展字段
        setSimpleExtendedFields(response, avTerm);
        
        return response;
    }

    /**
     * 设置扩展字段
     */
    private void setExtendedFields(AvTermResponse response, AvTerm avTerm) {
        // 显示用的词条（带音标）
        String displayHeadword = avTerm.getHeadword();
        if (avTerm.getIpa() != null && !avTerm.getIpa().trim().isEmpty()) {
            displayHeadword += " [" + avTerm.getIpa() + "]";
        }
        response.setDisplayHeadword(displayHeadword);
        
        // 完整释义
        StringBuilder fullDefinition = new StringBuilder();
        if (avTerm.getDefinitionEn() != null && !avTerm.getDefinitionEn().trim().isEmpty()) {
            fullDefinition.append(avTerm.getDefinitionEn());
        }
        if (avTerm.getDefinitionZh() != null && !avTerm.getDefinitionZh().trim().isEmpty()) {
            if (fullDefinition.length() > 0) {
                fullDefinition.append(" | ");
            }
            fullDefinition.append(avTerm.getDefinitionZh());
        }
        response.setFullDefinition(fullDefinition.toString());
        
        // 完整例句
        StringBuilder fullExample = new StringBuilder();
        if (avTerm.getExampleEn() != null && !avTerm.getExampleEn().trim().isEmpty()) {
            fullExample.append(avTerm.getExampleEn());
        }
        if (avTerm.getExampleZh() != null && !avTerm.getExampleZh().trim().isEmpty()) {
            if (fullExample.length() > 0) {
                fullExample.append(" | ");
            }
            fullExample.append(avTerm.getExampleZh());
        }
        response.setFullExample(fullExample.toString());
        
        // 布尔字段
        response.setHasLemma(avTerm.getLemma() != null && !avTerm.getLemma().trim().isEmpty());
        response.setHasPos(avTerm.getPos() != null && !avTerm.getPos().trim().isEmpty());
        response.setHasIpa(avTerm.getIpa() != null && !avTerm.getIpa().trim().isEmpty());
        response.setHasDefinitionEn(avTerm.getDefinitionEn() != null && !avTerm.getDefinitionEn().trim().isEmpty());
        response.setHasDefinitionZh(avTerm.getDefinitionZh() != null && !avTerm.getDefinitionZh().trim().isEmpty());
        response.setHasExampleEn(avTerm.getExampleEn() != null && !avTerm.getExampleEn().trim().isEmpty());
        response.setHasExampleZh(avTerm.getExampleZh() != null && !avTerm.getExampleZh().trim().isEmpty());
        response.setHasCefrLevel(avTerm.getCefrLevel() != null);
        response.setHasFreqRank(avTerm.getFreqRank() != null);
        response.setHasSource(avTerm.getSource() != null && !avTerm.getSource().trim().isEmpty());
        response.setHasAudioAsset(avTerm.getAudioAssetId() != null);
        response.setHasExtraJson(avTerm.getExtraJson() != null && !avTerm.getExtraJson().isEmpty());
        
        // CEFR等级分类
        if (avTerm.getCefrLevel() != null) {
            response.setCefrLevelDescription(getCefrLevelDescription(avTerm.getCefrLevel()));
            response.setIsBasicVocabulary(avTerm.getCefrLevel() == AvTerm.CefrLevel.A1 || avTerm.getCefrLevel() == AvTerm.CefrLevel.A2);
            response.setIsIntermediateVocabulary(avTerm.getCefrLevel() == AvTerm.CefrLevel.B1 || avTerm.getCefrLevel() == AvTerm.CefrLevel.B2);
            response.setIsAdvancedVocabulary(avTerm.getCefrLevel() == AvTerm.CefrLevel.C1 || avTerm.getCefrLevel() == AvTerm.CefrLevel.C2);
        } else {
            response.setIsBasicVocabulary(false);
            response.setIsIntermediateVocabulary(false);
            response.setIsAdvancedVocabulary(false);
        }
        
        // 频次分类
        if (avTerm.getFreqRank() != null) {
            response.setIsHighFrequencyTerm(avTerm.getFreqRank() <= 1000);
            response.setIsLowFrequencyTerm(avTerm.getFreqRank() > 5000);
        } else {
            response.setIsHighFrequencyTerm(false);
            response.setIsLowFrequencyTerm(false);
        }
        
        // 数据完整性检查
        boolean isValid = avTerm.getHeadword() != null && !avTerm.getHeadword().trim().isEmpty() &&
                         (response.getHasDefinitionEn() || response.getHasDefinitionZh());
        response.setIsValid(isValid);
        
        // 是否为完整术语
        boolean isCompleteTerm = response.getIsValid() && 
                               (response.getHasExampleEn() || response.getHasExampleZh()) &&
                               response.getHasCefrLevel();
        response.setIsCompleteTerm(isCompleteTerm);
    }

    /**
     * 设置简化扩展字段
     */
    private void setSimpleExtendedFields(AvTermSimpleResponse response, AvTerm avTerm) {
        // 显示用的词条（带音标）
        String displayHeadword = avTerm.getHeadword();
        if (avTerm.getIpa() != null && !avTerm.getIpa().trim().isEmpty()) {
            displayHeadword += " [" + avTerm.getIpa() + "]";
        }
        response.setDisplayHeadword(displayHeadword);
        
        // CEFR等级描述
        if (avTerm.getCefrLevel() != null) {
            response.setCefrLevelDescription(getCefrLevelDescription(avTerm.getCefrLevel()));
        }
        
        // 布尔字段
        response.setHasAudioAsset(avTerm.getAudioAssetId() != null);
        
        // 频次分类
        if (avTerm.getFreqRank() != null) {
            response.setIsHighFrequencyTerm(avTerm.getFreqRank() <= 1000);
        } else {
            response.setIsHighFrequencyTerm(false);
        }
        
        // CEFR等级分类
        if (avTerm.getCefrLevel() != null) {
            response.setIsBasicVocabulary(avTerm.getCefrLevel() == AvTerm.CefrLevel.A1 || avTerm.getCefrLevel() == AvTerm.CefrLevel.A2);
            response.setIsIntermediateVocabulary(avTerm.getCefrLevel() == AvTerm.CefrLevel.B1 || avTerm.getCefrLevel() == AvTerm.CefrLevel.B2);
            response.setIsAdvancedVocabulary(avTerm.getCefrLevel() == AvTerm.CefrLevel.C1 || avTerm.getCefrLevel() == AvTerm.CefrLevel.C2);
        } else {
            response.setIsBasicVocabulary(false);
            response.setIsIntermediateVocabulary(false);
            response.setIsAdvancedVocabulary(false);
        }
        
        // 数据完整性检查
        boolean hasDefinition = (avTerm.getDefinitionEn() != null && !avTerm.getDefinitionEn().trim().isEmpty()) ||
                               (avTerm.getDefinitionZh() != null && !avTerm.getDefinitionZh().trim().isEmpty());
        boolean isValid = avTerm.getHeadword() != null && !avTerm.getHeadword().trim().isEmpty() && hasDefinition;
        response.setIsValid(isValid);
        
        // 主题数量（需要查询）
        int topicCount = avTermTopicMapService.countByTermId(avTerm.getId());
        response.setTopicCount(topicCount);
    }

    /**
     * 获取CEFR等级描述
     */
    private String getCefrLevelDescription(AvTerm.CefrLevel cefrLevel) {
        switch (cefrLevel) {
            case A1: return "入门级";
            case A2: return "基础级";
            case B1: return "中级";
            case B2: return "中高级";
            case C1: return "高级";
            case C2: return "精通级";
            default: return "未知";
        }
    }

    /**
     * 将AvTermTopicMap实体转换为AvTermTopicMapResponse
     */
    private AvTermTopicMapResponse convertToTopicMapResponse(AvTermTopicMap topicMap) {
        AvTermTopicMapResponse response = AvTermTopicMapResponse.builder()
                .id(topicMap.getId())
                .termId(topicMap.getTermId())
                .topicId(topicMap.getTopicId())
                .isPrimary(topicMap.getIsPrimary())
                .build();
        
        // 获取术语信息
        Optional<AvTerm> termOpt = avTermService.findById(topicMap.getTermId());
        if (termOpt.isPresent()) {
            response.setTermHeadword(termOpt.get().getHeadword());
        }
        
        // 获取主题信息
        Optional<AvTermsTopic> topicOpt = avTermsTopicService.findById(topicMap.getTopicId());
        if (topicOpt.isPresent()) {
            AvTermsTopic topic = topicOpt.get();
            response.setTopicName(topic.getDisplayName());
            response.setTopicDescription(topic.getDescription());
        }
        
        // 设置扩展字段
        response.setMappingTypeDescription(topicMap.getIsPrimary() ? "主归属" : "次归属");
        response.setIsValidMapping(response.getTermHeadword() != null && response.getTopicName() != null);
        
        return response;
    }

    /**
     * 从请求更新AvTerm实体
     */
    private void updateAvTermFromRequest(AvTerm avTerm, AvTermUpdateRequest request) {
        if (request.getHeadword() != null) {
            avTerm.setHeadword(request.getHeadword());
        }
        if (request.getLemma() != null) {
            avTerm.setLemma(request.getLemma());
        }
        if (request.getPos() != null) {
            avTerm.setPos(request.getPos());
        }
        if (request.getIpa() != null) {
            avTerm.setIpa(request.getIpa());
        }
        if (request.getDefinitionEn() != null) {
            avTerm.setDefinitionEn(request.getDefinitionEn());
        }
        if (request.getDefinitionZh() != null) {
            avTerm.setDefinitionZh(request.getDefinitionZh());
        }
        if (request.getExampleEn() != null) {
            avTerm.setExampleEn(request.getExampleEn());
        }
        if (request.getExampleZh() != null) {
            avTerm.setExampleZh(request.getExampleZh());
        }
        if (request.getCefrLevel() != null) {
            avTerm.setCefrLevel(request.getCefrLevel());
        }
        if (request.getFreqRank() != null) {
            avTerm.setFreqRank(request.getFreqRank());
        }
        if (request.getSource() != null) {
            avTerm.setSource(request.getSource());
        }
        if (request.getAudioAssetId() != null) {
            avTerm.setAudioAssetId(request.getAudioAssetId());
        }
        if (request.getExtraJson() != null) {
            avTerm.setExtraJson(request.getExtraJson());
        }
    }

    // ==================== Data Import/Export API (数据导入导出) ====================
    
    /**
     * 导出术语数据
     */
    @GetMapping("/export")
    public ResponseEntity<List<AvTermResponse>> exportTermsData(
            @RequestParam(required = false) String format,
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) Boolean includeTopics) {
        
        AvTermQueryRequest queryRequest = new AvTermQueryRequest();
        if (includeTopics != null) {
            queryRequest.setIncludeTopics(includeTopics);
        }
        
        List<AvTerm> terms;
        if (ids != null && !ids.isEmpty()) {
            terms = ids.stream()
                    .map(avTermService::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } else {
            terms = avTermService.findAllByQuery(queryRequest);
        }
        
        List<AvTermResponse> responses = terms.stream()
                .map(term -> convertToResponse(term, queryRequest.getIncludeTopics()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * 导入术语数据
     */
    @PostMapping("/import")
    @CacheEvict(value = {"avTerms", "avTermStats"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> importTerms(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        // TODO: Implement import logic
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("importedCount", 0);
        response.put("message", "Import functionality not implemented yet");
        return ResponseEntity.ok(response);
    }

    // ==================== Additional Utility Methods ====================
    
    /**
     * 获取术语分类选项
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getTermCategories() {
        List<String> categories = List.of(
            "COMMUNICATION",
            "NAVIGATION", 
            "METEOROLOGY",
            "EMERGENCY",
            "AIRCRAFT",
            "AIRPORT",
            "PROCEDURES",
            "EQUIPMENT",
            "GENERAL"
        );
        return ResponseEntity.ok(categories);
    }

    /**
     * 获取术语来源选项
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getTermSources() {
        List<String> sources = List.of(
            "ICAO DOC4444",
            "ICAO DOC8168",
            "ICAO DOC9432",
            "ICAO Annex 10",
            "ICAO Annex 11",
            "ICAO Annex 14",
            "IATA",
            "FAA",
            "EUROCONTROL",
            "Custom"
        );
        return ResponseEntity.ok(sources);
    }
}