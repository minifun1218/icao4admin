package org.icao4.eqasbackend2.entity.exam;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 考试模块实体类，用于定义试卷中的一个组成部分，如听力、口语等。
 * 对应数据库表：exam_modules
 */
@Entity
@Table(name = "exam_modules")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExamModule {

    /**
     * 模块ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的考试试卷ID
     * 用于将模块与特定的考试试卷进行绑定
     */
    @Column(name = "exam_paper_id")
    private Long examPaperId;

    /**
     * 模块类型，使用枚举存储
     * 定义了该模块的具体功能，如听力选择题、口语面试等
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", nullable = false)
    private ModuleType moduleType;

    /**
     * 模块在试卷中的显示顺序
     * 用于确定模块的执行顺序
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * 模块的配置信息，以JSON格式存储
     * 包含该模块特有的参数设置
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_json", columnDefinition = "JSON")
    private String configJson;

    /**
     * 得分
     * 关联到具体的评分标准实体
     */
    @Column(name = "score")
    private Long score;

    /**
     * 模块创建时间
     * 由数据库自动设置，不可更新
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @Column(name = "is_activate",  nullable = false)
    private boolean isActivate;

    // ==================== 关联关系 ====================

    /**
     * 关联的考试试卷实体
     * 一个模块属于一个试卷，但一个试卷可以有多个模块
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_paper_id", insertable = false, updatable = false)
    private ExamPaper examPaper;

    /**
     * 模块类型枚举
     */
    public enum ModuleType {
        LISTENING_MCQ,  // 听力理解（多选题）
        STORY_RETELL,   // 故事复述
        LISTENING_SA,   // 听力简答题
        ATC_SIM,        // 模拟通话
        OPI             // 口语面试
    }
}