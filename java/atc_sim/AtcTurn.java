package org.icao4.eqasbackend2.entity.atc_sim;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 空中交通管制轮次实体类
 * 对应数据库表：atc_turns
 */
@Entity
@Table(name = "atc_turns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"scenario", "responses"})
public class AtcTurn {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 场景ID
     */
    @NotNull(message = "场景ID不能为空")
    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;

    /**
     * 轮次序号
     */
    @NotNull(message = "轮次序号不能为空")
    @Min(value = 1, message = "轮次序号必须大于0")
    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    /**
     * 说话者类型（pilot/controller）
     */
    @NotBlank(message = "说话者类型不能为空")
    @Size(max = 20, message = "说话者类型长度不能超过20个字符")
    @Column(name = "speaker_type", nullable = false, length = 20)
    private String speakerType;

    /**
     * 音频文件路径
     */
    @Size(max = 500, message = "音频文件路径长度不能超过500个字符")
    @Column(name = "audio_file_path", length = 500)
    private String audioFilePath;

    /**
     * 转录文本
     */
    @Column(name = "transcript_text", columnDefinition = "TEXT")
    private String transcriptText;

    /**
     * 预期回答
     */
    @Column(name = "expected_response", columnDefinition = "TEXT")
    private String expectedResponse;

    /**
     * 评分标准
     */
    @Column(name = "scoring_criteria", columnDefinition = "TEXT")
    private String scoringCriteria;

    /**
     * 最大分数
     */
    @DecimalMin(value = "0.0", message = "最大分数不能小于0")
    @Column(name = "max_score", precision = 5)
    private Double maxScore;

    /**
     * 音频时长（秒）
     */
    @Min(value = 0, message = "音频时长不能小于0")
    @Column(name = "audio_duration")
    private Integer audioDuration;

    /**
     * 难度等级（1-5）
     */
    @Min(value = 1, message = "难度等级不能小于1")
    @Max(value = 5, message = "难度等级不能大于5")
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    /**
     * 是否必答
     */
    @Builder.Default
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    /**
     * 是否激活
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
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

    // 关联关系

    /**
     * 关联的场景
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    private AtcScenario scenario;

    /**
     * 关联的回答记录
     */
    @OneToMany(mappedBy = "atcTurn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AtcTurnResponse> responses;

    // JPA生命周期回调

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 设置默认值
        if (this.isRequired == null) {
            this.isRequired = true;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.difficultyLevel == null) {
            this.difficultyLevel = 3; // 默认中等难度
        }
        if (this.maxScore == null) {
            this.maxScore = 10.0; // 默认满分10分
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 辅助方法

    /**
     * 检查是否有音频文件
     */
    public boolean hasAudioFile() {
        return this.audioFilePath != null && !this.audioFilePath.trim().isEmpty();
    }

    /**
     * 检查是否有转录文本
     */
    public boolean hasTranscript() {
        return this.transcriptText != null && !this.transcriptText.trim().isEmpty();
    }

    /**
     * 检查是否有预期回答
     */
    public boolean hasExpectedResponse() {
        return this.expectedResponse != null && !this.expectedResponse.trim().isEmpty();
    }

    /**
     * 检查是否有评分标准
     */
    public boolean hasScoringCriteria() {
        return this.scoringCriteria != null && !this.scoringCriteria.trim().isEmpty();
    }

    /**
     * 获取说话者类型描述
     */
    public String getSpeakerTypeDescription() {
        if (this.speakerType == null) {
            return "未知";
        }

        switch (this.speakerType.toLowerCase()) {
            case "pilot":
                return "飞行员";
            case "controller":
                return "管制员";
            default:
                return this.speakerType;
        }
    }

    /**
     * 获取难度等级描述
     */
    public String getDifficultyDescription() {
        if (this.difficultyLevel == null) {
            return "未设置";
        }

        switch (this.difficultyLevel) {
            case 1:
                return "非常简单";
            case 2:
                return "简单";
            case 3:
                return "中等";
            case 4:
                return "困难";
            case 5:
                return "非常困难";
            default:
                return "未知难度";
        }
    }

    /**
     * 获取音频时长描述
     */
    public String getAudioDurationDescription() {
        if (this.audioDuration == null || this.audioDuration <= 0) {
            return "未知";
        }

        int minutes = this.audioDuration / 60;
        int seconds = this.audioDuration % 60;

        if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 检查轮次完整性
     */
    public boolean isComplete() {
        return this.scenarioId != null &&
                this.turnNumber != null &&
                this.speakerType != null &&
                !this.speakerType.trim().isEmpty() &&
                this.hasAudioFile() &&
                this.hasTranscript();
    }

    /**
     * 检查是否可以评分
     */
    public boolean canBeScored() {
        return this.isComplete() &&
                this.hasExpectedResponse() &&
                this.hasScoringCriteria() &&
                this.maxScore != null &&
                this.maxScore > 0;
    }

    /**
     * 获取回答数量
     */
    public int getResponseCount() {
        return this.responses != null ? this.responses.size() : 0;
    }

    /**
     * 检查是否为飞行员轮次
     */
    public boolean isPilotTurn() {
        return "pilot".equalsIgnoreCase(this.speakerType);
    }

    /**
     * 检查是否为管制员轮次
     */
    public boolean isControllerTurn() {
        return "controller".equalsIgnoreCase(this.speakerType);
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        if (this.isActive == null || !this.isActive) {
            return "未激活";
        }

        if (!this.isComplete()) {
            return "不完整";
        }

        if (this.canBeScored()) {
            return "可评分";
        }

        return "已激活";
    }

    /**
     * 复制轮次（不包括ID和时间戳）
     */
    public AtcTurn copy() {
        return AtcTurn.builder()
                .scenarioId(this.scenarioId)
                .turnNumber(this.turnNumber)
                .speakerType(this.speakerType)
                .audioFilePath(this.audioFilePath)
                .transcriptText(this.transcriptText)
                .expectedResponse(this.expectedResponse)
                .scoringCriteria(this.scoringCriteria)
                .maxScore(this.maxScore)
                .audioDuration(this.audioDuration)
                .difficultyLevel(this.difficultyLevel)
                .isRequired(this.isRequired)
                .isActive(this.isActive)
                .build();
    }

    /**
     * 验证轮次数据
     */
    public boolean isValid() {
        return this.scenarioId != null &&
                this.turnNumber != null &&
                this.turnNumber > 0 &&
                this.speakerType != null &&
                !this.speakerType.trim().isEmpty() &&
                (this.maxScore == null || this.maxScore >= 0) &&
                (this.audioDuration == null || this.audioDuration >= 0) &&
                (this.difficultyLevel == null || (this.difficultyLevel >= 1 && this.difficultyLevel <= 5));
    }

    /**
     * 重置轮次数据
     */
    public void reset() {
        this.audioFilePath = null;
        this.transcriptText = null;
        this.expectedResponse = null;
        this.scoringCriteria = null;
        this.maxScore = 10.0;
        this.audioDuration = null;
        this.difficultyLevel = 3;
        this.isRequired = true;
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
}