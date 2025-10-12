package org.icao4.eqasbackend2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.entity.exam.ExamRecord;
import org.icao4.eqasbackend2.service.exam.ExamRecordService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 考试记录控制器
 * 提供考试记录的增删改查和管理功能
 */
@Slf4j
@RestController
@RequestMapping("/exam-records")
@CacheConfig(cacheNames = "examRecords")
@RequiredArgsConstructor
@Validated
public class ExamRecordController {

    private final ExamRecordService examRecordService;

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建考试记录
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamRecord>> createExamRecord(@Valid @RequestBody CreateExamRecordRequest request) {
        try {
            ExamRecord examRecord = examRecordService.createExamRecord(
                    request.getExamPaperId(),
                    request.getUserId()
            );

            log.info("成功创建考试记录: examPaperId={}, userId={}", 
                    examRecord.getExamPaperId(), examRecord.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(examRecord));
        } catch (Exception e) {
            log.error("创建考试记录失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建考试记录失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取考试记录
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ExamRecord>> getExamRecordById(@PathVariable @NotNull Long id) {
        try {
            Optional<ExamRecord> examRecord = examRecordService.findById(id);
            if (examRecord.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(examRecord.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error(404, "考试记录不存在"));
            }
        } catch (Exception e) {
            log.error("获取考试记录失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取考试记录失败"));
        }
    }

    /**
     * 根据用户ID获取考试记录
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamRecord>>> getExamRecordsByUserId(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
            Page<ExamRecord> examRecords = examRecordService.findByUserId(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success(examRecords));
        } catch (Exception e) {
            log.error("获取用户考试记录失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户考试记录失败"));
        }
    }

    /**
     * 根据试卷ID获取考试记录
     */
    @GetMapping("/exam-paper/{examPaperId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamRecord>>> getExamRecordsByExamPaperId(
            @PathVariable @NotNull Long examPaperId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
            Page<ExamRecord> examRecords = examRecordService.findByExamPaperId(examPaperId, pageable);
            return ResponseEntity.ok(ApiResponse.success(examRecords));
        } catch (Exception e) {
            log.error("获取试卷考试记录失败: examPaperId={}, error={}", examPaperId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取试卷考试记录失败"));
        }
    }

    /**
     * 获取用户在指定试卷的考试记录
     */
    @GetMapping("/user/{userId}/exam-paper/{examPaperId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamRecord>>> getUserExamRecordsByPaper(
            @PathVariable @NotNull Long userId,
            @PathVariable @NotNull Long examPaperId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
            Page<ExamRecord> examRecords = examRecordService.findByUserIdAndExamPaperId(userId, examPaperId, pageable);
            return ResponseEntity.ok(ApiResponse.success(examRecords));
        } catch (Exception e) {
            log.error("获取用户试卷考试记录失败: userId={}, examPaperId={}, error={}", 
                    userId, examPaperId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户试卷考试记录失败"));
        }
    }

    /**
     * 分页获取所有考试记录
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamRecord>>> getAllExamRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<ExamRecord> examRecords = examRecordService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(examRecords));
        } catch (Exception e) {
            log.error("获取考试记录列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取考试记录列表失败"));
        }
    }

    /**
     * 根据时间范围获取考试记录
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamRecord>>> getExamRecordsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
            Page<ExamRecord> examRecords = examRecordService.findByCreateAtBetween(startTime, endTime, pageable);
            return ResponseEntity.ok(ApiResponse.success(examRecords));
        } catch (Exception e) {
            log.error("获取时间范围考试记录失败: startTime={}, endTime={}, error={}", 
                    startTime, endTime, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取时间范围考试记录失败"));
        }
    }

    /**
     * 删除考试记录
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<Void>> deleteExamRecord(@PathVariable @NotNull Long id) {
        try {
            if (!examRecordService.existsById(id)) {
                return ResponseEntity.ok(ApiResponse.error(404, "考试记录不存在"));
            }

            examRecordService.deleteById(id);
            log.info("成功删除考试记录: id={}", id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("删除考试记录失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除考试记录失败"));
        }
    }

    // ==================== 考试流程管理 ====================

    /**
     * 开始考试
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamRecord>> startExam(@PathVariable @NotNull Long id) {
        try {
            ExamRecord examRecord = examRecordService.startExam(id);
            log.info("考试开始: recordId={}, userId={}, examPaperId={}", 
                    id, examRecord.getUserId(), examRecord.getExamPaperId());
            return ResponseEntity.ok(ApiResponse.success(examRecord));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("开始考试失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("开始考试失败"));
        }
    }

    /**
     * 完成考试
     */
    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamRecord>> finishExam(@PathVariable @NotNull Long id) {
        try {
            ExamRecord examRecord = examRecordService.finishExam(id);
            log.info("考试完成: recordId={}, userId={}, examPaperId={}", 
                    id, examRecord.getUserId(), examRecord.getExamPaperId());
            return ResponseEntity.ok(ApiResponse.success(examRecord));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("完成考试失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("完成考试失败"));
        }
    }

    /**
     * 更新考试状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamRecord>> updateExamStatus(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isFinished = request.get("isFinished");
            if (isFinished == null) {
                return ResponseEntity.ok(ApiResponse.error(400, "考试状态不能为空"));
            }

            ExamRecord examRecord = examRecordService.updateExamStatus(id, isFinished);
            log.info("更新考试状态: id={}, isFinished={}", id, isFinished);
            return ResponseEntity.ok(ApiResponse.success(examRecord));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("更新考试状态失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新考试状态失败"));
        }
    }

    // ==================== 查询和统计 ====================

    /**
     * 获取用户最近的考试记录
     */
    @GetMapping("/user/{userId}/recent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ExamRecord>>> getRecentExamRecords(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ExamRecord> examRecords = examRecordService.getRecentExamRecordsByUserId(userId, limit);
            return ResponseEntity.ok(ApiResponse.success(examRecords));
        } catch (Exception e) {
            log.error("获取最近考试记录失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取最近考试记录失败"));
        }
    }

    /**
     * 获取用户指定试卷的最新考试记录
     */
    @GetMapping("/user/{userId}/exam-paper/{examPaperId}/latest")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ExamRecord>> getLatestExamRecord(
            @PathVariable @NotNull Long userId,
            @PathVariable @NotNull Long examPaperId) {
        try {
            Optional<ExamRecord> examRecord = examRecordService.getLatestExamRecordByUserIdAndExamPaperId(userId, examPaperId);
            if (examRecord.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(examRecord.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error(404, "未找到考试记录"));
            }
        } catch (Exception e) {
            log.error("获取最新考试记录失败: userId={}, examPaperId={}, error={}", 
                    userId, examPaperId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取最新考试记录失败"));
        }
    }

    /**
     * 获取最热门的试卷统计
     */
    @GetMapping("/statistics/popular-papers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getMostPopularExamPapers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> popularPapers = examRecordService.findMostPopularExamPapers(pageable);
            return ResponseEntity.ok(ApiResponse.success(popularPapers));
        } catch (Exception e) {
            log.error("获取热门试卷统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取热门试卷统计失败"));
        }
    }

