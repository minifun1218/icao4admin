package org.icao4.eqasbackend2.impl.exam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.entity.exam.ExamModule;
import org.icao4.eqasbackend2.repository.exam.ExamModuleRepository;
import org.icao4.eqasbackend2.service.exam.ExamModuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 考试模块服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamModuleServiceImpl implements ExamModuleService {
    
    private final ExamModuleRepository examModuleRepository;
    
    // ==================== 基础CRUD操作 ====================
    
    @Override
    public ExamModule save(ExamModule examModule) {
        if (examModule == null) {
            throw new IllegalArgumentException("考试模块不能为空");
        }
        if (!validateModule(examModule)) {
            throw new IllegalArgumentException("考试模块数据无效");
        }
        
        log.debug("保存考试模块: id={}, examPaperId={}, moduleType={}", 
                 examModule.getId(), examModule.getExamPaperId(), examModule.getModuleType());
        
        return examModuleRepository.save(examModule);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExamModule> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return examModuleRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> findAll() {
        return examModuleRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamModule> findAll(Pageable pageable) {
        return examModuleRepository.findAll(pageable);
    }
    
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        
        log.debug("删除考试模块: id={}", id);
        examModuleRepository.deleteById(id);
    }
    
    @Override
    public void delete(ExamModule examModule) {
        if (examModule == null) {
            return;
        }
        
        log.debug("删除考试模块: id={}", examModule.getId());
        examModuleRepository.delete(examModule);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return examModuleRepository.existsById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long count() {
        return examModuleRepository.count();
    }
    
    // ==================== 查询操作 ====================
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> findByExamPaperId(Long examPaperId) {
        if (examPaperId == null) {
            return List.of();
        }
        return examModuleRepository.findByExamPaperId(examPaperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamModule> findByExamPaperId(Long examPaperId, Pageable pageable) {
        if (examPaperId == null) {
            return Page.empty();
        }
        return examModuleRepository.findByExamPaperId(examPaperId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> findByExamPaperIdOrderByDisplayOrderAsc(Long examPaperId) {
        if (examPaperId == null) {
            return List.of();
        }
        return examModuleRepository.findByExamPaperIdOrderByDisplayOrderAsc(examPaperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> findByModuleType(ExamModule.ModuleType moduleType) {
        if (moduleType == null) {
            return List.of();
        }
        return examModuleRepository.findByModuleType(moduleType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamModule> findByModuleType(ExamModule.ModuleType moduleType, Pageable pageable) {
        if (moduleType == null) {
            return Page.empty();
        }
        return examModuleRepository.findByModuleType(moduleType, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> findByScore(Long score) {
        if (score == null) {
            return List.of();
        }
        return examModuleRepository.findByScore(score);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamModule> findByScore(Long score, Pageable pageable) {
        if (score == null) {
            return Page.empty();
        }
        return examModuleRepository.findByScore(score, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return List.of();
        }
        return examModuleRepository.findByCreatedAtBetween(startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamModule> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (startTime == null || endTime == null) {
            return Page.empty();
        }
        return examModuleRepository.findByCreatedAtBetween(startTime, endTime, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> findByExamPaperIdIn(List<Long> examPaperIds) {
        if (examPaperIds == null || examPaperIds.isEmpty()) {
            return List.of();
        }
        return examModuleRepository.findByExamPaperIdIn(examPaperIds);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamModule> findByExamPaperIdIn(List<Long> examPaperIds, Pageable pageable) {
        if (examPaperIds == null || examPaperIds.isEmpty()) {
            return Page.empty();
        }
        return examModuleRepository.findByExamPaperIdIn(examPaperIds, pageable);
    }
    
    // ==================== 统计操作 ====================
    @Override
    @Transactional(readOnly = true)
    public long countByExamPaperId(Long examPaperId) {
        if (examPaperId == null) {
            return 0;
        }
        return examModuleRepository.countByExamPaperId(examPaperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByModuleType(ExamModule.ModuleType moduleType) {
        if (moduleType == null) {
            return 0;
        }
        return examModuleRepository.countByModuleType(moduleType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findModuleTypeDistribution() {
        return examModuleRepository.findModuleTypeDistribution();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countModulesWithRubric() {
        return examModuleRepository.countModulesWithRubric();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countModulesWithoutRubric() {
        return examModuleRepository.countModulesWithoutRubric();
    }
    
    // ==================== 验证操作 ====================
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByExamPaperId(Long examPaperId) {
        if (examPaperId == null) {
            return false;
        }
        return examModuleRepository.existsByExamPaperId(examPaperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByScore(Long score) {
        if (score == null) {
            return false;
        }
        return examModuleRepository.existsByScore(score);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateModule(ExamModule examModule) {
        if (examModule == null) {
            return false;
        }
        
        return examModule.getExamPaperId() != null && examModule.getExamPaperId() > 0
                && examModule.getModuleType() != null
                && examModule.getDisplayOrder() != null && examModule.getDisplayOrder() >= 0;
    }
    
    // ==================== 业务操作 ====================
    
    @Override
    public ExamModule createModule(Long examPaperId, ExamModule.ModuleType moduleType, Integer displayOrder, String configJson, Long score) {
        if (examPaperId == null || moduleType == null || displayOrder == null) {
            throw new IllegalArgumentException("试卷ID、模块类型和显示顺序不能为空");
        }
        
        ExamModule examModule = new ExamModule();
        examModule.setExamPaperId(examPaperId);
        examModule.setModuleType(moduleType);
        examModule.setDisplayOrder(displayOrder);
        examModule.setConfigJson(configJson);
        examModule.setScore(score);
        examModule.setActivate(true);
        
        return save(examModule);
    }
    
    @Override
    public ExamModule updateModuleConfig(Long id, String configJson) {
        Optional<ExamModule> moduleOpt = findById(id);
        if (moduleOpt.isEmpty()) {
            throw new IllegalArgumentException("考试模块不存在: " + id);
        }
        
        ExamModule examModule = moduleOpt.get();
        examModule.setConfigJson(configJson);
        
        return save(examModule);
    }
    
    @Override
    public ExamModule updateDisplayOrder(Long id, Integer displayOrder) {
        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException("显示顺序必须大于等于0");
        }
        
        Optional<ExamModule> moduleOpt = findById(id);
        if (moduleOpt.isEmpty()) {
            throw new IllegalArgumentException("考试模块不存在: " + id);
        }
        
        ExamModule examModule = moduleOpt.get();
        examModule.setDisplayOrder(displayOrder);
        
        return save(examModule);
    }
    
    @Override
    public ExamModule updateModuleScore(Long id, Long score) {
        Optional<ExamModule> moduleOpt = findById(id);
        if (moduleOpt.isEmpty()) {
            throw new IllegalArgumentException("考试模块不存在: " + id);
        }
        
        ExamModule examModule = moduleOpt.get();
        examModule.setScore(score);
        
        return save(examModule);
    }
    
    @Override
    public ExamModule copyModule(Long id) {
        Optional<ExamModule> moduleOpt = findById(id);
        if (moduleOpt.isEmpty()) {
            throw new IllegalArgumentException("考试模块不存在: " + id);
        }
        
        ExamModule original = moduleOpt.get();
        ExamModule copy = new ExamModule();
        copy.setExamPaperId(original.getExamPaperId());
        copy.setModuleType(original.getModuleType());
        copy.setDisplayOrder(original.getDisplayOrder() + 1);
        copy.setConfigJson(original.getConfigJson());
        copy.setScore(original.getScore());
        copy.setActivate(original.isActivate());
        
        return save(copy);
    }
    
    @Override
    public ExamModule copyModuleToExamPaper(Long id, Long targetExamPaperId) {
        if (targetExamPaperId == null) {
            throw new IllegalArgumentException("目标试卷ID不能为空");
        }
        
        ExamModule copy = copyModule(id);
        copy.setExamPaperId(targetExamPaperId);
        
        return save(copy);
    }
    
    @Override
    public List<ExamModule> batchCreateModules(List<ExamModule> modules) {
        if (modules == null || modules.isEmpty()) {
            return List.of();
        }
        
        log.debug("批量创建考试模块: count={}", modules.size());
        
        List<ExamModule> validModules = modules.stream()
                .filter(this::validateModule)
                .collect(Collectors.toList());
        
        return examModuleRepository.saveAll(validModules);
    }
    
    @Override
    public List<ExamModule> batchUpdateModules(List<ExamModule> modules) {
        if (modules == null || modules.isEmpty()) {
            return List.of();
        }
        
        log.debug("批量更新考试模块: count={}", modules.size());
        
        List<ExamModule> validModules = modules.stream()
                .filter(this::validateModule)
                .collect(Collectors.toList());
        
        return examModuleRepository.saveAll(validModules);
    }
    
    @Override
    public void batchDeleteModules(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        log.debug("批量删除考试模块: count={}", ids.size());
        examModuleRepository.deleteAllById(ids);
    }
    
    @Override
    public void deleteByExamPaperId(Long examPaperId) {
        if (examPaperId == null) {
            return;
        }
        
        log.debug("删除试卷的所有模块: examPaperId={}", examPaperId);
        examModuleRepository.deleteByExamPaperId(examPaperId);
    }
    
    @Override
    public List<ExamModule> reorderModules(Long examPaperId, List<Long> moduleIds) {
        if (examPaperId == null || moduleIds == null || moduleIds.isEmpty()) {
            return List.of();
        }
        
        log.debug("重新排序模块: examPaperId={}, moduleCount={}", examPaperId, moduleIds.size());
        
        List<ExamModule> modules = findByExamPaperId(examPaperId);
        
        for (int i = 0; i < moduleIds.size(); i++) {
            Long moduleId = moduleIds.get(i);
            final int displayOrder = i + 1;
            modules.stream()
                    .filter(module -> module.getId().equals(moduleId))
                    .findFirst()
                    .ifPresent(module -> module.setDisplayOrder(displayOrder));
        }
        
        return examModuleRepository.saveAll(modules);
    }
    
    @Override
    public ExamModule toggleModuleActivation(Long id, boolean isActivate) {
        Optional<ExamModule> moduleOpt = findById(id);
        if (moduleOpt.isEmpty()) {
            throw new IllegalArgumentException("考试模块不存在: " + id);
        }
        
        ExamModule examModule = moduleOpt.get();
        examModule.setActivate(isActivate);
        
        return save(examModule);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> getActiveModulesByExamPaperId(Long examPaperId) {
        if (examPaperId == null) {
            return List.of();
        }
        return findByExamPaperId(examPaperId).stream()
                .filter(ExamModule::isActivate)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamModule> getInactiveModulesByExamPaperId(Long examPaperId) {
        if (examPaperId == null) {
            return List.of();
        }
        return findByExamPaperId(examPaperId).stream()
                .filter(module -> !module.isActivate())
                .collect(Collectors.toList());
    }
}
