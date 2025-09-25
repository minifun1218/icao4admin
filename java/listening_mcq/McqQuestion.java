package org.icao4.eqasbackend2.entity.listening_mcq;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.icao4.eqasbackend2.entity.MediaAsset;
import org.icao4.eqasbackend2.entity.exam.ExamModule;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 听力理解选择题实体类
 * 对应数据库表：mcq_questions
 */
@Entity
@Table(name = "mcq_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class McqQuestion {

    /**
     * 题目ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模块ID - 指向LISTENING_MCQ模块
     */
    @Column(name = "module_id")
    private Long moduleId;

    /**
     * 音频ID - 对话音频（每分钟100-120词）
     */
    @Column(name = "audio_id", nullable = false)
    @NotNull(message = "音频ID不能为空")
    private Long audioId;
    
    /**
     * 题干文本
     */
    @Column(name = "text_stem", columnDefinition = "TEXT")
    private String textStem;

    /**
     * 是否仅播放一遍
     * 1-仅播放一遍，0-可重复播放
     */
    @Column(name = "play_once", nullable = false)
    @Builder.Default
    private Boolean playOnce = true;

    /**
     * 每题答题留时（秒）
     */
    @Column(name = "answer_seconds", nullable = false)
    @NotNull(message = "答题时间不能为空")
    @Min(value = 1, message = "答题时间必须大于0秒")
    @Max(value = 300, message = "答题时间不能超过300秒")
    @Builder.Default
    private Integer answerSeconds = 15;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ==================== 关联关系 ====================

    /**
     * 关联的考试模块
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", insertable = false, updatable = false)
    private ExamModule examModule;

    /**
     * 关联的音频资源
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id", insertable = false, updatable = false)
    private MediaAsset audioAsset;



    // ==================== 业务方法 ====================

    /**
     * 设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (playOnce == null) {
            playOnce = true;
        }
        if (answerSeconds == null) {
            answerSeconds = 15;
        }
    }

    /**
     * 获取音频播放次数描述
     */
    public String getPlayModeDescription() {
        return playOnce ? "仅播放一遍" : "可重复播放";
    }

    /**
     * 检查是否有题干文本
     */
    public boolean hasTextStem() {
        return textStem != null && !textStem.trim().isEmpty();
    }

    /**
     * 获取答题时间描述
     */
    public String getAnswerTimeDescription() {
        return answerSeconds + "秒";
    }
}