    /**
     * 获取最活跃的用户统计
     */
    @GetMapping("/statistics/active-users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getMostActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> activeUsers = examRecordService.findMostActiveUsers(pageable);
            return ResponseEntity.ok(ApiResponse.success(activeUsers));
        } catch (Exception e) {
            log.error("获取活跃用户统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取活跃用户统计失败"));
        }
    }

    /**
     * 获取每日考试统计
     */
    @GetMapping("/statistics/daily")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getDailyExamStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Object[]> dailyStats = examRecordService.getDailyExamStats(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(dailyStats));
        } catch (Exception e) {
            log.error("获取每日考试统计失败: startDate={}, endDate={}, error={}", 
                    startDate, endDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取每日考试统计失败"));
        }
    }

    /**
     * 获取每月考试统计
     */
    @GetMapping("/statistics/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getMonthlyExamStats() {
        try {
            List<Object[]> monthlyStats = examRecordService.getMonthlyExamStats();
            return ResponseEntity.ok(ApiResponse.success(monthlyStats));
        } catch (Exception e) {
            log.error("获取每月考试统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取每月考试统计失败"));
        }
    }

    /**
     * 获取考试统计报告
     */
    @GetMapping("/statistics/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getExamStatisticsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Object report = examRecordService.getExamStatisticsReport(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            log.error("获取考试统计报告失败: startDate={}, endDate={}, error={}", 
                    startDate, endDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取考试统计报告失败"));
        }
    }

    /**
     * 获取基础统计信息
     */
    @GetMapping("/statistics/basic")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBasicStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRecords", examRecordService.count());
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
            statistics.put("todayRecords", examRecordService.countByCreateAtBetween(startOfToday, now));
            
            LocalDateTime startOfWeek = now.minusDays(7);
            statistics.put("weekRecords", examRecordService.countByCreateAtBetween(startOfWeek, now));
            
            LocalDateTime startOfMonth = now.minusDays(30);
            statistics.put("monthRecords", examRecordService.countByCreateAtBetween(startOfMonth, now));

            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("获取基础统计信息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取基础统计信息失败"));
        }
    }

    // ==================== 数据传输对象 ====================

    /**
     * 创建考试记录请求
     */
    public static class CreateExamRecordRequest {
        @NotNull(message = "试卷ID不能为空")
        private Long examPaperId;
        
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        // Getters and Setters
        public Long getExamPaperId() { return examPaperId; }
        public void setExamPaperId(Long examPaperId) { this.examPaperId = examPaperId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
