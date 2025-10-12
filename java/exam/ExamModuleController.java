package org.icao4.eqasbackend2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.entity.exam.ExamModule;
import org.icao4.eqasbackend2.service.exam.ExamModuleService;
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
 * 考试模块控制器
 * 提供考试模块的增删改查和管理功能
 */
@Slf4j
@RestController
@RequestMapping("/exam-modules")
@CacheConfig(cacheNames = "examModules")
@RequiredArgsConstructor
@Validated
public class ExamModuleController {

    private final ExamModuleService examModuleService;

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建考试模块
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamModule>> createExamModule(@Valid @RequestBody CreateExamModuleRequest request) {
        try {
            ExamModule examModule = examModuleService.createModule(
                    request.getExamPaperId(),
                    request.getModuleType(),
                    request.getDisplayOrder(),
                    request.getConfigJson(),
                    request.getScore()
            );

            log.info("成功创建考试模块: examPaperId={}, moduleType={}, displayOrder={}", 
                    examModule.getExamPaperId(), examModule.getModuleType(), examModule.getDisplayOrder());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(examModule));
        } catch (Exception e) {
            log.error("创建考试模块失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建考试模块失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取考试模块
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ExamModule>> getExamModuleById(@PathVariable @NotNull Long id) {
        try {
            Optional<ExamModule> examModule = examModuleService.findById(id);
            if (examModule.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(examModule.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error(404, "考试模块不存在"));
            }
        } catch (Exception e) {
            log.error("获取考试模块失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取考试模块失败"));
        }
    }

    /**
     * 根据试卷ID获取模块列表
     */
    @GetMapping("/exam-paper/{examPaperId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ExamModule>>> getModulesByExamPaperId(
            @PathVariable @NotNull Long examPaperId,
            @RequestParam(defaultValue = "true") boolean ordered) {
        try {
            List<ExamModule> modules;
            if (ordered) {
                modules = examModuleService.findByExamPaperIdOrderByDisplayOrderAsc(examPaperId);
            } else {
                modules = examModuleService.findByExamPaperId(examPaperId);
            }
            return ResponseEntity.ok(ApiResponse.success(modules));
        } catch (Exception e) {
            log.error("获取试卷模块失败: examPaperId={}, error={}", examPaperId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取试卷模块失败"));
        }
    }

    /**
     * 根据模块类型获取模块列表
     */
    @GetMapping("/type/{moduleType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamModule>>> getModulesByType(
            @PathVariable ExamModule.ModuleType moduleType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
            Page<ExamModule> modules = examModuleService.findByModuleType(moduleType, pageable);
            return ResponseEntity.ok(ApiResponse.success(modules));
        } catch (Exception e) {
            log.error("获取模块类型失败: moduleType={}, error={}", moduleType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取模块类型失败"));
        }
    }

    /**
     * 分页获取所有考试模块
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ExamModule>>> getAllExamModules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<ExamModule> examModules = examModuleService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(examModules));
        } catch (Exception e) {
            log.error("获取考试模块列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取考试模块列表失败"));
        }
    }

    /**
     * 更新考试模块配置
     */
    @PutMapping("/{id}/config")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamModule>> updateModuleConfig(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, String> request) {
        try {
            String configJson = request.get("configJson");
            ExamModule examModule = examModuleService.updateModuleConfig(id, configJson);
            
            log.info("成功更新模块配置: id={}", id);
            return ResponseEntity.ok(ApiResponse.success(examModule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("更新模块配置失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新模块配置失败"));
        }
    }

    /**
     * 更新模块显示顺序
     */
    @PutMapping("/{id}/display-order")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamModule>> updateDisplayOrder(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer displayOrder = request.get("displayOrder");
            if (displayOrder == null) {
                return ResponseEntity.ok(ApiResponse.error(400, "显示顺序不能为空"));
            }

            ExamModule examModule = examModuleService.updateDisplayOrder(id, displayOrder);
            log.info("成功更新模块显示顺序: id={}, displayOrder={}", id, displayOrder);
            return ResponseEntity.ok(ApiResponse.success(examModule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("更新模块显示顺序失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新模块显示顺序失败"));
        }
    }

    /**
     * 更新模块评分标准
     */
    @PutMapping("/{id}/score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamModule>> updateModuleScore(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, Long> request) {
        try {
            Long score = request.get("score");
            ExamModule examModule = examModuleService.updateModuleScore(id, score);
            
            log.info("成功更新模块评分: id={}, score={}", id, score);
            return ResponseEntity.ok(ApiResponse.success(examModule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("更新模块评分失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新模块评分失败"));
        }
    }

    /**
     * 删除考试模块
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<Void>> deleteExamModule(@PathVariable @NotNull Long id) {
        try {
            if (!examModuleService.existsById(id)) {
                return ResponseEntity.ok(ApiResponse.error(404, "考试模块不存在"));
            }

            examModuleService.deleteById(id);
            log.info("成功删除考试模块: id={}", id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("删除考试模块失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除考试模块失败"));
        }
    }

    // ==================== 高级功能 ====================

    /**
     * 复制考试模块
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamModule>> copyExamModule(@PathVariable @NotNull Long id) {
        try {
            ExamModule examModule = examModuleService.copyModule(id);
            log.info("成功复制考试模块: originalId={}, newId={}", id, examModule.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(examModule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("复制考试模块失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("复制考试模块失败"));
        }
    }

    /**
     * 复制模块到其他试卷
     */
    @PostMapping("/{id}/copy-to-paper")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamModule>> copyModuleToExamPaper(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, Long> request) {
        try {
            Long targetExamPaperId = request.get("targetExamPaperId");
            if (targetExamPaperId == null) {
                return ResponseEntity.ok(ApiResponse.error(400, "目标试卷ID不能为空"));
            }

            ExamModule examModule = examModuleService.copyModuleToExamPaper(id, targetExamPaperId);
            log.info("成功复制模块到试卷: originalId={}, newId={}, targetExamPaperId={}", 
                    id, examModule.getId(), targetExamPaperId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(examModule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("复制模块到试卷失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("复制模块到试卷失败"));
        }
    }

    /**
     * 重新排序模块
     */
    @PutMapping("/exam-paper/{examPaperId}/reorder")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<List<ExamModule>>> reorderModules(
            @PathVariable @NotNull Long examPaperId,
            @RequestBody List<Long> moduleIds) {
        try {
            if (moduleIds == null || moduleIds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(400, "模块ID列表不能为空"));
            }

            List<ExamModule> modules = examModuleService.reorderModules(examPaperId, moduleIds);
            log.info("成功重新排序模块: examPaperId={}, moduleCount={}", examPaperId, moduleIds.size());
            return ResponseEntity.ok(ApiResponse.success(modules));
        } catch (Exception e) {
            log.error("重新排序模块失败: examPaperId={}, error={}", examPaperId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("重新排序模块失败"));
        }
    }

    /**
     * 激活/停用模块
     */
    @PutMapping("/{id}/toggle-activation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<ExamModule>> toggleModuleActivation(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isActivate = request.get("isActivate");
            if (isActivate == null) {
                return ResponseEntity.ok(ApiResponse.error(400, "激活状态不能为空"));
            }

            ExamModule examModule = examModuleService.toggleModuleActivation(id, isActivate);
            log.info("成功切换模块激活状态: id={}, isActivate={}", id, isActivate);
            return ResponseEntity.ok(ApiResponse.success(examModule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("切换模块激活状态失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("切换模块激活状态失败"));
        }
    }

    // ==================== 查询和统计 ====================

    /**
     * 获取试卷的活跃模块
     */
    @GetMapping("/exam-paper/{examPaperId}/active")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ExamModule>>> getActiveModules(@PathVariable @NotNull Long examPaperId) {
        try {
            List<ExamModule> modules = examModuleService.getActiveModulesByExamPaperId(examPaperId);
            return ResponseEntity.ok(ApiResponse.success(modules));
        } catch (Exception e) {
            log.error("获取活跃模块失败: examPaperId={}, error={}", examPaperId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取活跃模块失败"));
        }
    }

    /**
     * 获取试卷的非活跃模块
     */
    @GetMapping("/exam-paper/{examPaperId}/inactive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ExamModule>>> getInactiveModules(@PathVariable @NotNull Long examPaperId) {
        try {
            List<ExamModule> modules = examModuleService.getInactiveModulesByExamPaperId(examPaperId);
            return ResponseEntity.ok(ApiResponse.success(modules));
        } catch (Exception e) {
            log.error("获取非活跃模块失败: examPaperId={}, error={}", examPaperId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取非活跃模块失败"));
        }
    }

    /**
     * 获取模块类型分布统计
     */
    @GetMapping("/statistics/type-distribution")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getModuleTypeDistribution() {
        try {
            List<Object[]> distribution = examModuleService.findModuleTypeDistribution();
            return ResponseEntity.ok(ApiResponse.success(distribution));
        } catch (Exception e) {
            log.error("获取模块类型分布失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取模块类型分布失败"));
        }
    }

    /**
     * 获取模块统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getModuleStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", examModuleService.count());
            statistics.put("withRubric", examModuleService.countModulesWithRubric());
            statistics.put("withoutRubric", examModuleService.countModulesWithoutRubric());
            statistics.put("typeDistribution", examModuleService.findModuleTypeDistribution());

            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("获取模块统计信息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取模块统计信息失败"));
        }
    }

    // ==================== 数据传输对象 ====================

    /**
     * 创建考试模块请求
     */
    public static class CreateExamModuleRequest {
        @NotNull(message = "试卷ID不能为空")
        private Long examPaperId;
        
        @NotNull(message = "模块类型不能为空")
        private ExamModule.ModuleType moduleType;
        
        @NotNull(message = "显示顺序不能为空")
        private Integer displayOrder;
        
        private String configJson;
        private Long score;

        // Getters and Setters
        public Long getExamPaperId() { return examPaperId; }
        public void setExamPaperId(Long examPaperId) { this.examPaperId = examPaperId; }
        public ExamModule.ModuleType getModuleType() { return moduleType; }
        public void setModuleType(ExamModule.ModuleType moduleType) { this.moduleType = moduleType; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        public String getConfigJson() { return configJson; }
        public void setConfigJson(String configJson) { this.configJson = configJson; }
        public Long getScore() { return score; }
        public void setScore(Long score) { this.score = score; }
    }
}
