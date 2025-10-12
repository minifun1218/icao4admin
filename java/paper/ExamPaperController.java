package org.icao4.eqasbackend2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.entity.exam.ExamPaper;
import org.icao4.eqasbackend2.service.exam.ExamPaperService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 考试试卷控制器
 * 提供考试试卷的增删改查和管理功能
 */
@Slf4j
@RestController
@RequestMapping("/exam-papers")
@CacheConfig(cacheNames = "examPapers")
@RequiredArgsConstructor
@Validated
public class ExamPaperController {

    private final ExamPaperService examPaperService;

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建考试试卷
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamPaper>> createExamPaper(@Valid @RequestBody CreateExamPaperRequest request) {
        try {
            // 检查代码是否已存在
            if (examPaperService.existsByCode(request.getCode())) {
                return ResponseEntity.ok(ApiResponse.error(400, "试卷代码已存在"));
            }

            ExamPaper examPaper = examPaperService.createExamPaper(
                    request.getCode(),
                    request.getName(),
                    request.getTotalDurationMin(),
                    request.getDescription()
            );

            log.info("成功创建考试试卷: code={}, name={}", examPaper.getCode(), examPaper.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(examPaper));
        } catch (Exception e) {
            log.error("创建考试试卷失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建考试试卷失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取考试试卷
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ExamPaper>> getExamPaperById(@PathVariable @NotNull Long id) {
        try {
            Optional<ExamPaper> examPaper = examPaperService.findById(id);
            if (examPaper.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(examPaper.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error(404, "考试试卷不存在"));
            }
        } catch (Exception e) {
            log.error("获取考试试卷失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取考试试卷失败"));
        }
    }

    /**
     * 根据代码获取考试试卷
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ExamPaper>> getExamPaperByCode(@PathVariable String code) {
        try {
            Optional<ExamPaper> examPaper = examPaperService.findByCode(code);
            if (examPaper.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(examPaper.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error(404, "考试试卷不存在"));
            }
        } catch (Exception e) {
            log.error("获取考试试卷失败: code={}, error={}", code, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取考试试卷失败"));
        }
    }

    /**
     * 分页获取所有考试试卷
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamPaper>>> getAllExamPapers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<ExamPaper> examPapers = examPaperService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(examPapers));
        } catch (Exception e) {
            log.error("获取考试试卷列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取考试试卷列表失败"));
        }
    }

    /**
     * 搜索考试试卷
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamPaper>>> searchExamPapers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ExamPaper> examPapers = examPaperService.searchExamPapers(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success(examPapers));
        } catch (Exception e) {
            log.error("搜索考试试卷失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索考试试卷失败"));
        }
    }

    /**
     * 更新考试试卷
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamPaper>> updateExamPaper(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody UpdateExamPaperRequest request) {
        try {
            ExamPaper examPaper = examPaperService.updateExamPaper(
                    id,
                    request.getName(),
                    request.getTotalDurationMin(),
                    request.getDescription()
            );

            log.info("成功更新考试试卷: id={}, name={}", id, examPaper.getName());
            return ResponseEntity.ok(ApiResponse.success(examPaper));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("更新考试试卷失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新考试试卷失败"));
        }
    }

    /**
     * 更新考试试卷代码
     */
    @PutMapping("/{id}/code")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamPaper>> updateExamPaperCode(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newCode = request.get("code");
            if (newCode == null || newCode.trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(400, "试卷代码不能为空"));
            }

            ExamPaper examPaper = examPaperService.updateExamPaperCode(id, newCode);
            log.info("成功更新考试试卷代码: id={}, newCode={}", id, newCode);
            return ResponseEntity.ok(ApiResponse.success(examPaper));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("更新考试试卷代码失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新考试试卷代码失败"));
        }
    }

    /**
     * 删除考试试卷
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<Void>> deleteExamPaper(@PathVariable @NotNull Long id) {
        try {
            if (!examPaperService.existsById(id)) {
                return ResponseEntity.ok(ApiResponse.error(404, "考试试卷不存在"));
            }

            if (!examPaperService.canDeleteExamPaper(id)) {
                return ResponseEntity.ok(ApiResponse.error(400, "该试卷已被使用，无法删除"));
            }

            examPaperService.deleteById(id);
            log.info("成功删除考试试卷: id={}", id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("删除考试试卷失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除考试试卷失败"));
        }
    }

    // ==================== 高级功能 ====================

    /**
     * 复制考试试卷
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamPaper>> copyExamPaper(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newCode = request.get("code");
            String newName = request.get("name");

            if (newCode == null || newCode.trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(400, "新试卷代码不能为空"));
            }
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(400, "新试卷名称不能为空"));
            }

            ExamPaper examPaper = examPaperService.copyExamPaper(id, newCode, newName);
            log.info("成功复制考试试卷: originalId={}, newId={}, newCode={}", id, examPaper.getId(), newCode);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(examPaper));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("复制考试试卷失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("复制考试试卷失败"));
        }
    }

    /**
     * 生成试卷代码
     */
    @GetMapping("/generate-code")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateExamPaperCode() {
        try {
            String code = examPaperService.generateExamPaperCode();
            Map<String, String> result = new HashMap<>();
            result.put("code", code);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("生成试卷代码失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("生成试卷代码失败"));
        }
    }

    /**
     * 发布试卷
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamPaper>> publishExamPaper(@PathVariable @NotNull Long id) {
        try {
            ExamPaper examPaper = examPaperService.publishExamPaper(id);
            log.info("成功发布考试试卷: id={}, code={}", id, examPaper.getCode());
            return ResponseEntity.ok(ApiResponse.success(examPaper));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("发布考试试卷失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("发布考试试卷失败"));
        }
    }

    /**
     * 下架试卷
     */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamPaper>> unpublishExamPaper(@PathVariable @NotNull Long id) {
        try {
            ExamPaper examPaper = examPaperService.unpublishExamPaper(id);
            log.info("成功下架考试试卷: id={}, code={}", id, examPaper.getCode());
            return ResponseEntity.ok(ApiResponse.success(examPaper));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("下架考试试卷失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("下架考试试卷失败"));
        }
    }

    // ==================== 统计和报告 ====================

    /**
     * 获取试卷统计信息
     */
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getExamPaperStatistics(@PathVariable @NotNull Long id) {
        try {
            Object statistics = examPaperService.getExamPaperStatistics(id);
            if (statistics != null) {
                return ResponseEntity.ok(ApiResponse.success(statistics));
            } else {
                return ResponseEntity.ok(ApiResponse.error(404, "考试试卷不存在"));
            }
        } catch (Exception e) {
            log.error("获取试卷统计信息失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取试卷统计信息失败"));
        }
    }

    /**
     * 获取最近创建的试卷
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ExamPaper>>> getRecentExamPapers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ExamPaper> examPapers = examPaperService.getRecentlyCreatedExamPapers(limit);
            return ResponseEntity.ok(ApiResponse.success(examPapers));
        } catch (Exception e) {
            log.error("获取最近试卷失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取最近试卷失败"));
        }
    }

    /**
     * 获取最受欢迎的试卷
     */
    @GetMapping("/popular")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ExamPaper>>> getPopularExamPapers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ExamPaper> examPapers = examPaperService.getMostPopularExamPapers(limit);
            return ResponseEntity.ok(ApiResponse.success(examPapers));
        } catch (Exception e) {
            log.error("获取热门试卷失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取热门试卷失败"));
        }
    }

    /**
     * 获取推荐试卷
     */
    @GetMapping("/recommended")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ExamPaper>>> getRecommendedExamPapers(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<ExamPaper> examPapers = examPaperService.getRecommendedExamPapers(userId, limit);
            return ResponseEntity.ok(ApiResponse.success(examPapers));
        } catch (Exception e) {
            log.error("获取推荐试卷失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取推荐试卷失败"));
        }
    }

    // ==================== 数据传输对象 ====================

    /**
     * 创建考试试卷请求
     */
    public static class CreateExamPaperRequest {
        @NotNull(message = "试卷代码不能为空")
        private String code;
        
        @NotNull(message = "试卷名称不能为空")
        private String name;
        
        @NotNull(message = "考试时长不能为空")
        private Integer totalDurationMin;
        
        private String description;

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getTotalDurationMin() { return totalDurationMin; }
        public void setTotalDurationMin(Integer totalDurationMin) { this.totalDurationMin = totalDurationMin; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 更新考试试卷请求
     */
    public static class UpdateExamPaperRequest {
        private String name;
        private Integer totalDurationMin;
        private String description;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getTotalDurationMin() { return totalDurationMin; }
        public void setTotalDurationMin(Integer totalDurationMin) { this.totalDurationMin = totalDurationMin; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
