package org.icao4.eqasbackend2.entity.opi;

/**
 * OPI话题实体类
 * 对应opi_topics表
 */

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.icao4.eqasbackend2.entity.exam.ExamModule;

import java.time.LocalDateTime;

/**
 * OPI（口语面试）话题实体类
 * 对应表：opi_topics
 */
@Entity
@Table(
    name = "opi_topics",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_module_topic", columnNames = {"module_id", "t_order"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpiTopic {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 外键：所属模块ID */
    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    /** 话题顺序（同一模块内唯一） */
    @Column(name = "t_order", nullable = false)
    private Integer order;

    /** 话题标题 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 话题描述（可选） */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 创建时间 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    /** 关联的考试模块 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", insertable = false, updatable = false)
    private ExamModule examModule;
}