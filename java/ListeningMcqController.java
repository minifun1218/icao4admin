package org.icao4.eqasbackend2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.entity.listening_mcq.McqChoice;
import org.icao4.eqasbackend2.entity.listening_mcq.McqQuestion;
import org.icao4.eqasbackend2.entity.listening_mcq.McqResponse;
import org.icao4.eqasbackend2.service.listening_mcq.McqChoiceService;
import org.icao4.eqasbackend2.service.listening_mcq.McqQuestionService;
import org.icao4.eqasbackend2.service.listening_mcq.McqResponseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 听力理解选择题控制器
 * 提供听力理解选择题相关的REST API
 * 同时支持题库管理系统的听力选择题相关功能
 */
@Slf4j
@RestController
@RequestMapping("/listening-mcq")
@RequiredArgsConstructor
public class ListeningMcqController {
    
    private final McqQuestionService mcqQuestionService;
    private final McqChoiceService mcqChoiceService;
    private final McqResponseService mcqResponseService;
    
    // ==================== 题目相关接口 ====================
    
    /**
     * 获取所有题目
     */
    @GetMapping("/questions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<McqQuestion>>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<McqQuestion> questions = mcqQuestionService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取所有题目失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取所有题目失败"));
        }
    }
    
    /**
     * 根据ID获取题目
     */
    @GetMapping("/questions/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqQuestion>> getQuestionById(@PathVariable Long id) {
        try {
            Optional<McqQuestion> question = mcqQuestionService.findById(id);
            if (question.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(question.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("题目不存在"));
            }
        } catch (Exception e) {
            log.error("获取题目失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取题目失败"));
        }
    }
    
    /**
     * 根据模块ID获取题目
     */
    @GetMapping("/questions/module/{moduleId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<McqQuestion>>> getQuestionsByModuleId(
            @PathVariable Long moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<McqQuestion> questions = mcqQuestionService.findByModuleId(moduleId, pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取模块题目失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取模块题目失败"));
        }
    }
    
    /**
     * 根据音频ID获取题目
     */
    @GetMapping("/questions/audio/{audioId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<McqQuestion>>> getQuestionsByAudioId(
            @PathVariable Long audioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<McqQuestion> questions = mcqQuestionService.findByAudioId(audioId, pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取音频题目失败: audioId={}, error={}", audioId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取音频题目失败"));
        }
    }
    
    /**
     * 创建题目
     */
    @PostMapping("/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqQuestion>> createQuestion(@Valid @RequestBody McqQuestion question) {
        try {
            McqQuestion createdQuestion = mcqQuestionService.save(question);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdQuestion));
        } catch (Exception e) {
            log.error("创建题目失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建题目失败"));
        }
    }
    
    /**
     * 更新题目
     */
    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqQuestion>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody McqQuestion question) {
        
        try {
            if (!mcqQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("题目不存在"));
            }
            
            question.setId(id);
            McqQuestion updatedQuestion = mcqQuestionService.save(question);
            return ResponseEntity.ok(ApiResponse.success(updatedQuestion));
        } catch (Exception e) {
            log.error("更新题目失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新题目失败"));
        }
    }
    
    /**
     * 删除题目
     */
    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteQuestion(@PathVariable Long id) {
        try {
            if (!mcqQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("题目不存在"));
            }
            
            mcqQuestionService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("题目删除成功"));
        } catch (Exception e) {
            log.error("删除题目失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除题目失败"));
        }
    }
    
    /**
     * 搜索题目
     */
    @GetMapping("/questions/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<McqQuestion>>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<McqQuestion> questions = mcqQuestionService.findByTextStemContaining(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("搜索题目失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索题目失败"));
        }
    }
    
    /**
     * 复制题目
     */
    @PostMapping("/questions/{id}/copy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqQuestion>> copyQuestion(@PathVariable Long id) {
        try {
            McqQuestion copiedQuestion = mcqQuestionService.copyQuestion(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(copiedQuestion));
        } catch (Exception e) {
            log.error("复制题目失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("复制题目失败"));
        }
    }
    
    // ==================== 选项相关接口 ====================
    
    /**
     * 获取题目的所有选项
     */
    @GetMapping("/questions/{questionId}/choices")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<McqChoice>>> getChoicesByQuestionId(@PathVariable Long questionId) {
        try {
            List<McqChoice> choices = mcqChoiceService.findByQuestionIdOrderByLabel(questionId);
            return ResponseEntity.ok(ApiResponse.success(choices));
        } catch (Exception e) {
            log.error("获取题目选项失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取题目选项失败"));
        }
    }
    
    /**
     * 根据ID获取选项
     */
    @GetMapping("/choices/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqChoice>> getChoiceById(@PathVariable Long id) {
        try {
            Optional<McqChoice> choice = mcqChoiceService.findById(id);
            if (choice.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(choice.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("选项不存在"));
            }
        } catch (Exception e) {
            log.error("获取选项失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取选项失败"));
        }
    }
    
    /**
     * 创建选项
     */
    @PostMapping("/choices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqChoice>> createChoice(@Valid @RequestBody McqChoice choice) {
        try {
            McqChoice createdChoice = mcqChoiceService.save(choice);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdChoice));
        } catch (Exception e) {
            log.error("创建选项失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建选项失败"));
        }
    }
    
    /**
     * 为题目创建标准选项
     */
    @PostMapping("/questions/{questionId}/choices/standard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<McqChoice>>> createStandardChoices(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> request) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> contents = (List<String>) request.get("contents");
            String correctLabel = (String) request.get("correctLabel");
            
            if (contents == null || contents.size() != 4 || correctLabel == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("需要提供4个选项内容和正确答案标签"));
            }
            
            List<McqChoice> choices = mcqChoiceService.createStandardChoices(questionId, contents, correctLabel);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(choices));
        } catch (Exception e) {
            log.error("创建标准选项失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建标准选项失败"));
        }
    }
    
    /**
     * 更新选项
     */
    @PutMapping("/choices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqChoice>> updateChoice(
            @PathVariable Long id,
            @Valid @RequestBody McqChoice choice) {
        
        try {
            if (!mcqChoiceService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("选项不存在"));
            }
            
            choice.setId(id);
            McqChoice updatedChoice = mcqChoiceService.save(choice);
            return ResponseEntity.ok(ApiResponse.success(updatedChoice));
        } catch (Exception e) {
            log.error("更新选项失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新选项失败"));
        }
    }
    
    /**
     * 删除选项
     */
    @DeleteMapping("/choices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteChoice(@PathVariable Long id) {
        try {
            if (!mcqChoiceService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("选项不存在"));
            }
            
            mcqChoiceService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("选项删除成功"));
        } catch (Exception e) {
            log.error("删除选项失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除选项失败"));
        }
    }
    
    /**
     * 设置选项为正确答案
     */
    @PutMapping("/choices/{id}/correct")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqChoice>> setChoiceAsCorrect(@PathVariable Long id) {
        try {
            McqChoice choice = mcqChoiceService.setAsCorrect(id);
            return ResponseEntity.ok(ApiResponse.success(choice));
        } catch (Exception e) {
            log.error("设置正确答案失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("设置正确答案失败"));
        }
    }
    
    /**
     * 切换选项正确性
     */
    @PutMapping("/choices/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqChoice>> toggleChoiceCorrectness(@PathVariable Long id) {
        try {
            McqChoice choice = mcqChoiceService.toggleCorrectness(id);
            return ResponseEntity.ok(ApiResponse.success(choice));
        } catch (Exception e) {
            log.error("切换选项正确性失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("切换选项正确性失败"));
        }
    }
    
    // ==================== 回答相关接口 ====================
    
    /**
     * 获取所有回答
     */
    @GetMapping("/responses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<McqResponse>>> getAllResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<McqResponse> responses = mcqResponseService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取所有回答失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取所有回答失败"));
        }
    }
    
    /**
     * 根据题目ID获取回答
     */
    @GetMapping("/questions/{questionId}/responses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<McqResponse>>> getResponsesByQuestionId(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<McqResponse> responses = mcqResponseService.findByQuestionId(questionId, pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取题目回答失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取题目回答失败"));
        }
    }
    
    /**
     * 根据ID获取回答
     */
    @GetMapping("/responses/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqResponse>> getResponseById(@PathVariable Long id) {
        try {
            Optional<McqResponse> response = mcqResponseService.findById(id);
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqResponse>> submitResponse(@RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.valueOf(request.get("questionId").toString());
            Long selectedChoiceId = Long.valueOf(request.get("selectedChoiceId").toString());
            Integer elapsedMs = request.get("elapsedMs") != null ? 
                    Integer.valueOf(request.get("elapsedMs").toString()) : 0;
            
            McqResponse response = mcqResponseService.submitResponse(questionId, selectedChoiceId, elapsedMs);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("提交回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("提交回答失败"));
        }
    }
    
    /**
     * 创建回答记录
     */
    @PostMapping("/responses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqResponse>> createResponse(@Valid @RequestBody McqResponse response) {
        try {
            McqResponse createdResponse = mcqResponseService.save(response);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdResponse));
        } catch (Exception e) {
            log.error("创建回答失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建回答失败"));
        }
    }
    
    /**
     * 判分回答
     */
    @PutMapping("/responses/{id}/grade")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqResponse>> gradeResponse(@PathVariable Long id) {
        try {
            McqResponse response = mcqResponseService.gradeResponse(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("判分回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("判分回答失败"));
        }
    }
    
    /**
     * 批量判分回答
     */
    @PutMapping("/responses/batch-grade")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<McqResponse>>> batchGradeResponses(@RequestBody List<Long> responseIds) {
        try {
            List<McqResponse> responses = mcqResponseService.batchGradeResponses(responseIds);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("批量判分回答失败: count={}, error={}", responseIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量判分回答失败"));
        }
    }
    
    /**
     * 判分题目的所有回答
     */
    @PutMapping("/questions/{questionId}/responses/grade-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<McqResponse>>> gradeAllResponsesByQuestion(@PathVariable Long questionId) {
        try {
            List<McqResponse> responses = mcqResponseService.gradeAllResponsesByQuestion(questionId);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("判分题目所有回答失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("判分题目所有回答失败"));
        }
    }
    
    /**
     * 删除回答
     */
    @DeleteMapping("/responses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteResponse(@PathVariable Long id) {
        try {
            if (!mcqResponseService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("回答不存在"));
            }
            
            mcqResponseService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("回答删除成功"));
        } catch (Exception e) {
            log.error("删除回答失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除回答失败"));
        }
    }
    
    // ==================== 统计相关接口 ====================
    
    /**
     * 获取题目统计信息
     */
    @GetMapping("/questions/{questionId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuestionStats(@PathVariable Long questionId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计
            stats.put("totalResponses", mcqResponseService.countByQuestionId(questionId));
            stats.put("correctResponses", mcqResponseService.countCorrectResponsesByQuestionId(questionId));
            stats.put("incorrectResponses", mcqResponseService.countIncorrectResponsesByQuestionId(questionId));
            stats.put("ungradedResponses", mcqResponseService.countUngradedResponsesByQuestionId(questionId));
            
            // 选项统计
            List<McqChoice> choices = mcqChoiceService.findByQuestionIdOrderByLabel(questionId);
            Map<String, Long> choiceStats = new HashMap<>();
            for (McqChoice choice : choices) {
                choiceStats.put(choice.getLabel(), mcqResponseService.countBySelectedChoiceId(choice.getId()));
            }
            stats.put("choiceSelections", choiceStats);
            
            // 平均回答时间
            stats.put("averageResponseTime", mcqResponseService.getAverageResponseTimeByQuestion(questionId));
            
            // 难度分析
            stats.put("difficulty", mcqResponseService.analyzeDifficultyByQuestion(questionId));
            
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取题目统计失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取题目统计失败"));
        }
    }
    
    /**
     * 获取模块统计信息
     */
    @GetMapping("/modules/{moduleId}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getModuleStats(@PathVariable Long moduleId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 题目统计
            long questionCount = mcqQuestionService.countByModuleId(moduleId);
            stats.put("questionCount", questionCount);
            
            // 选项统计
            List<McqQuestion> questions = mcqQuestionService.findByModuleId(moduleId);
            long totalChoices = 0;
            for (McqQuestion question : questions) {
                totalChoices += mcqChoiceService.countByQuestionId(question.getId());
            }
            stats.put("totalChoices", totalChoices);
            
            // 回答统计
            long totalResponses = 0;
            long correctResponses = 0;
            long incorrectResponses = 0;
            long ungradedResponses = 0;
            
            for (McqQuestion question : questions) {
                totalResponses += mcqResponseService.countByQuestionId(question.getId());
                correctResponses += mcqResponseService.countCorrectResponsesByQuestionId(question.getId());
                incorrectResponses += mcqResponseService.countIncorrectResponsesByQuestionId(question.getId());
                ungradedResponses += mcqResponseService.countUngradedResponsesByQuestionId(question.getId());
            }
            
            stats.put("totalResponses", totalResponses);
            stats.put("correctResponses", correctResponses);
            stats.put("incorrectResponses", incorrectResponses);
            stats.put("ungradedResponses", ungradedResponses);
            
            // 正确率
            if (totalResponses > 0) {
                double accuracy = (double) correctResponses / (correctResponses + incorrectResponses);
                stats.put("accuracy", accuracy);
            } else {
                stats.put("accuracy", null);
            }
            
            stats.put("timestamp", LocalDateTime.now());
            
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
            
            // 基础统计
            stats.put("totalQuestions", mcqQuestionService.count());
            stats.put("totalChoices", mcqChoiceService.count());
            stats.put("totalResponses", mcqResponseService.count());
            
            // 回答统计
            stats.put("correctResponses", mcqResponseService.countCorrectResponses());
            stats.put("incorrectResponses", mcqResponseService.countIncorrectResponses());
            stats.put("ungradedResponses", mcqResponseService.countUngradedResponses());
            stats.put("gradedResponses", mcqResponseService.countGradedResponses());
            
            // 平均回答时间
            stats.put("averageResponseTime", mcqResponseService.getAverageResponseTime());
            
            // 正确率统计
            Object[] accuracyStats = mcqResponseService.getAccuracyStats();
            if (accuracyStats != null && accuracyStats.length >= 3) {
                long totalGraded = (Long) accuracyStats[0];
                long correct = (Long) accuracyStats[1];
                
                if (totalGraded > 0) {
                    double accuracy = (double) correct / totalGraded;
                    stats.put("overallAccuracy", accuracy);
                } else {
                    stats.put("overallAccuracy", null);
                }
            }
            
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
     * 批量删除题目
     */
    @DeleteMapping("/questions/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteQuestions(@RequestBody List<Long> questionIds) {
        try {
            mcqQuestionService.batchDeleteQuestions(questionIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除题目成功"));
        } catch (Exception e) {
            log.error("批量删除题目失败: count={}, error={}", questionIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除题目失败"));
        }
    }
    
    /**
     * 批量删除选项
     */
    @DeleteMapping("/choices/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteChoices(@RequestBody List<Long> choiceIds) {
        try {
            mcqChoiceService.batchDeleteChoices(choiceIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除选项成功"));
        } catch (Exception e) {
            log.error("批量删除选项失败: count={}, error={}", choiceIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除选项失败"));
        }
    }
    
    /**
     * 批量删除回答
     */
    @DeleteMapping("/responses/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteResponses(@RequestBody List<Long> responseIds) {
        try {
            mcqResponseService.batchDeleteResponses(responseIds);
            return ResponseEntity.ok(ApiResponse.success("批量删除回答成功"));
        } catch (Exception e) {
            log.error("批量删除回答失败: count={}, error={}", responseIds.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除回答失败"));
        }
    }
    
    // ==================== 导入导出接口 ====================
    
    /**
     * 导出模块题目
     */
    @GetMapping("/modules/{moduleId}/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<McqQuestion>>> exportQuestionsByModule(@PathVariable Long moduleId) {
        try {
            List<McqQuestion> questions = mcqQuestionService.exportQuestionsByModule(moduleId);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("导出模块题目失败: moduleId={}, error={}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导出模块题目失败"));
        }
    }
    
    /**
     * 导入题目到模块
     */
    @PostMapping("/modules/{moduleId}/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<McqQuestion>>> importQuestionsToModule(
            @PathVariable Long moduleId,
            @RequestBody List<McqQuestion> questions) {
        
        try {
            List<McqQuestion> importedQuestions = mcqQuestionService.importQuestionsToModule(moduleId, questions);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(importedQuestions));
        } catch (Exception e) {
            log.error("导入题目到模块失败: moduleId={}, count={}, error={}", 
                     moduleId, questions.size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导入题目到模块失败"));
        }
    }

    // ==================== 题库管理系统 - 听力选择题相关接口 ====================

    /**
     * 获取听力选择题列表 (对应 questionBank.js 的 getMCQQuestions)
     */
    @GetMapping("/question-bank/mcq")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<McqQuestion>>> getMCQQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean isActive) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<McqQuestion> questions;
            
            if (moduleId != null) {
                questions = mcqQuestionService.findByModuleId(moduleId, pageable);
            } else {
                questions = mcqQuestionService.findAll(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (Exception e) {
            log.error("获取听力选择题列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取听力选择题列表失败"));
        }
    }

    /**
     * 创建听力选择题 (对应 questionBank.js 的 createMCQQuestion)
     */
    @PostMapping("/question-bank/mcq")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqQuestion>> createMCQQuestion(@Valid @RequestBody McqQuestion question) {
        try {
            McqQuestion createdQuestion = mcqQuestionService.save(question);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdQuestion));
        } catch (Exception e) {
            log.error("创建听力选择题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建听力选择题失败: " + e.getMessage()));
        }
    }

    /**
     * 更新听力选择题 (对应 questionBank.js 的 updateMCQQuestion)
     */
    @PutMapping("/question-bank/mcq/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<McqQuestion>> updateMCQQuestion(
            @PathVariable Long id,
            @Valid @RequestBody McqQuestion question) {
        try {
            if (!mcqQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("听力选择题不存在"));
            }
            question.setId(id);
            McqQuestion updatedQuestion = mcqQuestionService.save(question);
            return ResponseEntity.ok(ApiResponse.success(updatedQuestion));
        } catch (Exception e) {
            log.error("更新听力选择题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新听力选择题失败: " + e.getMessage()));
        }
    }

    /**
     * 删除听力选择题 (对应 questionBank.js 的 deleteMCQQuestion)
     */
    @DeleteMapping("/question-bank/mcq/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMCQQuestion(@PathVariable Long id) {
        try {
            if (!mcqQuestionService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("听力选择题不存在"));
            }
            mcqQuestionService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("听力选择题删除成功"));
        } catch (Exception e) {
            log.error("删除听力选择题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除听力选择题失败: " + e.getMessage()));
        }
    }

    /**
     * 验证选择题答案 (对应 questionBank.js 的 validateMCQAnswer)
     */
    @PostMapping("/question-bank/mcq/{questionId}/validate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateMCQAnswer(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> request) {
        try {
            String answer = (String) request.get("answer");
            if (answer == null || answer.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("答案不能为空"));
            }
            
            // TODO: 实现答案验证逻辑
            Map<String, Object> result = Map.of(
                    "questionId", questionId,
                    "userAnswer", answer,
                    "isCorrect", true, // 临时返回true
                    "correctAnswer", "A",
                    "explanation", "这是正确答案的解释"
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("验证选择题答案失败: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("验证选择题答案失败: " + e.getMessage()));
        }
    }

    /**
     * 获取选择题统计 (对应 questionBank.js 的 getMCQStatistics)
     */
    @GetMapping("/question-bank/mcq/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMCQStatistics(
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Integer difficultyLevel) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计
            if (moduleId != null) {
                stats.put("totalQuestions", mcqQuestionService.countByModuleId(moduleId));
            } else {
                stats.put("totalQuestions", mcqQuestionService.count());
            }
            
            stats.put("totalChoices", mcqChoiceService.count());
            stats.put("totalResponses", mcqResponseService.count());
            
            // 回答统计
            stats.put("correctResponses", mcqResponseService.countCorrectResponses());
            stats.put("incorrectResponses", mcqResponseService.countIncorrectResponses());
            stats.put("ungradedResponses", mcqResponseService.countUngradedResponses());
            
            // 平均回答时间
            stats.put("averageResponseTime", mcqResponseService.getAverageResponseTime());
            
            // 正确率统计
            Object[] accuracyStats = mcqResponseService.getAccuracyStats();
            if (accuracyStats != null && accuracyStats.length >= 2) {
                long totalGraded = (Long) accuracyStats[0];
                long correct = (Long) accuracyStats[1];
                if (totalGraded > 0) {
                    double accuracy = (double) correct / totalGraded;
                    stats.put("overallAccuracy", accuracy);
                }
            }
            
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取选择题统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取选择题统计失败"));
        }
    }

    // ==================== 批量操作接口 ====================

    /**
     * 批量操作听力选择题 (对应 questionBank.js 的 batchOperateListeningQuestions)
     */
    @PostMapping("/question-bank/mcq/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchOperateMCQQuestions(@RequestBody Map<String, Object> request) {
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
                    mcqQuestionService.batchDeleteQuestions(questionIds);
                    result.put("message", "批量删除成功");
                    break;
                case "activate":
                    // TODO: 实现批量激活逻辑
                    result.put("message", "批量激活成功");
                    break;
                case "deactivate":
                    // TODO: 实现批量停用逻辑
                    result.put("message", "批量停用成功");
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
            log.error("批量操作听力选择题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量操作听力选择题失败: " + e.getMessage()));
        }
    }

    // ==================== 文件上传相关接口 ====================

    /**
     * 上传题目音频 (对应 questionBank.js 的 uploadQuestionAudio)
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
            log.error("上传题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("上传题目音频失败: " + e.getMessage()));
        }
    }

    /**
     * 批量上传音频 (对应 questionBank.js 的 batchUploadAudio)
     */
    @PostMapping("/question-bank/upload/audio/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> batchUploadAudio(
            @RequestParam("files") List<org.springframework.web.multipart.MultipartFile> files,
            @RequestParam(required = false) List<Long> questionIds) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();
            
            for (int i = 0; i < files.size(); i++) {
                org.springframework.web.multipart.MultipartFile file = files.get(i);
                Long questionId = (questionIds != null && i < questionIds.size()) ? questionIds.get(i) : null;
                
                // TODO: 实现批量音频文件上传逻辑
                Map<String, Object> result = Map.of(
                        "filename", file.getOriginalFilename(),
                        "size", file.getSize(),
                        "url", "/uploads/audio/" + file.getOriginalFilename(),
                        "questionId", questionId != null ? questionId : 0L,
                        "index", i
                );
                results.add(result);
            }
            
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            log.error("批量上传音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量上传音频失败: " + e.getMessage()));
        }
    }

    /**
     * 上传题目图片 (对应 questionBank.js 的 uploadQuestionImage)
     */
    @PostMapping("/question-bank/upload/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadQuestionImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) Long questionId) {
        try {
            // TODO: 实现图片文件上传逻辑
            Map<String, Object> result = Map.of(
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "url", "/uploads/images/" + file.getOriginalFilename(),
                    "questionId", questionId != null ? questionId : 0L
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("上传题目图片失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("上传题目图片失败: " + e.getMessage()));
        }
    }

    /**
     * 删除题目图片 (对应 questionBank.js 的 deleteQuestionImage)
     */
    @DeleteMapping("/question-bank/upload/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteQuestionImage(@RequestBody Map<String, Object> request) {
        try {
            String imageUrl = (String) request.get("imageUrl");
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("图片URL不能为空"));
            }
            
            // TODO: 实现删除图片文件的逻辑
            
            return ResponseEntity.ok(ApiResponse.success("题目图片删除成功"));
        } catch (Exception e) {
            log.error("删除题目图片失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除题目图片失败: " + e.getMessage()));
        }
    }

    // ==================== 数据导入导出接口 ====================

    /**
     * 批量导入题目 (对应 questionBank.js 的 importQuestions)
     */
    @PostMapping("/question-bank/questions/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importQuestions(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") Boolean skipDuplicates,
            @RequestParam(required = false, defaultValue = "false") Boolean updateExisting,
            @RequestParam(required = false, defaultValue = "true") Boolean validateData) {
        try {
            // TODO: 实现题目批量导入逻辑
            Map<String, Object> result = Map.of(
                    "totalRows", 100,
                    "successCount", 95,
                    "failureCount", 5,
                    "duplicateCount", 3,
                    "message", "题目导入完成"
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("批量导入题目失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量导入题目失败: " + e.getMessage()));
        }
    }

    /**
     * 导出题目 (对应 questionBank.js 的 exportQuestions)
     */
    @GetMapping("/question-bank/questions/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportQuestions(
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) String format) {
        try {
            // TODO: 实现题目导出逻辑
            String csvContent = "ID,题干,选项A,选项B,选项C,选项D,正确答案,难度等级\n1,这是一道测试题,选项A,选项B,选项C,选项D,A,3";
            
            String filename = "mcq_questions." + (format != null ? format : "csv");
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出题目失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出题目模板 (对应 questionBank.js 的 exportQuestionTemplate)
     */
    @GetMapping("/question-bank/questions/export/template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportQuestionTemplate(@RequestParam(defaultValue = "mcq") String type) {
        try {
            String csvContent = "ID,题干,选项A,选项B,选项C,选项D,正确答案,难度等级,模块ID\n";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=mcq_template.csv")
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出题目模板失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 验证导入文件 (对应 questionBank.js 的 validateImportFile)
     */
    @PostMapping("/question-bank/questions/import/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateImportFile(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            // TODO: 实现导入文件验证逻辑
            Map<String, Object> result = Map.of(
                    "isValid", true,
                    "totalRows", 100,
                    "validRows", 95,
                    "invalidRows", 5,
                    "errors", List.of("第6行：题干不能为空", "第12行：选项数量不足"),
                    "warnings", List.of("第3行：难度等级超出范围，已自动调整")
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("验证导入文件失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("验证导入文件失败: " + e.getMessage()));
        }
    }

    /**
     * 获取导入历史 (对应 questionBank.js 的 getImportHistory)
     */
    @GetMapping("/question-bank/questions/import/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getImportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // TODO: 实现获取导入历史逻辑
            List<Map<String, Object>> history = List.of(
                    Map.of("id", 1L, "filename", "questions_batch1.xlsx", "importTime", LocalDateTime.now().minusDays(1), "status", "success", "totalRows", 100, "successCount", 95),
                    Map.of("id", 2L, "filename", "questions_batch2.xlsx", "importTime", LocalDateTime.now().minusDays(2), "status", "partial", "totalRows", 50, "successCount", 45)
            );
            
            Pageable pageable = PageRequest.of(page, size);
            // TODO: 实现真正的分页逻辑
            Page<Map<String, Object>> historyPage = new org.springframework.data.domain.PageImpl<>(history, pageable, history.size());
            
            return ResponseEntity.ok(ApiResponse.success(historyPage));
        } catch (Exception e) {
            log.error("获取导入历史失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取导入历史失败"));
        }
    }
}
