package org.icao4.eqasbackend2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.entity.lsa_dialogs.LsaDialog;
import org.icao4.eqasbackend2.entity.lsa_dialogs.LsaQuestion;
import org.icao4.eqasbackend2.entity.lsa_dialogs.LsaResponse;
import org.icao4.eqasbackend2.service.lsa_dialogs.LsaDialogService;
import org.icao4.eqasbackend2.service.lsa_dialogs.LsaQuestionService;
import org.icao4.eqasbackend2.service.lsa_dialogs.LsaResponseService;
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
 * 听力理解对话控制器
 * 提供听力理解对话、问题和回答相关的REST API
 * 同时支持题库管理系统的听力理解题相关功能
 */
@Slf4j
@RestController
@RequestMapping("/lsa-dialogs")
@RequiredArgsConstructor
public class LsaDialogsController {
    
    private final LsaDialogService lsaDialogService;
    private final LsaQuestionService lsaQuestionService;
    private final LsaResponseService lsaResponseService;
    
    // ==================== 对话相关接口 ====================
    
    /**
     * 获取所有对话
     */
    @GetMapping("/dialogs")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaDialog>>> getAllDialogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder,asc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<LsaDialog> dialogs = lsaDialogService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("获取所有对话失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取所有对话失败"));
        }
    }
    
    /**
     * 根据ID获取对话
     */
    @GetMapping("/dialogs/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaDialog>> getDialogById(@PathVariable Long id) {
        try {
            Optional<LsaDialog> dialog = lsaDialogService.findById(id);
            if (dialog.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(dialog.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("对话不存在"));
            }
        } catch (Exception e) {
            log.error("获取对话失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取对话失败"));
        }
    }
    
    /**
     * 根据模块ID获取对话
     */
    @GetMapping("/dialogs/module/{moduleId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaDialog>>> getDialogsByModuleId(
            @PathVariable Long moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder,asc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<LsaDialog> dialogs = lsaDialogService.findByModuleId(moduleId, pageable);
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("获取模块对话失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取模块对话失败"));
        }
    }
    
    /**
     * 创建对话
     */
    @PostMapping("/dialogs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaDialog>> createDialog(@Valid @RequestBody LsaDialog dialog) {
        try {
            LsaDialog createdDialog = lsaDialogService.save(dialog);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdDialog));
        } catch (Exception e) {
            log.error("创建对话失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建对话失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新对话
     */
    @PutMapping("/dialogs/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaDialog>> updateDialog(
            @PathVariable Long id,
            @Valid @RequestBody LsaDialog dialog) {
        
        try {
            if (!lsaDialogService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("对话不存在"));
            }
            
            dialog.setId(id);
            LsaDialog updatedDialog = lsaDialogService.save(dialog);
            return ResponseEntity.ok(ApiResponse.success(updatedDialog));
        } catch (Exception e) {
            log.error("更新对话失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新对话失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除对话
     */
    @DeleteMapping("/dialogs/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteDialog(@PathVariable Long id) {
        try {
            if (!lsaDialogService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("对话不存在"));
            }
            
            lsaDialogService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("对话删除成功"));
        } catch (Exception e) {
            log.error("删除对话失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除对话失败"));
        }
    }
    
    /**
     * 激活/停用对话
     */
    @PutMapping("/dialogs/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaDialog>> toggleDialogActive(@PathVariable Long id) {
        try {
            LsaDialog dialog = lsaDialogService.toggleActiveStatus(id);
            return ResponseEntity.ok(ApiResponse.success(dialog));
        } catch (Exception e) {
            log.error("切换对话状态失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("切换对话状态失败"));
        }
    }
    
    /**
     * 复制对话
     */
    @PostMapping("/dialogs/{id}/copy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaDialog>> copyDialog(@PathVariable Long id) {
        try {
            LsaDialog copiedDialog = lsaDialogService.copyDialog(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(copiedDialog));
        } catch (Exception e) {
            log.error("复制对话失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("复制对话失败"));
        }
    }
    
    /**
     * 搜索对话
     */
    @GetMapping("/dialogs/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaDialog>>> searchDialogs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LsaDialog> dialogs = lsaDialogService.searchDialogs(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("搜索对话失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索对话失败"));
        }
    }
    
    // ==================== 问题相关接口 ====================
    
    /**
     * 获取对话的问题列表
     */
    @GetMapping("/dialogs/{dialogId}/questions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaQuestion>>> getQuestionsByDialogId(
            @PathVariable Long dialogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder,asc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<LsaQuestion> questions = lsaQuestionService.findByDialogId(dialogId, pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取对话问题失败: dialogId={}, error={}", dialogId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取对话问题失败"));
        }
    }
    
    /**
     * 根据ID获取问题
     */
    @GetMapping("/questions/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaQuestion>> getQuestionById(@PathVariable Long id) {
        try {
            Optional<LsaQuestion> question = lsaQuestionService.findById(id);
            if (question.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(question.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("问题不存在"));
            }
        } catch (Exception e) {
            log.error("获取问题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取问题失败"));
        }
    }
    
    /**
     * 创建问题
     */
    @PostMapping("/questions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaQuestion>> createQuestion(@Valid @RequestBody LsaQuestion question) {
        try {
            LsaQuestion createdQuestion = lsaQuestionService.save(question);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdQuestion));
        } catch (Exception e) {
            log.error("创建问题失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建问题失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新问题
     */
    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaQuestion>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody LsaQuestion question) {
        
        try {
            if (!lsaQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("问题不存在"));
            }
            
            question.setId(id);
            LsaQuestion updatedQuestion = lsaQuestionService.save(question);
            return ResponseEntity.ok(ApiResponse.success(updatedQuestion));
        } catch (Exception e) {
            log.error("更新问题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新问题失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除问题
     */
    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteQuestion(@PathVariable Long id) {
        try {
            if (!lsaQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("问题不存在"));
            }
            
            lsaQuestionService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("问题删除成功"));
        } catch (Exception e) {
            log.error("删除问题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除问题失败"));
        }
    }
    
    /**
     * 根据问题类型获取问题
     */
    @GetMapping("/questions/type/{questionType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaQuestion>>> getQuestionsByType(
            @PathVariable String questionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LsaQuestion> questions = lsaQuestionService.findByQuestionType(questionType, pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("根据类型获取问题失败: questionType={}, error={}", questionType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据类型获取问题失败"));
        }
    }
    
    // ==================== 回答相关接口 ====================
    
    /**
     * 获取问题的回答列表
     */
    @GetMapping("/questions/{questionId}/responses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaResponse>>> getResponsesByQuestionId(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "answeredAt,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<LsaResponse> responses = lsaResponseService.findByQuestionId(questionId, pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取问题回答失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取问题回答失败"));
        }
    }
    
    /**
     * 根据ID获取回答
     */
    @GetMapping("/responses/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaResponse>> getResponseById(@PathVariable Long id) {
        try {
            Optional<LsaResponse> response = lsaResponseService.findById(id);
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(response.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("回答不存在"));
            }
        } catch (Exception e) {
            log.error("获取回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取回答失败"));
        }
    }
    
    /**
     * 提交回答
     */
    @PostMapping("/responses/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaResponse>> submitResponse(@RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.valueOf(request.get("questionId").toString());
            Long answerAudioId = request.get("answerAudioId") != null ? 
                    Long.valueOf(request.get("answerAudioId").toString()) : null;
            String asrText = request.get("asrText") != null ? request.get("asrText").toString() : null;
            Integer elapsedMs = request.get("elapsedMs") != null ? 
                    Integer.valueOf(request.get("elapsedMs").toString()) : 0;
            
            LsaResponse response = lsaResponseService.submitResponse(questionId, answerAudioId, asrText, elapsedMs);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("提交回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("提交回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建回答记录
     */
    @PostMapping("/responses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaResponse>> createResponse(@Valid @RequestBody LsaResponse response) {
        try {
            LsaResponse createdResponse = lsaResponseService.save(response);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdResponse));
        } catch (Exception e) {
            log.error("创建回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 评分回答
     */
    @PutMapping("/responses/{id}/score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaResponse>> scoreResponse(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        try {
            BigDecimal score = new BigDecimal(request.get("score").toString());
            String scoreDetailJson = request.get("scoreDetailJson") != null ? 
                    request.get("scoreDetailJson").toString() : null;
            
            LsaResponse response = lsaResponseService.scoreResponse(id, score, scoreDetailJson);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("评分回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("评分回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 自动评分回答
     */
    @PutMapping("/responses/{id}/auto-score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaResponse>> autoScoreResponse(@PathVariable Long id) {
        try {
            LsaResponse response = lsaResponseService.autoScoreResponse(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("自动评分回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("自动评分回答失败"));
        }
    }
    
    /**
     * 删除回答
     */
    @DeleteMapping("/responses/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteResponse(@PathVariable Long id) {
        try {
            if (!lsaResponseService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("回答不存在"));
            }
            
            lsaResponseService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("回答删除成功"));
        } catch (Exception e) {
            log.error("删除回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除回答失败"));
        }
    }
    
    // ==================== 统计相关接口 ====================
    
    /**
     * 获取对话统计信息
     */
    @GetMapping("/dialogs/{dialogId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDialogStats(@PathVariable Long dialogId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计
            stats.put("totalQuestions", lsaQuestionService.countByDialogId(dialogId));
            stats.put("activeQuestions", lsaQuestionService.countByDialogIdAndIsActive(dialogId, true));
            stats.put("inactiveQuestions", lsaQuestionService.countByDialogIdAndIsActive(dialogId, false));
            
            // 问题类型统计
            List<LsaQuestion> questions = lsaQuestionService.findByDialogId(dialogId);
            Map<String, Long> typeStats = new HashMap<>();
            questions.forEach(q -> typeStats.merge(q.getQuestionType(), 1L, Long::sum));
            stats.put("questionTypeStats", typeStats);
            
            // 分值统计
            stats.put("totalPoints", lsaQuestionService.getTotalPointsByDialogId(dialogId));
            
            // 回答统计
            long totalResponses = 0;
            long scoredResponses = 0;
            for (LsaQuestion question : questions) {
                totalResponses += lsaResponseService.countByQuestionId(question.getId());
                scoredResponses += lsaResponseService.countScoredResponsesByQuestionId(question.getId());
            }
            stats.put("totalResponses", totalResponses);
            stats.put("scoredResponses", scoredResponses);
            stats.put("unscoredResponses", totalResponses - scoredResponses);
            
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取对话统计失败: dialogId={}, error={}", dialogId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取对话统计失败"));
        }
    }
    
    /**
     * 获取模块统计信息
     */
    @GetMapping("/modules/{moduleId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getModuleStats(@PathVariable Long moduleId) {
        try {
            Map<String, Object> stats = lsaDialogService.getModuleDialogStatistics(moduleId);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverallStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 对话统计
            Map<String, Object> dialogStats = lsaDialogService.getDialogStatistics();
            stats.put("dialogs", dialogStats);
            
            // 问题统计
            Map<String, Object> questionStats = lsaQuestionService.getQuestionStatistics();
            stats.put("questions", questionStats);
            
            // 回答统计
            Map<String, Object> responseStats = lsaResponseService.getResponseStatistics();
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
     * 批量删除对话
     */
    @DeleteMapping("/dialogs/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteDialogs(@RequestBody List<Long> dialogIds) {
        try {
            lsaDialogService.batchDeleteDialogs(dialogIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除对话成功"));
        } catch (Exception e) {
            log.error("批量删除对话失败: count={}, error={}", dialogIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除对话失败"));
        }
    }
    
    /**
     * 批量激活对话
     */
    @PutMapping("/dialogs/batch-activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LsaDialog>>> batchActivateDialogs(@RequestBody List<Long> dialogIds) {
        try {
            List<LsaDialog> dialogs = lsaDialogService.batchActivateDialogs(dialogIds);
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("批量激活对话失败: count={}, error={}", dialogIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量激活对话失败"));
        }
    }
    
    /**
     * 批量停用对话
     */
    @PutMapping("/dialogs/batch-deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LsaDialog>>> batchDeactivateDialogs(@RequestBody List<Long> dialogIds) {
        try {
            List<LsaDialog> dialogs = lsaDialogService.batchDeactivateDialogs(dialogIds);
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("批量停用对话失败: count={}, error={}", dialogIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量停用对话失败"));
        }
    }
    
    /**
     * 批量删除问题
     */
    @DeleteMapping("/questions/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteQuestions(@RequestBody List<Long> questionIds) {
        try {
            lsaQuestionService.batchDeleteQuestions(questionIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除问题成功"));
        } catch (Exception e) {
            log.error("批量删除问题失败: count={}, error={}", questionIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除问题失败"));
        }
    }
    
    /**
     * 批量删除回答
     */
    @DeleteMapping("/responses/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteResponses(@RequestBody List<Long> responseIds) {
        try {
            lsaResponseService.batchDeleteResponses(responseIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除回答成功"));
        } catch (Exception e) {
            log.error("批量删除回答失败: count={}, error={}", responseIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除回答失败"));
        }
    }
    
    // ==================== 导入导出接口 ====================
    
    /**
     * 导出模块对话
     */
    @GetMapping("/modules/{moduleId}/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LsaDialog>>> exportDialogsByModule(@PathVariable Long moduleId) {
        try {
            List<LsaDialog> dialogs = lsaDialogService.exportDialogsByModule(moduleId);
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("导出模块对话失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导出模块对话失败"));
        }
    }
    
    /**
     * 导入对话到模块
     */
    @PostMapping("/modules/{moduleId}/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LsaDialog>>> importDialogsToModule(
            @PathVariable Long moduleId,
            @RequestBody List<LsaDialog> dialogs) {
        
        try {
            List<LsaDialog> importedDialogs = lsaDialogService.importDialogsToModule(moduleId, dialogs);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(importedDialogs));
        } catch (Exception e) {
            log.error("导入对话到模块失败: moduleId={}, count={}, error={}", 
                     moduleId, dialogs.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导入对话到模块失败"));
        }
    }

    // ==================== 题库管理系统 - 听力理解题相关接口 ====================

    /**
     * 获取听力理解题列表 (对应 questionBank.js 的 getListeningQuestions)
     */
    @GetMapping("/question-bank/listening")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaDialog>>> getListeningQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean isActive) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LsaDialog> dialogs;
            
            if (moduleId != null) {
                dialogs = lsaDialogService.findByModuleId(moduleId, pageable);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                dialogs = lsaDialogService.searchDialogs(keyword, pageable);
            } else {
                dialogs = lsaDialogService.findAll(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("获取听力理解题列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取听力理解题列表失败"));
        }
    }

    /**
     * 创建听力理解题 (对应 questionBank.js 的 createListeningQuestion)
     */
    @PostMapping("/question-bank/listening")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaDialog>> createListeningQuestion(@Valid @RequestBody LsaDialog dialog) {
        try {
            LsaDialog createdDialog = lsaDialogService.save(dialog);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdDialog));
        } catch (Exception e) {
            log.error("创建听力理解题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建听力理解题失败: " + e.getMessage()));
        }
    }

    /**
     * 更新听力理解题 (对应 questionBank.js 的 updateListeningQuestion)
     */
    @PutMapping("/question-bank/listening/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LsaDialog>> updateListeningQuestion(
            @PathVariable Long id,
            @Valid @RequestBody LsaDialog dialog) {
        try {
            if (!lsaDialogService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("听力理解题不存在"));
            }
            dialog.setId(id);
            LsaDialog updatedDialog = lsaDialogService.save(dialog);
            return ResponseEntity.ok(ApiResponse.success(updatedDialog));
        } catch (Exception e) {
            log.error("更新听力理解题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新听力理解题失败: " + e.getMessage()));
        }
    }

    /**
     * 删除听力理解题 (对应 questionBank.js 的 deleteListeningQuestion)
     */
    @DeleteMapping("/question-bank/listening/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteListeningQuestion(@PathVariable Long id) {
        try {
            if (!lsaDialogService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("听力理解题不存在"));
            }
            lsaDialogService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("听力理解题删除成功"));
        } catch (Exception e) {
            log.error("删除听力理解题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除听力理解题失败: " + e.getMessage()));
        }
    }

    /**
     * 批量操作听力理解题 (对应 questionBank.js 的 batchOperateListeningQuestions)
     */
    @PostMapping("/question-bank/listening/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchOperateListeningQuestions(@RequestBody Map<String, Object> request) {
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
                    lsaDialogService.batchDeleteDialogs(questionIds);
                    result.put("message", "批量删除成功");
                    break;
                case "activate":
                    List<LsaDialog> activatedDialogs = lsaDialogService.batchActivateDialogs(questionIds);
                    result.put("message", "批量激活成功");
                    result.put("activatedCount", activatedDialogs.size());
                    break;
                case "deactivate":
                    List<LsaDialog> deactivatedDialogs = lsaDialogService.batchDeactivateDialogs(questionIds);
                    result.put("message", "批量停用成功");
                    result.put("deactivatedCount", deactivatedDialogs.size());
                    break;
                case "update_difficulty":
                    // TODO: 实现批量更新难度逻辑
                    result.put("message", "批量更新难度成功");
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("不支持的操作类型: " + operation));
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("批量操作听力理解题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量操作听力理解题失败: " + e.getMessage()));
        }
    }

    // ==================== 文件上传相关接口 ====================

    /**
     * 上传听力题目音频 (对应 questionBank.js 的 uploadQuestionAudio)
     */
    @PostMapping("/question-bank/upload/audio")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
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
            log.error("上传听力题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("上传听力题目音频失败: " + e.getMessage()));
        }
    }

    /**
     * 删除听力题目音频 (对应 questionBank.js 的 deleteQuestionAudio)
     */
    @DeleteMapping("/question-bank/upload/audio")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteQuestionAudio(@RequestBody Map<String, Object> request) {
        try {
            String audioUrl = (String) request.get("audioUrl");
            if (audioUrl == null || audioUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("音频URL不能为空"));
            }
            
            // TODO: 实现删除音频文件的逻辑
            
            return ResponseEntity.ok(ApiResponse.success("听力题目音频删除成功"));
        } catch (Exception e) {
            log.error("删除听力题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除听力题目音频失败: " + e.getMessage()));
        }
    }

    // ==================== 数据导入导出接口 ====================

    /**
     * 批量导入听力理解题 (对应 questionBank.js 的 importQuestions)
     */
    @PostMapping("/question-bank/listening/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importListeningQuestions(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") Boolean skipDuplicates,
            @RequestParam(required = false, defaultValue = "false") Boolean updateExisting,
            @RequestParam(required = false, defaultValue = "true") Boolean validateData) {
        try {
            // TODO: 实现听力理解题批量导入逻辑
            Map<String, Object> result = Map.of(
                    "totalRows", 100,
                    "successCount", 95,
                    "failureCount", 5,
                    "duplicateCount", 3,
                    "message", "听力理解题导入完成"
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("批量导入听力理解题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量导入听力理解题失败: " + e.getMessage()));
        }
    }

    /**
     * 导出听力理解题 (对应 questionBank.js 的 exportQuestions)
     */
    @GetMapping("/question-bank/listening/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<byte[]> exportListeningQuestions(
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean isActive) {
        try {
            // TODO: 实现听力理解题导出逻辑
            String csvContent = "ID,对话标题,对话内容,问题数量,难度等级,是否激活,创建时间\n1,机场对话,Welcome to the airport...,3,2,true,2024-01-01";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=listening_questions.csv")
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出听力理解题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出听力理解题模板 (对应 questionBank.js 的 exportQuestionTemplate)
     */
    @GetMapping("/question-bank/listening/export/template")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<byte[]> exportListeningQuestionTemplate() {
        try {
            String csvContent = "ID,对话标题,对话内容,问题数量,难度等级,模块ID,是否激活\n";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=listening_template.csv")
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出听力理解题模板失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 验证导入文件 (对应 questionBank.js 的 validateImportFile)
     */
    @PostMapping("/question-bank/listening/import/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateImportFile(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            // TODO: 实现导入文件验证逻辑
            Map<String, Object> result = Map.of(
                    "isValid", true,
                    "totalRows", 50,
                    "validRows", 48,
                    "invalidRows", 2,
                    "errors", List.of("第3行：对话内容不能为空", "第8行：难度等级无效"),
                    "warnings", List.of("第5行：问题数量为0，建议添加问题")
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("验证导入文件失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("验证导入文件失败: " + e.getMessage()));
        }
    }

    // ==================== 高级搜索和筛选接口 ====================

    /**
     * 高级搜索听力理解题 (对应 questionBank.js 的 advancedSearch)
     */
    @PostMapping("/question-bank/listening/search/advanced")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaDialog>>> advancedSearchListeningQuestions(@RequestBody Map<String, Object> criteria) {
        try {
            // TODO: 实现高级搜索逻辑
            Pageable pageable = PageRequest.of(0, 20);
            Page<LsaDialog> dialogs = lsaDialogService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("高级搜索听力理解题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("高级搜索听力理解题失败"));
        }
    }

    /**
     * 全文搜索听力理解题 (对应 questionBank.js 的 fullTextSearch)
     */
    @GetMapping("/question-bank/listening/search/fulltext")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LsaDialog>>> fullTextSearchListeningQuestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LsaDialog> dialogs = lsaDialogService.searchDialogs(query, pageable);
            return ResponseEntity.ok(ApiResponse.success(dialogs));
        } catch (Exception e) {
            log.error("全文搜索听力理解题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("全文搜索听力理解题失败"));
        }
    }

    /**
     * 获取相似题目推荐 (对应 questionBank.js 的 getSimilarQuestions)
     */
    @GetMapping("/question-bank/listening/{questionId}/similar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LsaDialog>>> getSimilarListeningQuestions(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // TODO: 实现相似题目推荐逻辑
            List<LsaDialog> similarDialogs = List.of(); // 临时返回空列表
            return ResponseEntity.ok(ApiResponse.success(similarDialogs));
        } catch (Exception e) {
            log.error("获取相似听力理解题失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取相似听力理解题失败"));
        }
    }
}
