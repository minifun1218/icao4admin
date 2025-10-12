package org.icao4.eqasbackend2.entity.atc_sim;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.icao4.eqasbackend2.entity.exam.ExamModule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 空中交通管制场景实体类
 * 对应数据库表：atc_scenarios
 */
@Entity
@Table(name = "atc_scenarios")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"turns"})
@Accessors(chain = true)
public class AtcScenario {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 机场ID
     */
    @Column(name = "airport_id", nullable = false)
    private Long airportId;


    @Column(name = "module_id", nullable = false)
    @NotNull(message = "模块ID不能为空")
    private Long moduleId;


    /**
     * 场景标题
     */
    @Column(name = "title", nullable = false, length = 255)
    @NotBlank(message = "场景标题不能为空")
    @Size(max = 255, message = "场景标题长度不能超过255个字符")
    private String title;

    /**
     * 场景描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 2000, message = "场景描述长度不能超过2000个字符")
    private String description;

    /**
     * 场景类型
     */
    @Column(name = "scenario_type", length = 50)
    @Size(max = 50, message = "场景类型长度不能超过50个字符")
    private String scenarioType;

    /**
     * 难度等级 (1-5)
     */
    @Column(name = "difficulty_level")
    @Min(value = 1, message = "难度等级最小值为1")
    @Max(value = 5, message = "难度等级最大值为5")
    private Integer difficultyLevel;

    /**
     * 预计持续时间（分钟）
     */
    @Column(name = "estimated_duration")
    @Min(value = 1, message = "预计持续时间最小值为1分钟")
    @Max(value = 180, message = "预计持续时间最大值为180分钟")
    private Integer estimatedDuration;

    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "激活状态不能为空")
    private Boolean isActive = true;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 关联的交通管制轮次
     */
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AtcTurn> turns;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", insertable = false, updatable = false)
    private ExamModule examModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_id", insertable = false, updatable = false)
    private Airport airport;

    /**
     * 自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.difficultyLevel == null) {
            this.difficultyLevel = 1;
        }
    }

    /**
     * 自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 辅助方法

    /**
     * 获取轮次数量
     */
    public int getTurnCount() {
        return turns != null ? turns.size() : 0;
    }

    /**
     * 检查是否有轮次
     */
    public boolean hasTurns() {
        return turns != null && !turns.isEmpty();
    }

    /**
     * 获取难度等级描述
     */
    public String getDifficultyDescription() {
        if (difficultyLevel == null) {
            return "未设置";
        }
        switch (difficultyLevel) {
            case 1:
                return "初级";
            case 2:
                return "初中级";
            case 3:
                return "中级";
            case 4:
                return "中高级";
            case 5:
                return "高级";
            default:
                return "未知";
        }
    }

    /**
     * 获取持续时间描述
     */
    public String getDurationDescription() {
        if (estimatedDuration == null) {
            return "未设置";
        }
        if (estimatedDuration < 60) {
            return estimatedDuration + "分钟";
        } else {
            int hours = estimatedDuration / 60;
            int minutes = estimatedDuration % 60;
            if (minutes == 0) {
                return hours + "小时";
            } else {
                return hours + "小时" + minutes + "分钟";
            }
        }
    }

    /**
     * 检查场景是否完整
     */
    public boolean isComplete() {
        return title != null && !title.trim().isEmpty() &&
               scenarioType != null && !scenarioType.trim().isEmpty() &&
               difficultyLevel != null && difficultyLevel >= 1 && difficultyLevel <= 5 &&
               estimatedDuration != null && estimatedDuration > 0;
    }

    /**
     * 检查是否为简单场景（轮次数少于等于5）
     */
    public boolean isSimpleScenario() {
        return getTurnCount() <= 5;
    }

    /**
     * 检查是否为复杂场景（轮次数大于10）
     */
    public boolean isComplexScenario() {
        return getTurnCount() > 10;
    }

    /**
     * 检查是否为短时场景（持续时间小于等于30分钟）
     */
    public boolean isShortScenario() {
        return estimatedDuration != null && estimatedDuration <= 30;
    }

    /**
     * 检查是否为长时场景（持续时间大于等于120分钟）
     */
    public boolean isLongScenario() {
        return estimatedDuration != null && estimatedDuration >= 120;
    }

    /**
     * 获取场景状态描述
     */
    public String getStatusDescription() {
        if (isActive == null || !isActive) {
            return "未激活";
        }
        if (!isComplete()) {
            return "不完整";
        }
        if (!hasTurns()) {
            return "无轮次";
        }
        return "正常";
    }

    /**
     * 检查场景类型是否匹配
     */
    public boolean isScenarioType(String type) {
        return scenarioType != null && scenarioType.equalsIgnoreCase(type);
    }

    /**
     * 检查难度等级是否匹配
     */
    public boolean isDifficultyLevel(Integer level) {
        return difficultyLevel != null && difficultyLevel.equals(level);
    }

    /**
     * 检查是否在指定难度范围内
     */
    public boolean isDifficultyInRange(Integer minLevel, Integer maxLevel) {
        if (difficultyLevel == null) {
            return false;
        }
        return difficultyLevel >= minLevel && difficultyLevel <= maxLevel;
    }

    /**
     * 检查持续时间是否在指定范围内
     */
    public boolean isDurationInRange(Integer minDuration, Integer maxDuration) {
        if (estimatedDuration == null) {
            return false;
        }
        return estimatedDuration >= minDuration && estimatedDuration <= maxDuration;
    }
}