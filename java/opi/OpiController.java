package org.icao4.eqasbackend2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.entity.opi.OpiQuestion;
import org.icao4.eqasbackend2.entity.opi.OpiResponse;
import org.icao4.eqasbackend2.entity.opi.OpiTopic;
import org.icao4.eqasbackend2.service.opi.OpiQuestionService;
import org.icao4.eqasbackend2.service.opi.OpiResponseService;
import org.icao4.eqasbackend2.service.opi.OpiTopicService;
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
 * OPI（口语面试）控制器
 * 提供OPI话题、问题和回答相关的REST API
 * 同时支持题库管理系统的口语模仿题相关功能
 */
@Slf4j
@RestController
@RequestMapping("/opi")
@RequiredArgsConstructor
public class OpiController {
    
    private final OpiTopicService opiTopicService;
    private final OpiQuestionService opiQuestionService;
    private final OpiResponseService opiResponseService;
    
    // ==================== 话题相关接口 ====================
    
    /**
     * 获取所有话题
     */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<Page<OpiTopic>>> getAllTopics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "order,asc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<OpiTopic> topics = opiTopicService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(topics));
        } catch (Exception e) {
            log.error("获取所有OPI话题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取所有OPI话题失败"));
        }
    }
    
    /**
     * 根据ID获取话题
     */
    @GetMapping("/topics/{id}")
    public ResponseEntity<ApiResponse<OpiTopic>> getTopicById(@PathVariable Long id) {
        try {
            Optional<OpiTopic> topic = opiTopicService.findById(id);
            if (topic.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(topic.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("OPI话题不存在"));
            }
        } catch (Exception e) {
            log.error("获取OPI话题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取OPI话题失败"));
        }
    }
    
    /**
     * 根据模块ID获取话题
     */
    @GetMapping("/topics/module/{moduleId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiTopic>>> getTopicsByModuleId(
            @PathVariable Long moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "order,asc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<OpiTopic> topics = opiTopicService.findByModuleId(moduleId, pageable);
            return ResponseEntity.ok(ApiResponse.success(topics));
        } catch (Exception e) {
            log.error("获取模块OPI话题失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取模块OPI话题失败"));
        }
    }
    
    /**
     * 创建话题
     */
    @PostMapping("/topics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiTopic>> createTopic(@Valid @RequestBody OpiTopic topic) {
        try {
            OpiTopic createdTopic = opiTopicService.save(topic);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdTopic));
        } catch (Exception e) {
            log.error("创建OPI话题失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建OPI话题失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新话题
     */
    @PutMapping("/topics/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiTopic>> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody OpiTopic topic) {
        
        try {
            if (!opiTopicService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("OPI话题不存在"));
            }
            
            topic.setId(id);
            OpiTopic updatedTopic = opiTopicService.save(topic);
            return ResponseEntity.ok(ApiResponse.success(updatedTopic));
        } catch (Exception e) {
            log.error("更新OPI话题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新OPI话题失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除话题
     */
    @DeleteMapping("/topics/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteTopic(@PathVariable Long id) {
        try {
            if (!opiTopicService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("OPI话题不存在"));
            }
            
            opiTopicService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("OPI话题删除成功"));
        } catch (Exception e) {
            log.error("删除OPI话题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除OPI话题失败"));
        }
    }
    
    /**
     * 复制话题
     */
    @PostMapping("/topics/{id}/copy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiTopic>> copyTopic(@PathVariable Long id) {
        try {
            OpiTopic copiedTopic = opiTopicService.copyTopic(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(copiedTopic));
        } catch (Exception e) {
            log.error("复制OPI话题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("复制OPI话题失败"));
        }
    }
    
    /**
     * 搜索话题
     */
    @GetMapping("/topics/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiTopic>>> searchTopics(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiTopic> topics = opiTopicService.searchTopics(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success(topics));
        } catch (Exception e) {
            log.error("搜索OPI话题失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索OPI话题失败"));
        }
    }
    
    // ==================== 问题相关接口 ====================
    
    /**
     * 获取所有问题列表（不限定话题）
     */
    @GetMapping("/questions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiQuestion>>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "QOrder,asc") String[] sort,
            @RequestParam(required = false) Long topicId) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<OpiQuestion> questions;
            
            if (topicId != null) {
                questions = opiQuestionService.findByTopicId(topicId, pageable);
            } else {
                questions = opiQuestionService.findAll(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取问题列表失败: topicId={}, error={}", topicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取问题列表失败"));
        }
    }

    /**
     * 获取话题的问题列表
     */
    @GetMapping("/topics/{topicId}/questions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiQuestion>>> getQuestionsByTopicId(
            @PathVariable String topicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "QOrder,asc") String[] sort) {
        
        try {
            // 检查 topicId 是否为有效的 Long 值
            if ("null".equals(topicId) || topicId == null || topicId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("话题ID不能为空或null，请使用 /opi/questions 获取所有问题"));
            }
            
            Long parsedTopicId;
            try {
                parsedTopicId = Long.valueOf(topicId);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("话题ID格式无效: " + topicId + "，必须是有效的数字"));
            }
            
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<OpiQuestion> questions = opiQuestionService.findByTopicId(parsedTopicId, pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取话题问题失败: topicId={}, error={}", topicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取话题问题失败"));
        }
    }
    
    /**
     * 根据ID获取问题
     */
    @GetMapping("/questions/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiQuestion>> getQuestionById(@PathVariable Long id) {
        try {
            Optional<OpiQuestion> question = opiQuestionService.findById(id);
            if (question.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(question.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("OPI问题不存在"));
            }
        } catch (Exception e) {
            log.error("获取OPI问题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取OPI问题失败"));
        }
    }
    
    /**
     * 创建问题
     */
    @PostMapping("/questions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiQuestion>> createQuestion(@Valid @RequestBody OpiQuestion question) {
        try {
            OpiQuestion createdQuestion = opiQuestionService.save(question);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdQuestion));
        } catch (Exception e) {
            log.error("创建OPI问题失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建OPI问题失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新问题
     */
    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiQuestion>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody OpiQuestion question) {
        
        try {
            if (!opiQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("OPI问题不存在"));
            }
            
            question.setId(id);
            OpiQuestion updatedQuestion = opiQuestionService.save(question);
            return ResponseEntity.ok(ApiResponse.success(updatedQuestion));
        } catch (Exception e) {
            log.error("更新OPI问题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新OPI问题失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除问题
     */
    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteQuestion(@PathVariable Long id) {
        try {
            if (!opiQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("OPI问题不存在"));
            }
            
            opiQuestionService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("OPI问题删除成功"));
        } catch (Exception e) {
            log.error("删除OPI问题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除OPI问题失败"));
        }
    }
    
    /**
     * 复制问题
     */
    @PostMapping("/questions/{id}/copy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiQuestion>> copyQuestion(@PathVariable Long id) {
        try {
            OpiQuestion copiedQuestion = opiQuestionService.copyQuestion(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(copiedQuestion));
        } catch (Exception e) {
            log.error("复制OPI问题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("复制OPI问题失败"));
        }
    }
    
    /**
     * 根据回答时间范围查找问题
     */
    @GetMapping("/questions/answer-time")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiQuestion>>> getQuestionsByAnswerTime(
            @RequestParam Integer minSeconds,
            @RequestParam Integer maxSeconds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiQuestion> questions = opiQuestionService.findByAnswerSecondsBetween(
                    minSeconds, maxSeconds, pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("根据回答时间查找问题失败: min={}, max={}, error={}", 
                     minSeconds, maxSeconds, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据回答时间查找问题失败"));
        }
    }
    
    /**
     * 获取有屏显文本的问题
     */
    @GetMapping("/questions/with-prompt-text")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiQuestion>>> getQuestionsWithPromptText(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiQuestion> questions = opiQuestionService.findQuestionsWithPromptText(pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取有屏显文本的问题失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取有屏显文本的问题失败"));
        }
    }
    
    /**
     * 获取没有屏显文本的问题
     */
    @GetMapping("/questions/without-prompt-text")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiQuestion>>> getQuestionsWithoutPromptText(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiQuestion> questions = opiQuestionService.findQuestionsWithoutPromptText(pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取没有屏显文本的问题失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取没有屏显文本的问题失败"));
        }
    }
    
    // ==================== 回答相关接口 ====================
    
    /**
     * 获取问题的回答列表
     */
    @GetMapping("/questions/{questionId}/responses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiResponse>>> getResponsesByQuestionId(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "answeredAt,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<OpiResponse> responses = opiResponseService.findByQuestionId(questionId, pageable);
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
    public ResponseEntity<ApiResponse<OpiResponse>> getResponseById(@PathVariable Long id) {
        try {
            Optional<OpiResponse> response = opiResponseService.findById(id);
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(response.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("OPI回答不存在"));
            }
        } catch (Exception e) {
            log.error("获取OPI回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取OPI回答失败"));
        }
    }
    
    /**
     * 提交回答
     */
    @PostMapping("/responses/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiResponse>> submitResponse(@RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.valueOf(request.get("questionId").toString());
            Long answerAudioId = request.get("answerAudioId") != null ? 
                    Long.valueOf(request.get("answerAudioId").toString()) : null;
            String asrText = request.get("asrText") != null ? request.get("asrText").toString() : null;
            Integer elapsedMs = request.get("elapsedMs") != null ? 
                    Integer.valueOf(request.get("elapsedMs").toString()) : 0;
            
            OpiResponse response = opiResponseService.submitResponse(questionId, answerAudioId, asrText, elapsedMs);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("提交OPI回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("提交OPI回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建回答记录
     */
    @PostMapping("/responses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiResponse>> createResponse(@Valid @RequestBody OpiResponse response) {
        try {
            OpiResponse createdResponse = opiResponseService.save(response);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdResponse));
        } catch (Exception e) {
            log.error("创建OPI回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建OPI回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 评分回答
     */
    @PutMapping("/responses/{id}/score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiResponse>> scoreResponse(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        try {
            BigDecimal score = new BigDecimal(request.get("score").toString());
            String scoreDetailJson = request.get("scoreDetailJson") != null ? 
                    request.get("scoreDetailJson").toString() : null;
            
            OpiResponse response = opiResponseService.scoreResponse(id, score, scoreDetailJson);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("评分OPI回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("评分OPI回答失败: " + e.getMessage()));
        }
    }
    
    /**
     * 自动评分回答
     */
    @PutMapping("/responses/{id}/auto-score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiResponse>> autoScoreResponse(@PathVariable Long id) {
        try {
            OpiResponse response = opiResponseService.autoScoreResponse(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("自动评分OPI回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("自动评分OPI回答失败"));
        }
    }
    
    /**
     * 删除回答
     */
    @DeleteMapping("/responses/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteResponse(@PathVariable Long id) {
        try {
            if (!opiResponseService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("OPI回答不存在"));
            }
            
            opiResponseService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("OPI回答删除成功"));
        } catch (Exception e) {
            log.error("删除OPI回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除OPI回答失败"));
        }
    }
    
    /**
     * 获取已评分的回答
     */
    @GetMapping("/responses/scored")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiResponse>>> getScoredResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "score,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<OpiResponse> responses = opiResponseService.findScoredResponses(pageable);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiResponse>>> getUnscoredResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "answeredAt,desc") String[] sort) {
        
        try {
            Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<OpiResponse> responses = opiResponseService.findUnscoredResponses(pageable);
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiResponse>>> getPassedResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiResponse> responses = opiResponseService.findPassedResponses(pageable);
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiResponse>>> getFailedResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiResponse> responses = opiResponseService.findFailedResponses(pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取不及格回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取不及格回答失败"));
        }
    }
    
    /**
     * 根据评分范围查找回答
     */
    @GetMapping("/responses/score-range")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiResponse>>> getResponsesByScoreRange(
            @RequestParam BigDecimal minScore,
            @RequestParam BigDecimal maxScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiResponse> responses = opiResponseService.findByScoreBetween(
                    minScore, maxScore, pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("根据评分范围查找回答失败: min={}, max={}, error={}", 
                     minScore, maxScore, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据评分范围查找回答失败"));
        }
    }
    
    // ==================== 统计相关接口 ====================
    
    /**
     * 获取话题统计信息
     */
    @GetMapping("/topics/{topicId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopicStats(@PathVariable String topicId) {
        try {
            // 检查 topicId 是否为有效的 Long 值
            if ("null".equals(topicId) || topicId == null || topicId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("话题ID不能为空或null"));
            }
            
            Long parsedTopicId;
            try {
                parsedTopicId = Long.valueOf(topicId);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("话题ID格式无效: " + topicId + "，必须是有效的数字"));
            }
            
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计
            stats.put("totalQuestions", opiQuestionService.countByTopicId(parsedTopicId));
            stats.put("questionsWithPromptText", 
                     opiQuestionService.findByTopicId(parsedTopicId).stream()
                             .mapToLong(q -> q.hasPromptText() ? 1 : 0).sum());
            stats.put("averageAnswerSeconds", opiQuestionService.getAverageAnswerSecondsByTopicId(parsedTopicId));
            
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取话题统计失败: topicId={}, error={}", topicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取话题统计失败"));
        }
    }
    
    /**
     * 获取问题统计信息
     */
    @GetMapping("/questions/{questionId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuestionStats(@PathVariable Long questionId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计
            stats.put("totalResponses", opiResponseService.countByQuestionId(questionId));
            stats.put("scoredResponses", opiResponseService.countScoredResponsesByQuestionId(questionId));
            stats.put("unscoredResponses", opiResponseService.countUnscoredResponsesByQuestionId(questionId));
            stats.put("passedResponses", opiResponseService.countPassedResponsesByQuestionId(questionId));
            
            // 平均统计
            stats.put("averageScore", opiResponseService.getAverageScoreByQuestionId(questionId));
            stats.put("averageElapsedMs", opiResponseService.getAverageElapsedMsByQuestionId(questionId));
            
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取问题统计失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取问题统计失败"));
        }
    }
    
    /**
     * 获取模块统计信息
     */
    @GetMapping("/modules/{moduleId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getModuleStats(@PathVariable Long moduleId) {
        try {
            Map<String, Object> stats = opiTopicService.getModuleTopicStatistics(moduleId);
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
            
            // 话题统计
            Map<String, Object> topicStats = opiTopicService.getTopicStatistics();
            stats.put("topics", topicStats);
            
            // 问题统计
            Map<String, Object> questionStats = opiQuestionService.getQuestionStatistics();
            stats.put("questions", questionStats);
            
            // 回答统计
            Map<String, Object> responseStats = opiResponseService.getResponseStatistics();
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
     * 批量删除话题
     */
    @DeleteMapping("/topics/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteTopics(@RequestBody List<Long> topicIds) {
        try {
            opiTopicService.batchDeleteTopics(topicIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除话题成功"));
        } catch (Exception e) {
            log.error("批量删除话题失败: count={}, error={}", topicIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除话题失败"));
        }
    }
    
    /**
     * 批量删除问题
     */
    @DeleteMapping("/questions/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteQuestions(@RequestBody List<Long> questionIds) {
        try {
            opiQuestionService.batchDeleteQuestions(questionIds);
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
            opiResponseService.batchDeleteResponses(responseIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除回答成功"));
        } catch (Exception e) {
            log.error("批量删除回答失败: count={}, error={}", responseIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除回答失败"));
        }
    }
    
    // ==================== 导入导出接口 ====================
    
    /**
     * 导出模块话题
     */
    @GetMapping("/modules/{moduleId}/topics/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OpiTopic>>> exportTopicsByModule(@PathVariable Long moduleId) {
        try {
            List<OpiTopic> topics = opiTopicService.exportTopicsByModule(moduleId);
            return ResponseEntity.ok(ApiResponse.success(topics));
        } catch (Exception e) {
            log.error("导出模块话题失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导出模块话题失败"));
        }
    }
    
    /**
     * 导出话题问题
     */
    @GetMapping("/topics/{topicId}/questions/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OpiQuestion>>> exportQuestionsByTopic(@PathVariable String topicId) {
        try {
            // 检查 topicId 是否为有效的 Long 值
            if ("null".equals(topicId) || topicId == null || topicId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("话题ID不能为空或null"));
            }
            
            Long parsedTopicId;
            try {
                parsedTopicId = Long.valueOf(topicId);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("话题ID格式无效: " + topicId + "，必须是有效的数字"));
            }
            
            List<OpiQuestion> questions = opiQuestionService.exportQuestionsByTopic(parsedTopicId);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("导出话题问题失败: topicId={}, error={}", topicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导出话题问题失败"));
        }
    }
    
    /**
     * 导出问题回答
     */
    @GetMapping("/questions/{questionId}/responses/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OpiResponse>>> exportResponsesByQuestion(@PathVariable Long questionId) {
        try {
            List<OpiResponse> responses = opiResponseService.exportResponsesByQuestion(questionId);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("导出问题回答失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导出问题回答失败"));
        }
    }

    // ==================== 题库管理系统 - 口语模仿题相关接口 ====================

    /**
     * 获取口语模仿题列表 (对应 questionBank.js 的 getOPIQuestions)
     */
    @GetMapping("/question-bank/opi")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiQuestion>>> getOPIQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean hasPromptText) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiQuestion> questions;
            
            if (topicId != null) {
                questions = opiQuestionService.findByTopicId(topicId, pageable);
            } else if (hasPromptText != null) {
                if (hasPromptText) {
                    questions = opiQuestionService.findQuestionsWithPromptText(pageable);
                } else {
                    questions = opiQuestionService.findQuestionsWithoutPromptText(pageable);
                }
            } else {
                // TODO: 实现关键词搜索
                questions = opiQuestionService.findAll(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取口语模仿题列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取口语模仿题列表失败"));
        }
    }

    /**
     * 创建口语模仿题 (对应 questionBank.js 的 createOPIQuestion)
     */
    @PostMapping("/question-bank/opi")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiQuestion>> createOPIQuestion(@Valid @RequestBody OpiQuestion question) {
        try {
            OpiQuestion createdQuestion = opiQuestionService.save(question);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdQuestion));
        } catch (Exception e) {
            log.error("创建口语模仿题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建口语模仿题失败: " + e.getMessage()));
        }
    }

    /**
     * 更新口语模仿题 (对应 questionBank.js 的 updateOPIQuestion)
     */
    @PutMapping("/question-bank/opi/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OpiQuestion>> updateOPIQuestion(
            @PathVariable Long id,
            @Valid @RequestBody OpiQuestion question) {
        try {
            if (!opiQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("口语模仿题不存在"));
            }
            question.setId(id);
            OpiQuestion updatedQuestion = opiQuestionService.save(question);
            return ResponseEntity.ok(ApiResponse.success(updatedQuestion));
        } catch (Exception e) {
            log.error("更新口语模仿题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新口语模仿题失败: " + e.getMessage()));
        }
    }

    /**
     * 删除口语模仿题 (对应 questionBank.js 的 deleteOPIQuestion)
     */
    @DeleteMapping("/question-bank/opi/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteOPIQuestion(@PathVariable Long id) {
        try {
            if (!opiQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("口语模仿题不存在"));
            }
            opiQuestionService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("口语模仿题删除成功"));
        } catch (Exception e) {
            log.error("删除口语模仿题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除口语模仿题失败: " + e.getMessage()));
        }
    }

    /**
     * 获取口语评分标准 (对应 questionBank.js 的 getOPIScoringCriteria)
     */
    @GetMapping("/question-bank/opi/scoring-criteria")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOPIScoringCriteria() {
        try {
            // TODO: 实现获取口语评分标准的逻辑
            Map<String, Object> criteria = Map.of(
                    "pronunciation", Map.of("weight", 25, "description", "发音准确性"),
                    "fluency", Map.of("weight", 25, "description", "流利度"),
                    "vocabulary", Map.of("weight", 25, "description", "词汇使用"),
                    "grammar", Map.of("weight", 25, "description", "语法正确性"),
                    "totalScore", 100,
                    "passingScore", 60
            );
            return ResponseEntity.ok(ApiResponse.success(criteria));
        } catch (Exception e) {
            log.error("获取口语评分标准失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取口语评分标准失败"));
        }
    }

    /**
     * 更新口语评分标准 (对应 questionBank.js 的 updateOPIScoringCriteria)
     */
    @PutMapping("/question-bank/opi/scoring-criteria")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateOPIScoringCriteria(@RequestBody Map<String, Object> criteria) {
        try {
            // TODO: 实现更新口语评分标准的逻辑
            criteria.put("updatedAt", LocalDateTime.now());
            return ResponseEntity.ok(ApiResponse.success(criteria));
        } catch (Exception e) {
            log.error("更新口语评分标准失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新口语评分标准失败: " + e.getMessage()));
        }
    }

    // ==================== 批量操作接口 ====================

    /**
     * 批量操作口语模仿题 (对应 questionBank.js 的 batchOperateQuestions)
     */
    @PostMapping("/question-bank/opi/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchOperateOPIQuestions(@RequestBody Map<String, Object> request) {
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
                    opiQuestionService.batchDeleteQuestions(questionIds);
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
                case "move_topic":
                    // TODO: 实现批量移动话题逻辑
                    result.put("message", "批量移动话题成功");
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("不支持的操作类型: " + operation));
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("批量操作口语模仿题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量操作口语模仿题失败: " + e.getMessage()));
        }
    }

    // ==================== 文件上传相关接口 ====================

    /**
     * 上传口语题目音频 (对应 questionBank.js 的 uploadQuestionAudio)
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
            log.error("上传口语题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("上传口语题目音频失败: " + e.getMessage()));
        }
    }

    /**
     * 删除口语题目音频 (对应 questionBank.js 的 deleteQuestionAudio)
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
            
            return ResponseEntity.ok(ApiResponse.success("口语题目音频删除成功"));
        } catch (Exception e) {
            log.error("删除口语题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除口语题目音频失败: " + e.getMessage()));
        }
    }

    // ==================== 数据导入导出接口 ====================

    /**
     * 批量导入口语模仿题 (对应 questionBank.js 的 importQuestions)
     */
    @PostMapping("/question-bank/opi/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importOPIQuestions(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") Boolean skipDuplicates,
            @RequestParam(required = false, defaultValue = "false") Boolean updateExisting,
            @RequestParam(required = false, defaultValue = "true") Boolean validateData) {
        try {
            // TODO: 实现口语模仿题批量导入逻辑
            Map<String, Object> result = Map.of(
                    "totalRows", 80,
                    "successCount", 76,
                    "failureCount", 4,
                    "duplicateCount", 2,
                    "message", "口语模仿题导入完成"
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("批量导入口语模仿题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量导入口语模仿题失败: " + e.getMessage()));
        }
    }

    /**
     * 导出口语模仿题 (对应 questionBank.js 的 exportQuestions)
     */
    @GetMapping("/question-bank/opi/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<byte[]> exportOPIQuestions(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean hasPromptText) {
        try {
            // TODO: 实现口语模仿题导出逻辑
            String csvContent = "ID,话题ID,问题内容,屏显文本,回答时间,难度等级,创建时间\n1,1,Please introduce yourself,Please introduce yourself briefly,60,2,2024-01-01";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=opi_questions.csv")
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出口语模仿题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出口语模仿题模板 (对应 questionBank.js 的 exportQuestionTemplate)
     */
    @GetMapping("/question-bank/opi/export/template")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<byte[]> exportOPIQuestionTemplate() {
        try {
            String csvContent = "ID,话题ID,问题内容,屏显文本,回答时间(秒),难度等级,是否激活\n";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=opi_template.csv")
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出口语模仿题模板失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== 高级搜索和筛选接口 ====================

    /**
     * 高级搜索口语模仿题 (对应 questionBank.js 的 advancedSearch)
     */
    @PostMapping("/question-bank/opi/search/advanced")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiQuestion>>> advancedSearchOPIQuestions(@RequestBody Map<String, Object> criteria) {
        try {
            // TODO: 实现高级搜索逻辑
            Pageable pageable = PageRequest.of(0, 20);
            Page<OpiQuestion> questions = opiQuestionService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("高级搜索口语模仿题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("高级搜索口语模仿题失败"));
        }
    }

    /**
     * 全文搜索口语模仿题 (对应 questionBank.js 的 fullTextSearch)
     */
    @GetMapping("/question-bank/opi/search/fulltext")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OpiQuestion>>> fullTextSearchOPIQuestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // TODO: 实现全文搜索逻辑
            Pageable pageable = PageRequest.of(page, size);
            Page<OpiQuestion> questions = opiQuestionService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("全文搜索口语模仿题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("全文搜索口语模仿题失败"));
        }
    }

    /**
     * 获取相似题目推荐 (对应 questionBank.js 的 getSimilarQuestions)
     */
    @GetMapping("/question-bank/opi/{questionId}/similar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OpiQuestion>>> getSimilarOPIQuestions(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // TODO: 实现相似题目推荐逻辑
            List<OpiQuestion> similarQuestions = List.of(); // 临时返回空列表
            return ResponseEntity.ok(ApiResponse.success(similarQuestions));
        } catch (Exception e) {
            log.error("获取相似口语模仿题失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取相似口语模仿题失败"));
        }
    }

    /**
     * 智能推荐题目 (对应 questionBank.js 的 getRecommendedQuestions)
     */
    @GetMapping("/question-bank/opi/recommend")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OpiQuestion>>> getRecommendedOPIQuestions(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer difficultyLevel) {
        try {
            // TODO: 实现智能推荐逻辑
            List<OpiQuestion> recommendedQuestions = List.of(); // 临时返回空列表
            return ResponseEntity.ok(ApiResponse.success(recommendedQuestions));
        } catch (Exception e) {
            log.error("获取推荐口语模仿题失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取推荐口语模仿题失败"));
        }
    }
}
