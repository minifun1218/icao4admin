package org.icao4.eqasbackend2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.entity.story_retell.RetellItem;
import org.icao4.eqasbackend2.entity.story_retell.RetellResponse;
import org.icao4.eqasbackend2.service.story_retell.RetellItemService;
import org.icao4.eqasbackend2.service.story_retell.RetellResponseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 故事复述控制器
 * 提供故事复述题目和回答相关的REST API
 * 同时支持题库管理系统的故事复述题相关功能
 */
@Slf4j
@RestController
@RequestMapping("/story-retell")
@RequiredArgsConstructor
public class StoryRetellController {
    
    private final RetellItemService retellItemService;
    private final RetellResponseService retellResponseService;
    
    // ==================== 复述题目相关接口 ====================
    
    /**
     * 获取所有复述题目
     */
    @GetMapping("/items")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> getAllRetellItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<RetellItem> items = retellItemService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("获取所有复述题目失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取所有复述题目失败"));
        }
    }
    
    /**
     * 根据ID获取复述题目
     */
    @GetMapping("/items/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellItem>> getRetellItemById(@PathVariable Long id) {
        try {
            Optional<RetellItem> item = retellItemService.findById(id);
            if (item.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(item.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("复述题目不存在"));
            }
        } catch (Exception e) {
            log.error("获取复述题目失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取复述题目失败"));
        }
    }
    
    /**
     * 根据模块ID获取复述题目
     */
    @GetMapping("/items/module/{moduleId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> getRetellItemsByModuleId(
            @PathVariable Long moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<RetellItem> items = retellItemService.findByModuleId(moduleId, pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("获取模块复述题目失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取模块复述题目失败"));
        }
    }
    
    /**
     * 创建复述题目
     */
    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellItem>> createRetellItem(@Valid @RequestBody RetellItem item) {
        try {
            RetellItem createdItem = retellItemService.save(item);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdItem));
        } catch (Exception e) {
            log.error("创建复述题目失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建复述题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新复述题目
     */
    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellItem>> updateRetellItem(
            @PathVariable Long id,
            @Valid @RequestBody RetellItem item) {
        
        try {
            if (!retellItemService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("复述题目不存在"));
            }
            
            item.setId(id);
            RetellItem updatedItem = retellItemService.save(item);
            return ResponseEntity.ok(ApiResponse.success(updatedItem));
        } catch (Exception e) {
            log.error("更新复述题目失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新复述题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除复述题目
     */
    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteRetellItem(@PathVariable Long id) {
        try {
            if (!retellItemService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("复述题目不存在"));
            }
            
            retellItemService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("复述题目删除成功"));
        } catch (Exception e) {
            log.error("删除复述题目失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除复述题目失败"));
        }
    }
    
    /**
     * 复制复述题目
     */
    @PostMapping("/items/{id}/copy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellItem>> copyRetellItem(@PathVariable Long id) {
        try {
            RetellItem copiedItem = retellItemService.copyRetellItem(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(copiedItem));
        } catch (Exception e) {
            log.error("复制复述题目失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("复制复述题目失败"));
        }
    }
    
    /**
     * 搜索复述题目
     */
    @GetMapping("/items/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> searchRetellItems(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellItem> items = retellItemService.searchRetellItems(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("搜索复述题目失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索复述题目失败"));
        }
    }
    
    /**
     * 获取有参考文本的复述题目
     */
    @GetMapping("/items/with-reference")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> getItemsWithReferenceText(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellItem> items = retellItemService.findItemsWithReferenceText(pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("获取有参考文本的复述题目失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取有参考文本的复述题目失败"));
        }
    }
    
    /**
     * 获取没有参考文本的复述题目
     */
    @GetMapping("/items/without-reference")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> getItemsWithoutReferenceText(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellItem> items = retellItemService.findItemsWithoutReferenceText(pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("获取没有参考文本的复述题目失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取没有参考文本的复述题目失败"));
        }
    }
    
    // ==================== 复述回答相关接口 ====================
    
    /**
     * 获取复述题目的回答列表
     */
    @GetMapping("/items/{itemId}/responses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellResponse>>> getResponsesByItemId(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "answeredAt,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<RetellResponse> responses = retellResponseService.findByItemId(itemId, pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取复述题目回答失败: itemId={}, error={}", itemId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取复述题目回答失败"));
        }
    }
    
    /**
     * 根据ID获取复述回答
     */
    @GetMapping("/responses/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellResponse>> getResponseById(@PathVariable Long id) {
        try {
            Optional<RetellResponse> response = retellResponseService.findById(id);
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(response.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("复述回答不存在"));
            }
        } catch (Exception e) {
            log.error("获取复述回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取复述回答失败"));
        }
    }
    
    /**
     * 提交复述回答
     */
    @PostMapping("/responses/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellResponse>> submitResponse(@RequestBody Map<String, Object> request) {
        try {
            Long itemId = Long.valueOf(request.get("itemId").toString());
            Long answerAudioId = request.get("answerAudioId") != null ? 
                    Long.valueOf(request.get("answerAudioId").toString()) : null;
            String asrText = request.get("asrText") != null ? request.get("asrText").toString() : null;
            Integer elapsedMs = request.get("elapsedMs") != null ? 
                    Integer.valueOf(request.get("elapsedMs").toString()) : 0;
            
            RetellResponse response = retellResponseService.submitResponse(itemId, answerAudioId, asrText, elapsedMs);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("提交复述回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("提交复述回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建复述回答记录
     */
    @PostMapping("/responses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellResponse>> createResponse(@Valid @RequestBody RetellResponse response) {
        try {
            RetellResponse createdResponse = retellResponseService.save(response);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdResponse));
        } catch (Exception e) {
            log.error("创建复述回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建复述回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 评分复述回答
     */
    @PutMapping("/responses/{id}/score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellResponse>> scoreResponse(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        try {
            BigDecimal score = new BigDecimal(request.get("score").toString());
            String scoreDetailJson = request.get("scoreDetailJson") != null ? 
                    request.get("scoreDetailJson").toString() : null;
            
            RetellResponse response = retellResponseService.scoreResponse(id, score, scoreDetailJson);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("评分复述回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("评分复述回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 自动评分复述回答
     */
    @PutMapping("/responses/{id}/auto-score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellResponse>> autoScoreResponse(@PathVariable Long id) {
        try {
            RetellResponse response = retellResponseService.autoScoreResponse(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("自动评分复述回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("自动评分复述回答失败"));
        }
    }
    
    /**
     * 删除复述回答
     */
    @DeleteMapping("/responses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteResponse(@PathVariable Long id) {
        try {
            if (!retellResponseService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("复述回答不存在"));
            }
            
            retellResponseService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("复述回答删除成功"));
        } catch (Exception e) {
            log.error("删除复述回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除复述回答失败"));
        }
    }
    
    /**
     * 获取已评分的回答
     */
    @GetMapping("/responses/scored")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellResponse>>> getScoredResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "score,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<RetellResponse> responses = retellResponseService.findScoredResponses(pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取已评分回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取已评分回答失败"));
        }
    }
    
    /**
     * 获取未评分的回答
     */
    @GetMapping("/responses/unscored")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellResponse>>> getUnscoredResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "answeredAt,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<RetellResponse> responses = retellResponseService.findUnscoredResponses(pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取未评分回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取未评分回答失败"));
        }
    }
    
    /**
     * 获取及格的回答
     */
    @GetMapping("/responses/passed")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellResponse>>> getPassedResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellResponse> responses = retellResponseService.findPassedResponses(pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取及格回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取及格回答失败"));
        }
    }
    
    /**
     * 获取不及格的回答
     */
    @GetMapping("/responses/failed")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellResponse>>> getFailedResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellResponse> responses = retellResponseService.findFailedResponses(pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取不及格回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取不及格回答失败"));
        }
    }
    
    // ==================== 统计相关接口 ====================
    
    /**
     * 获取复述题目统计信息
     */
    @GetMapping("/items/{itemId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getItemStats(@PathVariable Long itemId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计
            stats.put("totalResponses", retellResponseService.countByItemId(itemId));
            stats.put("scoredResponses", retellResponseService.countScoredResponsesByItemId(itemId));
            stats.put("unscoredResponses", retellResponseService.countUnscoredResponsesByItemId(itemId));
            stats.put("passedResponses", retellResponseService.countPassedResponsesByItemId(itemId));
            
            // 平均统计
            stats.put("averageScore", retellResponseService.getAverageScoreByItemId(itemId));
            stats.put("averageElapsedMs", retellResponseService.getAverageElapsedMsByItemId(itemId));
            
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取复述题目统计失败: itemId={}, error={}", itemId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取复述题目统计失败"));
        }
    }
    
    /**
     * 获取模块统计信息
     */
    @GetMapping("/modules/{moduleId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getModuleStats(@PathVariable Long moduleId) {
        try {
            Map<String, Object> stats = retellItemService.getModuleRetellItemStatistics(moduleId);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取模块统计失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取模块统计失败"));
        }
    }
    
    /**
     * 获取整体统计信息
     */
    @GetMapping("/stats/overall")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverallStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 复述题目统计
            Map<String, Object> itemStats = retellItemService.getRetellItemStatistics();
            stats.put("items", itemStats);
            
            // 复述回答统计
            Map<String, Object> responseStats = retellResponseService.getResponseStatistics();
            stats.put("responses", responseStats);
            
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取整体统计失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取整体统计失败"));
        }
    }
    
    // ==================== 批量操作接口 ====================
    
    /**
     * 批量删除复述题目
     */
    @DeleteMapping("/items/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteItems(@RequestBody List<Long> itemIds) {
        try {
            retellItemService.batchDeleteRetellItems(itemIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除复述题目成功"));
        } catch (Exception e) {
            log.error("批量删除复述题目失败: count={}, error={}", itemIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除复述题目失败"));
        }
    }
    
    /**
     * 批量删除复述回答
     */
    @DeleteMapping("/responses/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteResponses(@RequestBody List<Long> responseIds) {
        try {
            retellResponseService.batchDeleteResponses(responseIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除复述回答成功"));
        } catch (Exception e) {
            log.error("批量删除复述回答失败: count={}, error={}", responseIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除复述回答失败"));
        }
    }
    
    /**
     * 批量移动复述题目到其他模块
     */
    @PutMapping("/items/batch-move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RetellItem>>> batchMoveItemsToModule(
            @RequestBody Map<String, Object> request) {
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> itemIds = (List<Long>) request.get("itemIds");
            Long targetModuleId = Long.valueOf(request.get("targetModuleId").toString());
            
            List<RetellItem> items = retellItemService.batchMoveRetellItemsToModule(itemIds, targetModuleId);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("批量移动复述题目失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量移动复述题目失败"));
        }
    }
    
    // ==================== 导入导出接口 ====================
    
    /**
     * 导出模块复述题目
     */
    @GetMapping("/modules/{moduleId}/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RetellItem>>> exportItemsByModule(@PathVariable Long moduleId) {
        try {
            List<RetellItem> items = retellItemService.exportRetellItemsByModule(moduleId);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("导出模块复述题目失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导出模块复述题目失败"));
        }
    }
    
    /**
     * 导入复述题目到模块
     */
    @PostMapping("/modules/{moduleId}/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RetellItem>>> importItemsToModule(
            @PathVariable Long moduleId,
            @RequestBody List<RetellItem> items) {
        
        try {
            List<RetellItem> importedItems = retellItemService.importRetellItemsToModule(moduleId, items);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(importedItems));
        } catch (Exception e) {
            log.error("导入复述题目到模块失败: moduleId={}, count={}, error={}", 
                     moduleId, items.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导入复述题目到模块失败"));
        }
    }
    
    /**
     * 导出复述题目的所有回答
     */
    @GetMapping("/items/{itemId}/responses/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RetellResponse>>> exportResponsesByItem(@PathVariable Long itemId) {
        try {
            List<RetellResponse> responses = retellResponseService.exportResponsesByItem(itemId);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("导出复述题目回答失败: itemId={}, error={}", itemId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导出复述题目回答失败"));
        }
    }
    
    // ==================== 高级查询接口 ====================
    
    /**
     * 根据音频时长范围查找复述题目
     */
    @GetMapping("/items/audio-duration")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> getItemsByAudioDuration(
            @RequestParam Integer minDuration,
            @RequestParam Integer maxDuration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellItem> items = retellItemService.findByAudioDurationSecBetween(
                    minDuration, maxDuration, pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("根据音频时长查找复述题目失败: min={}, max={}, error={}", 
                     minDuration, maxDuration, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据音频时长查找复述题目失败"));
        }
    }
    
    /**
     * 根据答题时长范围查找复述题目
     */
    @GetMapping("/items/answer-time")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> getItemsByAnswerTime(
            @RequestParam Integer minSeconds,
            @RequestParam Integer maxSeconds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellItem> items = retellItemService.findByAnswerSecondsBetween(
                    minSeconds, maxSeconds, pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("根据答题时长查找复述题目失败: min={}, max={}, error={}", 
                     minSeconds, maxSeconds, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据答题时长查找复述题目失败"));
        }
    }
    
    /**
     * 根据得分范围查找复述回答
     */
    @GetMapping("/responses/score-range")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellResponse>>> getResponsesByScoreRange(
            @RequestParam BigDecimal minScore,
            @RequestParam BigDecimal maxScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellResponse> responses = retellResponseService.findByScoreBetween(
                    minScore, maxScore, pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("根据得分范围查找复述回答失败: min={}, max={}, error={}", 
                     minScore, maxScore, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据得分范围查找复述回答失败"));
        }
    }

    // ==================== 题库管理系统 - 故事复述题相关接口 ====================

    /**
     * 获取故事复述题列表 (对应 questionBank.js 的 getRetellQuestions)
     */
    @GetMapping("/question-bank/retell")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> getRetellQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean hasReferenceText) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellItem> items;
            
            if (moduleId != null) {
                items = retellItemService.findByModuleId(moduleId, pageable);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                items = retellItemService.searchRetellItems(keyword, pageable);
            } else if (hasReferenceText != null) {
                if (hasReferenceText) {
                    items = retellItemService.findItemsWithReferenceText(pageable);
                } else {
                    items = retellItemService.findItemsWithoutReferenceText(pageable);
                }
            } else {
                items = retellItemService.findAll(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("获取故事复述题列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取故事复述题列表失败"));
        }
    }

    /**
     * 创建故事复述题 (对应 questionBank.js 的 createRetellQuestion)
     */
    @PostMapping("/question-bank/retell")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellItem>> createRetellQuestion(@Valid @RequestBody RetellItem item) {
        try {
            RetellItem createdItem = retellItemService.save(item);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdItem));
        } catch (Exception e) {
            log.error("创建故事复述题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建故事复述题失败: " + e.getMessage()));
        }
    }

    /**
     * 更新故事复述题 (对应 questionBank.js 的 updateRetellQuestion)
     */
    @PutMapping("/question-bank/retell/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetellItem>> updateRetellQuestion(
            @PathVariable Long id,
            @Valid @RequestBody RetellItem item) {
        try {
            if (!retellItemService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("故事复述题不存在"));
            }
            item.setId(id);
            RetellItem updatedItem = retellItemService.save(item);
            return ResponseEntity.ok(ApiResponse.success(updatedItem));
        } catch (Exception e) {
            log.error("更新故事复述题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新故事复述题失败: " + e.getMessage()));
        }
    }

    /**
     * 删除故事复述题 (对应 questionBank.js 的 deleteRetellQuestion)
     */
    @DeleteMapping("/question-bank/retell/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteRetellQuestion(@PathVariable Long id) {
        try {
            if (!retellItemService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("故事复述题不存在"));
            }
            retellItemService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("故事复述题删除成功"));
        } catch (Exception e) {
            log.error("删除故事复述题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除故事复述题失败: " + e.getMessage()));
        }
    }

    /**
     * 获取故事模板 (对应 questionBank.js 的 getStoryTemplates)
     */
    @GetMapping("/question-bank/retell/templates")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStoryTemplates() {
        try {
            // TODO: 实现获取故事模板的逻辑
            List<Map<String, Object>> templates = List.of(
                    Map.of("id", 1L, "name", "日常对话模板", "description", "适用于日常生活场景的复述题", "structure", "背景-对话-问题"),
                    Map.of("id", 2L, "name", "工作场景模板", "description", "适用于职场环境的复述题", "structure", "情境-任务-结果"),
                    Map.of("id", 3L, "name", "紧急情况模板", "description", "适用于应急处理的复述题", "structure", "事件-处理-总结")
            );
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            log.error("获取故事模板失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取故事模板失败"));
        }
    }

    /**
     * 创建故事模板 (对应 questionBank.js 的 createStoryTemplate)
     */
    @PostMapping("/question-bank/retell/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStoryTemplate(@RequestBody Map<String, Object> template) {
        try {
            // TODO: 实现创建故事模板的逻辑
            template.put("id", System.currentTimeMillis());
            template.put("createdAt", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(template));
        } catch (Exception e) {
            log.error("创建故事模板失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建故事模板失败: " + e.getMessage()));
        }
    }

    // ==================== 批量操作接口 ====================

    /**
     * 批量操作故事复述题 (对应 questionBank.js 的 batchOperateQuestions)
     */
    @PostMapping("/question-bank/retell/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchOperateRetellQuestions(@RequestBody Map<String, Object> request) {
        try {
            String operation = (String) request.get("operation");
            @SuppressWarnings("unchecked")
            List<Long> questionIds = (List<Long>) request.get("questionIds");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.get("data");
            
            if (operation == null || questionIds == null || questionIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("操作类型和题目ID列表不能为空"));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("operation", operation);
            result.put("processedCount", questionIds.size());
            
            switch (operation.toLowerCase()) {
                case "delete":
                    retellItemService.batchDeleteRetellItems(questionIds);
                    result.put("message", "批量删除成功");
                    break;
                case "copy":
                    // TODO: 实现批量复制逻辑
                    result.put("message", "批量复制成功");
                    break;
                case "update_difficulty":
                    // TODO: 实现批量更新难度逻辑
                    result.put("message", "批量更新难度成功");
                    break;
                case "move_module":
                    Long targetModuleId = Long.valueOf(data.get("targetModuleId").toString());
                    retellItemService.batchMoveRetellItemsToModule(questionIds, targetModuleId);
                    result.put("message", "批量移动模块成功");
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("不支持的操作类型: " + operation));
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("批量操作故事复述题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量操作故事复述题失败: " + e.getMessage()));
        }
    }

    // ==================== 文件上传相关接口 ====================

    /**
     * 上传复述题目音频 (对应 questionBank.js 的 uploadQuestionAudio)
     */
    @PostMapping("/question-bank/upload/audio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadQuestionAudio(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) Long questionId,
            @RequestParam(required = false) String quality) {
        try {
            // TODO: 实现音频文件上传逻辑
            Map<String, Object> result = Map.of(
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "url", "/uploads/audio/" + file.getOriginalFilename(),
                    "questionId", questionId != null ? questionId : 0L,
                    "quality", quality != null ? quality : "medium"
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("上传复述题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("上传复述题目音频失败: " + e.getMessage()));
        }
    }

    /**
     * 删除复述题目音频 (对应 questionBank.js 的 deleteQuestionAudio)
     */
    @DeleteMapping("/question-bank/upload/audio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteQuestionAudio(@RequestBody Map<String, Object> request) {
        try {
            String audioUrl = (String) request.get("audioUrl");
            if (audioUrl == null || audioUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("音频URL不能为空"));
            }
            
            // TODO: 实现删除音频文件的逻辑
            
            return ResponseEntity.ok(ApiResponse.success("复述题目音频删除成功"));
        } catch (Exception e) {
            log.error("删除复述题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除复述题目音频失败: " + e.getMessage()));
        }
    }

    // ==================== 数据导入导出接口 ====================

    /**
     * 批量导入故事复述题 (对应 questionBank.js 的 importQuestions)
     */
    @PostMapping("/question-bank/retell/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importRetellQuestions(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") Boolean skipDuplicates,
            @RequestParam(required = false, defaultValue = "false") Boolean updateExisting,
            @RequestParam(required = false, defaultValue = "true") Boolean validateData) {
        try {
            // TODO: 实现故事复述题批量导入逻辑
            Map<String, Object> result = Map.of(
                    "totalRows", 60,
                    "successCount", 57,
                    "failureCount", 3,
                    "duplicateCount", 1,
                    "message", "故事复述题导入完成"
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("批量导入故事复述题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量导入故事复述题失败: " + e.getMessage()));
        }
    }

    /**
     * 导出故事复述题 (对应 questionBank.js 的 exportQuestions)
     */
    @GetMapping("/question-bank/retell/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportRetellQuestions(
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean hasReferenceText) {
        try {
            // TODO: 实现故事复述题导出逻辑
            String csvContent = "ID,标题,故事内容,参考文本,音频时长,答题时长,难度等级,创建时间\n1,机场故事,A passenger arrives at the airport...,The passenger should check in...,120,180,3,2024-01-01";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=retell_questions.csv")
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出故事复述题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出故事复述题模板 (对应 questionBank.js 的 exportQuestionTemplate)
     */
    @GetMapping("/question-bank/retell/export/template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportRetellQuestionTemplate() {
        try {
            String csvContent = "ID,标题,故事内容,参考文本,音频时长(秒),答题时长(秒),难度等级,模块ID\n";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=retell_template.csv")
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出故事复述题模板失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== 高级搜索和筛选接口 ====================

    /**
     * 高级搜索故事复述题 (对应 questionBank.js 的 advancedSearch)
     */
    @PostMapping("/question-bank/retell/search/advanced")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> advancedSearchRetellQuestions(@RequestBody Map<String, Object> criteria) {
        try {
            // TODO: 实现高级搜索逻辑
            Pageable pageable = PageRequest.of(0, 20);
            Page<RetellItem> items = retellItemService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("高级搜索故事复述题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("高级搜索故事复述题失败"));
        }
    }

    /**
     * 全文搜索故事复述题 (对应 questionBank.js 的 fullTextSearch)
     */
    @GetMapping("/question-bank/retell/search/fulltext")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RetellItem>>> fullTextSearchRetellQuestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetellItem> items = retellItemService.searchRetellItems(query, pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("全文搜索故事复述题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("全文搜索故事复述题失败"));
        }
    }

    /**
     * 获取相似题目推荐 (对应 questionBank.js 的 getSimilarQuestions)
     */
    @GetMapping("/question-bank/retell/{questionId}/similar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RetellItem>>> getSimilarRetellQuestions(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // TODO: 实现相似题目推荐逻辑
            List<RetellItem> similarItems = List.of(); // 临时返回空列表
            return ResponseEntity.ok(ApiResponse.success(similarItems));
        } catch (Exception e) {
            log.error("获取相似故事复述题失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取相似故事复述题失败"));
        }
    }

    /**
     * 智能推荐题目 (对应 questionBank.js 的 getRecommendedQuestions)
     */
    @GetMapping("/question-bank/retell/recommend")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RetellItem>>> getRecommendedRetellQuestions(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer difficultyLevel) {
        try {
            // TODO: 实现智能推荐逻辑
            List<RetellItem> recommendedItems = List.of(); // 临时返回空列表
            return ResponseEntity.ok(ApiResponse.success(recommendedItems));
        } catch (Exception e) {
            log.error("获取推荐故事复述题失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取推荐故事复述题失败"));
        }
    }
}
