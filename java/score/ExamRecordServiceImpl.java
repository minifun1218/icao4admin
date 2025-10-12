package org.icao4.eqasbackend2.impl.exam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.entity.exam.ExamRecord;
import org.icao4.eqasbackend2.repository.exam.ExamRecordRepository;
import org.icao4.eqasbackend2.service.exam.ExamRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 考试记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamRecordServiceImpl implements ExamRecordService {
    
    private final ExamRecordRepository examRecordRepository;
    
    // ==================== 基础CRUD操作 ====================
    
    @Override
    public ExamRecord save(ExamRecord examRecord) {
        if (examRecord == null) {
            throw new IllegalArgumentException("考试记录不能为空");
        }
        if (!validateExamRecord(examRecord)) {
            throw new IllegalArgumentException("考试记录数据无效");
        }
        
        log.debug("保存考试记录: id={}, examPaperId={}, userId={}", 
                 examRecord.getId(), examRecord.getExamPaperId(), examRecord.getUserId());
        
        return examRecordRepository.save(examRecord);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExamRecord> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return examRecordRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> findAll() {
        return examRecordRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> findAll(Pageable pageable) {
        return examRecordRepository.findAll(pageable);
    }
    
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        
        log.debug("删除考试记录: id={}", id);
        examRecordRepository.deleteById(id);
    }
    
    @Override
    public void delete(ExamRecord examRecord) {
        if (examRecord == null) {
            return;
        }
        
        log.debug("删除考试记录: id={}", examRecord.getId());
        examRecordRepository.delete(examRecord);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return examRecordRepository.existsById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long count() {
        return examRecordRepository.count();
    }
    
    // ==================== 查询操作 ====================
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> findByExamPaperId(Long examPaperId) {
        if (examPaperId == null) {
            return List.of();
        }
        return examRecordRepository.findByExamPaperId(examPaperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> findByExamPaperId(Long examPaperId, Pageable pageable) {
        if (examPaperId == null) {
            return Page.empty();
        }
        return examRecordRepository.findByExamPaperId(examPaperId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> findByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return examRecordRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> findByUserId(Long userId, Pageable pageable) {
        if (userId == null) {
            return Page.empty();
        }
        return examRecordRepository.findByUserId(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> findByUserIdAndExamPaperId(Long userId, Long examPaperId) {
        if (userId == null || examPaperId == null) {
            return List.of();
        }
        return examRecordRepository.findByUserIdAndExamPaperId(userId, examPaperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> findByUserIdAndExamPaperId(Long userId, Long examPaperId, Pageable pageable) {
        if (userId == null || examPaperId == null) {
            return Page.empty();
        }
        return examRecordRepository.findByUserIdAndExamPaperId(userId, examPaperId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> findByCreateAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return List.of();
        }
        return examRecordRepository.findByCreateAtBetween(startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> findByCreateAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (startTime == null || endTime == null) {
            return Page.empty();
        }
        return examRecordRepository.findByCreateAtBetween(startTime, endTime, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> findAllByOrderByCreateAtDesc(Pageable pageable) {
        return examRecordRepository.findAllByOrderByCreateAtDesc(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> findAllByOrderByCreateAtAsc(Pageable pageable) {
        return examRecordRepository.findAllByOrderByCreateAtAsc(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> findByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return examRecordRepository.findByIdIn(ids);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> findByExamPaperIdIn(List<Long> examPaperIds) {
        if (examPaperIds == null || examPaperIds.isEmpty()) {
            return List.of();
        }
        return examRecordRepository.findByExamPaperIdIn(examPaperIds);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> findByUserIdIn(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return examRecordRepository.findByUserIdIn(userIds);
    }
    
    // ==================== 统计操作 ====================
    
    @Override
    @Transactional(readOnly = true)
    public long countByExamPaperId(Long examPaperId) {
        if (examPaperId == null) {
            return 0;
        }
        return examRecordRepository.countByExamPaperId(examPaperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return examRecordRepository.countByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByCreateAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return examRecordRepository.countByCreateAtBetween(startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findMostPopularExamPapers(Pageable pageable) {
        return examRecordRepository.findMostPopularExamPapers(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findMostActiveUsers(Pageable pageable) {
        return examRecordRepository.findMostActiveUsers(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDailyExamStats(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return List.of();
        }
        return examRecordRepository.getDailyExamStats(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyExamStats() {
        return examRecordRepository.getMonthlyExamStats();
    }
    
    // ==================== 验证操作 ====================
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndExamPaperId(Long userId, Long examPaperId) {
        if (userId == null || examPaperId == null) {
            return false;
        }
        return examRecordRepository.existsByUserIdAndExamPaperId(userId, examPaperId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateExamRecord(ExamRecord examRecord) {
        if (examRecord == null) {
            return false;
        }
        
        return examRecord.getExamPaperId() != null && examRecord.getExamPaperId() > 0
                && examRecord.getUserId() != null && examRecord.getUserId() > 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isExamFinished(Long id) {
        Optional<ExamRecord> examRecordOpt = findById(id);
        if (examRecordOpt.isEmpty()) {
            return false;
        }
        
        ExamRecord examRecord = examRecordOpt.get();
        return examRecord.getIsFinished() != null && examRecord.getIsFinished();
    }
    
    // ==================== 业务操作 ====================
    
    @Override
    public ExamRecord createExamRecord(Long examPaperId, Long userId) {
        if (examPaperId == null || userId == null) {
            throw new IllegalArgumentException("试卷ID和用户ID不能为空");
        }
        
        ExamRecord examRecord = new ExamRecord();
        examRecord.setExamPaperId(examPaperId);
        examRecord.setUserId(userId);
        examRecord.setCreateAt(LocalDateTime.now());
        examRecord.setIsFinished(false);
        
        return save(examRecord);
    }
    
    @Override
    public ExamRecord startExam(Long examRecordId) {
        Optional<ExamRecord> examRecordOpt = findById(examRecordId);
        if (examRecordOpt.isEmpty()) {
            throw new IllegalArgumentException("考试记录不存在: " + examRecordId);
        }
        
        ExamRecord examRecord = examRecordOpt.get();
        if (examRecord.getCreateAt() == null) {
            examRecord.setCreateAt(LocalDateTime.now());
        }
        examRecord.setIsFinished(false);
        
        return save(examRecord);
    }
    
    @Override
    public ExamRecord finishExam(Long examRecordId) {
        Optional<ExamRecord> examRecordOpt = findById(examRecordId);
        if (examRecordOpt.isEmpty()) {
            throw new IllegalArgumentException("考试记录不存在: " + examRecordId);
        }
        
        ExamRecord examRecord = examRecordOpt.get();
        examRecord.setIsFinished(true);
        
        return save(examRecord);
    }
    
    @Override
    public ExamRecord updateExamStatus(Long id, Boolean isFinished) {
        Optional<ExamRecord> examRecordOpt = findById(id);
        if (examRecordOpt.isEmpty()) {
            throw new IllegalArgumentException("考试记录不存在: " + id);
        }
        
        ExamRecord examRecord = examRecordOpt.get();
        examRecord.setIsFinished(isFinished);
        
        return save(examRecord);
    }
    
    @Override
    public List<ExamRecord> batchCreateExamRecords(List<ExamRecord> examRecords) {
        if (examRecords == null || examRecords.isEmpty()) {
            return List.of();
        }
        
        log.debug("批量创建考试记录: count={}", examRecords.size());
        
        List<ExamRecord> validRecords = examRecords.stream()
                .filter(this::validateExamRecord)
                .collect(Collectors.toList());
        
        return examRecordRepository.saveAll(validRecords);
    }
    
    @Override
    public List<ExamRecord> batchUpdateExamRecords(List<ExamRecord> examRecords) {
        if (examRecords == null || examRecords.isEmpty()) {
            return List.of();
        }
        
        log.debug("批量更新考试记录: count={}", examRecords.size());
        
        List<ExamRecord> validRecords = examRecords.stream()
                .filter(this::validateExamRecord)
                .collect(Collectors.toList());
        
        return examRecordRepository.saveAll(validRecords);
    }
    
    @Override
    public void batchDeleteExamRecords(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        log.debug("批量删除考试记录: count={}", ids.size());
        examRecordRepository.deleteAllById(ids);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> getRecentExamRecordsByUserId(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        Page<ExamRecord> page = findByUserId(userId, pageable);
        return page.getContent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExamRecord> getLatestExamRecordByUserIdAndExamPaperId(Long userId, Long examPaperId) {
        if (userId == null || examPaperId == null) {
            return Optional.empty();
        }
        
        List<ExamRecord> records = findByUserIdAndExamPaperId(userId, examPaperId);
        return records.stream()
                .max((r1, r2) -> r1.getCreateAt().compareTo(r2.getCreateAt()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> getUserExamHistory(Long userId, Pageable pageable) {
        if (userId == null) {
            return Page.empty();
        }
        return findByUserId(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExamRecord> getExamPaperHistory(Long examPaperId, Pageable pageable) {
        if (examPaperId == null) {
            return Page.empty();
        }
        return findByExamPaperId(examPaperId, pageable);
    }
    
    @Override
    public int cleanupExpiredExamRecords(LocalDateTime beforeDate) {
        if (beforeDate == null) {
            return 0;
        }
        
        log.debug("清理过期考试记录: beforeDate={}", beforeDate);
        List<ExamRecord> expiredRecords = findByCreateAtBetween(LocalDateTime.MIN, beforeDate);
        
        if (!expiredRecords.isEmpty()) {
            List<Long> ids = expiredRecords.stream()
                    .map(ExamRecord::getId)
                    .collect(Collectors.toList());
            batchDeleteExamRecords(ids);
        }
        
        return expiredRecords.size();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getExamStatisticsReport(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        
        // 这里可以实现具体的统计报告逻辑
        // 返回包含各种统计数据的对象
        return new Object(); // 占位符
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamRecord> exportExamRecords(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return List.of();
        }
        
        log.debug("导出考试记录: startDate={}, endDate={}", startDate, endDate);
        return findByCreateAtBetween(startDate, endDate);
    }
    
    @Override
    public List<ExamRecord> importExamRecords(List<ExamRecord> examRecords) {
        if (examRecords == null || examRecords.isEmpty()) {
            return List.of();
        }
        
        log.debug("导入考试记录: count={}", examRecords.size());
        
        List<ExamRecord> importRecords = examRecords.stream()
                .map(record -> {
                    record.setId(null); // 重置ID以创建新记录
                    record.setCreateAt(LocalDateTime.now());
                    return record;
                })
                .collect(Collectors.toList());
        
        return batchCreateExamRecords(importRecords);
    }
}
