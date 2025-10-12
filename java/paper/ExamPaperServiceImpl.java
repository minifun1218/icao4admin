package org.icao4.eqasbackend2.impl.exam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.entity.exam.ExamPaper;
import org.icao4.eqasbackend2.repository.exam.ExamPaperRepository;
import org.icao4.eqasbackend2.service.exam.ExamPaperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 考试试卷服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamPaperServiceImpl implements ExamPaperService {
    
    private final ExamPaperRepository examPaperRepository;
    
    // ==================== 基础CRUD操作 ====================
    
    @Override
    public ExamPaper save(ExamPaper examPaper) {
        if (examPaper == null) {
            throw new IllegalArgumentException("考试试卷不能为空");
        }
        if (!validateExamPaper(examPaper)) {
            throw new IllegalArgumentException("考试试卷数据无效");
        }
        
        log.debug("保存考试试卷: id={}, code={}, name={}", 
                 examPaper.getId(), examPaper.getCode(), examPaper.getName());
        
        return examPaperRepository.save(examPaper);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExamPaper> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return examPaperRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> findAll() {
        return examPaperRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamPaper> findAll(Pageable pageable) {
        return examPaperRepository.findAll(pageable);
    }
    
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        
        log.debug("删除考试试卷: id={}", id);
        examPaperRepository.deleteById(id);
    }
    
    @Override
    public void delete(ExamPaper examPaper) {
        if (examPaper == null) {
            return;
        }
        
        log.debug("删除考试试卷: id={}", examPaper.getId());
        examPaperRepository.delete(examPaper);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return examPaperRepository.existsById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long count() {
        return examPaperRepository.count();
    }
    
    // ==================== 查询操作 ====================
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExamPaper> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return examPaperRepository.findByCode(code.trim());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> findByNameContaining(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        return examPaperRepository.findByNameContaining(name.trim());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamPaper> findByNameContaining(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return Page.empty();
        }
        return examPaperRepository.findByNameContaining(name.trim(), pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> findByTotalDurationMinBetween(Integer minDuration, Integer maxDuration) {
        if (minDuration == null || maxDuration == null) {
            return List.of();
        }
        return examPaperRepository.findByTotalDurationMinBetween(minDuration, maxDuration);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamPaper> findByTotalDurationMinBetween(Integer minDuration, Integer maxDuration, Pageable pageable) {
        if (minDuration == null || maxDuration == null) {
            return Page.empty();
        }
        return examPaperRepository.findByTotalDurationMinBetween(minDuration, maxDuration, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return List.of();
        }
        return examPaperRepository.findByCreatedAtBetween(startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamPaper> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (startTime == null || endTime == null) {
            return Page.empty();
        }
        return examPaperRepository.findByCreatedAtBetween(startTime, endTime, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamPaper> findAllByOrderByCreatedAtDesc(Pageable pageable) {
        return examPaperRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    // ==================== 验证操作 ====================
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return examPaperRepository.existsByCode(code.trim());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateExamPaper(ExamPaper examPaper) {
        if (examPaper == null) {
            return false;
        }
        
        return examPaper.getCode() != null && !examPaper.getCode().trim().isEmpty()
                && examPaper.getName() != null && !examPaper.getName().trim().isEmpty()
                && isDurationValid(examPaper.getTotalDurationMin());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isCodeUnique(String code, Long excludeId) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        Optional<ExamPaper> existingPaper = findByCode(code.trim());
        if (existingPaper.isEmpty()) {
            return true;
        }
        
        return excludeId != null && existingPaper.get().getId().equals(excludeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isDurationValid(Integer totalDurationMin) {
        return totalDurationMin != null && totalDurationMin > 0 && totalDurationMin <= 1440; // 最多24小时
    }
    
    // ==================== 统计操作 ====================
    
    @Override
    @Transactional(readOnly = true)
    public long countByDurationRange(Integer minDuration, Integer maxDuration) {
        if (minDuration == null || maxDuration == null) {
            return 0;
        }
        return examPaperRepository.countByDurationRange(minDuration, maxDuration);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getAverageDuration() {
        return examPaperRepository.getAverageDuration();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getMaxDuration() {
        return examPaperRepository.getMaxDuration();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getMinDuration() {
        return examPaperRepository.getMinDuration();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDurationDistribution() {
        // 可以实现时长分布统计逻辑
        return List.of();
    }
    
    // ==================== 业务操作 ====================
    
    @Override
    public ExamPaper createExamPaper(String code, String name, Integer totalDurationMin, String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("试卷代码不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("试卷名称不能为空");
        }
        if (existsByCode(code.trim())) {
            throw new IllegalArgumentException("试卷代码已存在: " + code);
        }
        if (!isDurationValid(totalDurationMin)) {
            throw new IllegalArgumentException("试卷时长无效: " + totalDurationMin);
        }
        
        ExamPaper examPaper = new ExamPaper();
        examPaper.setCode(code.trim());
        examPaper.setName(name.trim());
        examPaper.setTotalDurationMin(totalDurationMin);
        examPaper.setDescription(description);
        
        return save(examPaper);
    }
    
    @Override
    public ExamPaper updateExamPaper(Long id, String name, Integer totalDurationMin, String description) {
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("考试试卷不存在: " + id);
        }
        
        ExamPaper examPaper = paperOpt.get();
        if (name != null && !name.trim().isEmpty()) {
            examPaper.setName(name.trim());
        }
        if (isDurationValid(totalDurationMin)) {
            examPaper.setTotalDurationMin(totalDurationMin);
        }
        examPaper.setDescription(description);
        
        return save(examPaper);
    }
    
    @Override
    public ExamPaper updateExamPaperCode(Long id, String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("试卷代码不能为空");
        }
        if (!isCodeUnique(code.trim(), id)) {
            throw new IllegalArgumentException("试卷代码已存在: " + code);
        }
        
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("考试试卷不存在: " + id);
        }
        
        ExamPaper examPaper = paperOpt.get();
        examPaper.setCode(code.trim());
        
        return save(examPaper);
    }
    
    @Override
    public ExamPaper updateExamPaperName(Long id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("试卷名称不能为空");
        }
        
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("考试试卷不存在: " + id);
        }
        
        ExamPaper examPaper = paperOpt.get();
        examPaper.setName(name.trim());
        
        return save(examPaper);
    }
    
    @Override
    public ExamPaper updateExamPaperDuration(Long id, Integer totalDurationMin) {
        if (!isDurationValid(totalDurationMin)) {
            throw new IllegalArgumentException("试卷时长无效: " + totalDurationMin);
        }
        
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("考试试卷不存在: " + id);
        }
        
        ExamPaper examPaper = paperOpt.get();
        examPaper.setTotalDurationMin(totalDurationMin);
        
        return save(examPaper);
    }
    
    @Override
    public ExamPaper updateExamPaperDescription(Long id, String description) {
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("考试试卷不存在: " + id);
        }
        
        ExamPaper examPaper = paperOpt.get();
        examPaper.setDescription(description);
        
        return save(examPaper);
    }
    
    @Override
    public ExamPaper copyExamPaper(Long id, String newCode, String newName) {
        if (newCode == null || newCode.trim().isEmpty()) {
            throw new IllegalArgumentException("新试卷代码不能为空");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("新试卷名称不能为空");
        }
        if (existsByCode(newCode.trim())) {
            throw new IllegalArgumentException("新试卷代码已存在: " + newCode);
        }
        
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("考试试卷不存在: " + id);
        }
        
        ExamPaper original = paperOpt.get();
        ExamPaper copy = new ExamPaper();
        copy.setCode(newCode.trim());
        copy.setName(newName.trim());
        copy.setTotalDurationMin(original.getTotalDurationMin());
        copy.setDescription(original.getDescription());
        
        return save(copy);
    }
    
    @Override
    public List<ExamPaper> batchCreateExamPapers(List<ExamPaper> examPapers) {
        if (examPapers == null || examPapers.isEmpty()) {
            return List.of();
        }
        
        log.debug("批量创建考试试卷: count={}", examPapers.size());
        
        List<ExamPaper> validPapers = examPapers.stream()
                .filter(this::validateExamPaper)
                .filter(paper -> !existsByCode(paper.getCode()))
                .collect(Collectors.toList());
        
        return examPaperRepository.saveAll(validPapers);
    }
    
    @Override
    public List<ExamPaper> batchUpdateExamPapers(List<ExamPaper> examPapers) {
        if (examPapers == null || examPapers.isEmpty()) {
            return List.of();
        }
        
        log.debug("批量更新考试试卷: count={}", examPapers.size());
        
        List<ExamPaper> validPapers = examPapers.stream()
                .filter(this::validateExamPaper)
                .collect(Collectors.toList());
        
        return examPaperRepository.saveAll(validPapers);
    }
    
    @Override
    public void batchDeleteExamPapers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        log.debug("批量删除考试试卷: count={}", ids.size());
        examPaperRepository.deleteAllById(ids);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamPaper> searchExamPapers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll(pageable);
        }
        return findByNameContaining(keyword.trim(), pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> getRecentlyCreatedExamPapers(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        Page<ExamPaper> page = findAllByOrderByCreatedAtDesc(pageable);
        return page.getContent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> getMostPopularExamPapers(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        
        // 这里可以根据考试记录数量来排序
        // 目前返回最近创建的试卷作为占位符
        return getRecentlyCreatedExamPapers(limit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> getRecommendedExamPapers(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }
        
        // 这里可以实现推荐算法
        // 目前返回最近创建的试卷作为占位符
        return getRecentlyCreatedExamPapers(limit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getExamPaperStatistics(Long id) {
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            return null;
        }
        
        // 这里可以实现具体的统计逻辑
        return new Object(); // 占位符
    }
    
    @Override
    public String generateExamPaperCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%03d", new Random().nextInt(1000));
        return "EP" + timestamp + randomSuffix;
    }
    
    @Override
    public ExamPaper publishExamPaper(Long id) {
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("考试试卷不存在: " + id);
        }
        
        ExamPaper examPaper = paperOpt.get();
        // 这里可以添加发布逻辑，比如设置状态字段
        log.debug("发布考试试卷: id={}, code={}", id, examPaper.getCode());
        
        return save(examPaper);
    }
    
    @Override
    public ExamPaper unpublishExamPaper(Long id) {
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("考试试卷不存在: " + id);
        }
        
        ExamPaper examPaper = paperOpt.get();
        // 这里可以添加下架逻辑，比如设置状态字段
        log.debug("下架考试试卷: id={}, code={}", id, examPaper.getCode());
        
        return save(examPaper);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteExamPaper(Long id) {
        if (id == null) {
            return false;
        }
        
        // 这里可以检查是否有相关的考试记录或模块
        // 如果有，则不能删除
        return true; // 占位符
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object exportExamPaperConfig(Long id) {
        Optional<ExamPaper> paperOpt = findById(id);
        if (paperOpt.isEmpty()) {
            return null;
        }
        
        // 这里可以实现导出配置的逻辑
        return new Object(); // 占位符
    }
    
    @Override
    public ExamPaper importExamPaperConfig(Object config) {
        if (config == null) {
            throw new IllegalArgumentException("配置不能为空");
        }
        
        // 这里可以实现导入配置的逻辑
        // 目前返回空占位符
        return null;
    }
    
    @Override
    public int cleanupUnusedExamPapers(LocalDateTime beforeDate) {
        if (beforeDate == null) {
            return 0;
        }
        
        log.debug("清理未使用的考试试卷: beforeDate={}", beforeDate);
        // List<ExamPaper> oldPapers = findByCreatedAtBetween(LocalDateTime.MIN, beforeDate);
        
        // 这里可以添加逻辑检查试卷是否被使用
        // 如果未被使用，则删除
        
        return 0; // 占位符
    }
}
