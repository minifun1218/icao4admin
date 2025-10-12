package org.icao4.eqasbackend2.entity.opi;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OPI回答实体类
 * 对应数据库表：opi_responses
 */
@Entity
@Table(name = "opi_responses", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_attempt_q", columnNames = {"question_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"question"})
public class OpiResponse {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    /**
     * 问题ID
     */
    @NotNull(message = "问题ID不能为空")
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 回答音频ID
     */
    @Column(name = "answer_audio_id")
    private Long answerAudioId;

    /**
     * ASR识别文本
     */
    @Column(name = "asr_text", columnDefinition = "TEXT")
    private String asrText;

    /**
     * 回答时间
     */
    @NotNull(message = "回答时间不能为空")
    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    /**
     * 耗时（毫秒）
     */
    @NotNull(message = "耗时不能为空")
    @Min(value = 0, message = "耗时不能为负数")
    @Column(name = "elapsed_ms", nullable = false)
    private Integer elapsedMs;

    /**
     * 评分
     */
    @DecimalMin(value = "0.00", message = "评分不能小于0")
    @DecimalMax(value = "10.00", message = "评分不能大于10")
    @Column(name = "score", precision = 6)
    private BigDecimal score;

    /**
     * 评分详情JSON
     */
    @Column(name = "score_detail_json", columnDefinition = "JSON")
    private String scoreDetailJson;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==================== 关联关系 ====================

    /**
     * 关联的问题
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_opi_response_question"))
    private OpiQuestion question;

    // ==================== JPA生命周期回调 ====================

    /**
     * 保存前设置默认值
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }

    // ==================== 业务方法 ====================



    /**
     * 检查是否有音频回答
     */
    public boolean hasAudioAnswer() {
        return answerAudioId != null;
    }

    /**
     * 检查是否有ASR文本
     */
    public boolean hasAsrText() {
        return asrText != null && !asrText.trim().isEmpty();
    }

    /**
     * 检查是否已评分
     */
    public boolean isScored() {
        return score != null;
    }

    /**
     * 检查是否有评分详情
     */
    public boolean hasScoreDetail() {
        return scoreDetailJson != null && !scoreDetailJson.trim().isEmpty();
    }

    /**
     * 获取耗时（秒）
     */
    public Double getElapsedSeconds() {
        return elapsedMs != null ? elapsedMs / 1000.0 : null;
    }

    /**
     * 检查是否超时回答
     */
    public boolean isOvertime(Integer allowedSeconds) {
        if (allowedSeconds == null || elapsedMs == null) {
            return false;
        }
        return elapsedMs > allowedSeconds * 1000;
    }

    /**
     * 检查是否快速回答（小于5秒）
     */
    public boolean isQuickAnswer() {
        return elapsedMs != null && elapsedMs < 5000;
    }

    /**
     * 检查是否慢速回答（大于等于允许时间的80%）
     */
    public boolean isSlowAnswer(Integer allowedSeconds) {
        if (allowedSeconds == null || elapsedMs == null) {
            return false;
        }
        return elapsedMs >= allowedSeconds * 800; // 80% of allowed time in ms
    }

    /**
     * 获取评分等级
     */
    public String getScoreGrade() {
        if (score == null) {
            return "未评分";
        }
        double scoreValue = score.doubleValue();
        if (scoreValue >= 9.0) {
            return "优秀";
        } else if (scoreValue >= 8.0) {
            return "良好";
        } else if (scoreValue >= 7.0) {
            return "中等";
        } else if (scoreValue >= 6.0) {
            return "及格";
        } else {
            return "不及格";
        }
    }

    /**
     * 获取回答时长描述
     */
    public String getElapsedTimeDescription() {
        if (elapsedMs == null) {
            return "未知";
        }
        double seconds = elapsedMs / 1000.0;
        if (seconds < 5) {
            return "很快";
        } else if (seconds < 30) {
            return "较快";
        } else if (seconds < 60) {
            return "正常";
        } else if (seconds < 120) {
            return "较慢";
        } else {
            return "很慢";
        }
    }

    /**
     * 设置评分
     */
    public void setScore(BigDecimal scoreValue) {
        if (scoreValue != null) {
            if (scoreValue.compareTo(BigDecimal.ZERO) < 0) {
                this.score = BigDecimal.ZERO;
            } else if (scoreValue.compareTo(BigDecimal.TEN) > 0) {
                this.score = BigDecimal.TEN;
            } else {
                this.score = scoreValue;
            }
        } else {
            this.score = null;
        }
    }

    /**
     * 获取评分值
     */
    public Double getScoreValue() {
        return score != null ? score.doubleValue() : null;
    }

    /**
     * 更新ASR文本
     */
    public void updateAsrText(String newAsrText) {
        this.asrText = newAsrText;
    }

    /**
     * 更新评分详情
     */
    public void updateScoreDetail(String newScoreDetailJson) {
        this.scoreDetailJson = newScoreDetailJson;
    }

    /**
     * 清除评分
     */
    public void clearScore() {
        this.score = null;
        this.scoreDetailJson = null;
    }


    /**
     * 获取回答摘要信息
     */
    public String getSummary() {
        String scoreStr = score != null ? String.format("%.2f分", score.doubleValue()) : "未评分";
        String timeStr = String.format("%.1f秒", getElapsedSeconds());
        return String.format("回答 (耗时: %s, 评分: %s)", timeStr, scoreStr);
    }

    /**
     * 检查是否为有效回答
     */
    public boolean isValidAnswer() {
        return hasAudioAnswer() || hasAsrText();
    }


}