package org.icao4.eqasbackend2.entity.story_retell;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.icao4.eqasbackend2.entity.MediaAsset;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 复述回答实体类
 * 对应数据库表：retell_responses
 */
@Entity
@Table(name = "retell_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RetellResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联复述题目ID
     */
    @Column(name = "item_id", nullable = false)
    @NotNull(message = "复述题目ID不能为空")
    private Long itemId;

    /**
     * 回答音频资源ID
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
    @Column(name = "answered_at", nullable = false)
    @NotNull(message = "回答时间不能为空")
    private LocalDateTime answeredAt;

    /**
     * 用时（毫秒）
     */
    @Column(name = "elapsed_ms", nullable = false)
    @NotNull(message = "用时不能为空")
    @PositiveOrZero(message = "用时不能为负数")
    private Integer elapsedMs;

    /**
     * 得分
     */
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
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 关联的复述题目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"retellResponses"})
    private RetellItem retellItem;

    /**
     * 关联的回答音频资源
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_audio_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"retellResponses"})
    private MediaAsset answerAudio;

    /**
     * 获取用时的秒数表示
     */
    public Double getElapsedSeconds() {
        if (elapsedMs == null) {
            return 0.0;
        }
        return elapsedMs / 1000.0;
    }

    /**
     * 获取用时的分钟表示
     */
    public String getElapsedTimeFormatted() {
        if (elapsedMs == null) {
            return "0:00";
        }
        int totalSeconds = elapsedMs / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * 检查是否有ASR文本
     */
    public boolean hasAsrText() {
        return asrText != null && !asrText.trim().isEmpty();
    }

    /**
     * 获取ASR文本字数
     */
    public int getAsrTextWordCount() {
        if (!hasAsrText()) {
            return 0;
        }
        return asrText.trim().split("\\s+").length;
    }

    /**
     * 检查是否有得分
     */
    public boolean hasScore() {
        return score != null;
    }

    /**
     * 检查是否有评分详情
     */
    public boolean hasScoreDetail() {
        return scoreDetailJson != null && !scoreDetailJson.trim().isEmpty();
    }

    /**
     * 检查是否有回答音频
     */
    public boolean hasAnswerAudio() {
        return answerAudioId != null;
    }

    /**
     * 获取得分百分比表示
     */
    public String getScorePercentage() {
        if (score == null) {
            return "未评分";
        }
        return score.toString() + "%";
    }

    /**
     * 判断是否及格（假设60分及格）
     */
    public boolean isPassed() {
        if (score == null) {
            return false;
        }
        return score.compareTo(new BigDecimal("60")) >= 0;
    }

    /**
     * 获取得分等级
     */
    public String getScoreGrade() {
        if (score == null) {
            return "未评分";
        }
        
        if (score.compareTo(new BigDecimal("90")) >= 0) {
            return "优秀";
        } else if (score.compareTo(new BigDecimal("80")) >= 0) {
            return "良好";
        } else if (score.compareTo(new BigDecimal("70")) >= 0) {
            return "中等";
        } else if (score.compareTo(new BigDecimal("60")) >= 0) {
            return "及格";
        } else {
            return "不及格";
        }
    }

    // ====== JPA生命周期回调 ======

    /**
     * 保存前设置默认值
     */
    @PrePersist
    protected void onCreate() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
        if (elapsedMs == null) {
            elapsedMs = 0;
        }
    }
}