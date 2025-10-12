package org.icao4.eqasbackend2.entity.exam;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试类型实体类
 */
@Entity
@Table(name = "exam_paper")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExamPaper {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String code;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(name = "total_duration_min", nullable = false)
    private Integer totalDurationMin;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 关联的考试模块
    @JsonIgnore
    @OneToMany(mappedBy = "examPaper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamModule> examModules;
}