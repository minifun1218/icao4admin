package org.icao4.eqasbackend2.entity.opi;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OPI问题实体类
 * 对应数据库表：opi_questions
 */
@Entity
@Table(name = "opi_questions", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_topic_q", columnNames = {"topic_id", "q_order"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"topic"})
public class OpiQuestion {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 话题ID
     */
    @NotNull(message = "话题ID不能为空")
    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    /**
     * 问题顺序
     */
    @NotNull(message = "问题顺序不能为空")
    @Min(value = 1, message = "问题顺序必须大于0")
    @Column(name = "q_order", nullable = false)
    private Integer QOrder;

    /**
     * 预录提问音频ID
     */
    @NotNull(message = "预录提问音频ID不能为空")
    @Column(name = "prompt_audio_id", nullable = false)
    private Long promptAudioId;

    /**
     * 回答时间（秒）
     */
    @NotNull(message = "回答时间不能为空")
    @Min(value = 1, message = "回答时间必须大于0秒")
    @Max(value = 300, message = "回答时间不能超过300秒")
    @Column(name = "answer_seconds", nullable = false)
    @Builder.Default
    private Integer answerSeconds = 60;

    /**
     * 屏显文本（可选）
     */
    @Column(name = "prompt_text", columnDefinition = "TEXT")
    private String promptText;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==================== 关联关系 ====================

    /**
     * 关联的话题
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_opi_question_topic"))
    private OpiTopic topic;

    // ==================== JPA生命周期回调 ====================

    /**
     * 保存前设置默认值
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (answerSeconds == null) {
            answerSeconds = 60;
        }
    }

    // ==================== 业务方法 ====================

    /**
     * 检查问题是否完整
     */
    public boolean isComplete() {
        return topicId != null && 
               QOrder != null && 
               promptAudioId != null && 
               answerSeconds != null && answerSeconds > 0;
    }

    /**
     * 检查是否有屏显文本
     */
    public boolean hasPromptText() {
        return promptText != null && !promptText.trim().isEmpty();
    }

    /**
     * 检查回答时间是否合理
     */
    public boolean isAnswerTimeValid() {
        return answerSeconds != null && answerSeconds >= 10 && answerSeconds <= 300;
    }

    /**
     * 获取问题标识
     */
    public String getQuestionIdentifier() {
        return String.format("Topic-%d-Q%d", topicId, QOrder);  
    }

    /**
     * 检查是否为第一个问题
     */
    public boolean isFirstQuestion() {
        return QOrder != null && QOrder == 1;
    }

    /**
     * 检查是否为短时间回答问题（小于30秒）
     */
    public boolean isShortAnswerQuestion() {
        return answerSeconds != null && answerSeconds < 30;
    }

    /**
     * 检查是否为长时间回答问题（大于等于120秒）
     */
    public boolean isLongAnswerQuestion() {
        return answerSeconds != null && answerSeconds >= 120;
    }

    /**
     * 获取回答时间描述
     */
    public String getAnswerTimeDescription() {
        if (answerSeconds == null) {
            return "未设置";
        }
        if (answerSeconds < 30) {
            return "短时间回答";
        } else if (answerSeconds >= 120) {
            return "长时间回答";
        } else {
            return "标准时间回答";
        }
    }

    /**
     * 复制问题基本信息
     */
    public OpiQuestion copyBasicInfo() {
        return OpiQuestion.builder()
                .topicId(this.topicId)
                .QOrder(this.QOrder)
                .promptAudioId(this.promptAudioId)
                .answerSeconds(this.answerSeconds)
                .promptText(this.promptText)
                .build();
    }

    /**
     * 更新问题顺序
     */
    public void updateOrder(Integer newOrder) {
        if (newOrder != null && newOrder > 0) {
            this.QOrder = newOrder;
        }
    }

    /**
     * 更新回答时间
     */
    public void updateAnswerTime(Integer newAnswerSeconds) {
        if (newAnswerSeconds != null && newAnswerSeconds >= 10 && newAnswerSeconds <= 300) {
            this.answerSeconds = newAnswerSeconds;
        }
    }

    /**
     * 更新屏显文本
     */
    public void updatePromptText(String newPromptText) {
        this.promptText = newPromptText;
    }

    /**
     * 清除屏显文本
     */
    public void clearPromptText() {
        this.promptText = null;
    }

    /**
     * 验证问题数据
     */
    public boolean validate() {
        return isComplete() && isAnswerTimeValid();
    }

    /**
     * 获取问题摘要信息
     */
    public String getSummary() {
        return String.format("问题%d (回答时间: %d秒, 音频ID: %d)", 
                           QOrder, answerSeconds, promptAudioId);
    }
}