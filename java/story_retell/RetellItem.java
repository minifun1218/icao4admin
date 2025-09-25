package org.icao4.eqasbackend2.entity.story_retell;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.icao4.eqasbackend2.entity.MediaAsset;
import org.icao4.eqasbackend2.entity.exam.ExamModule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 复述题目实体类
 * 对应数据库表：retell_items
 */
@Entity
@Table(name = "retell_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RetellItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联模块ID
     */
    @Column(name = "module_id")
    @NotNull(message = "模块ID不能为空")
    private Long moduleId;

    /**
     * 题目标题
     */
    @Column(name = "title", nullable = false, length = 200)
    @NotBlank(message = "题目标题不能为空")
    @Size(max = 200, message = "题目标题长度不能超过200个字符")
    private String title;

    /**
     * 音频资源ID
     */
    @Column(name = "audio_asset_id", nullable = false)
    @NotNull(message = "音频资源ID不能为空")
    private Long audioAssetId;

    /**
     * 音频时长（秒）
     */
    @Column(name = "audio_duration_sec", nullable = false)
    @NotNull(message = "音频时长不能为空")
    @Positive(message = "音频时长必须为正数")
    private Integer audioDurationSec;

    /**
     * 答题时长（秒）
     */
    @Column(name = "answer_seconds", nullable = false)
    @NotNull(message = "答题时长不能为空")
    @Positive(message = "答题时长必须为正数")
    @Builder.Default
    private Integer answerSeconds = 90;

    /**
     * 参考文本（用于评分）
     */
    @Column(name = "reference_text", columnDefinition = "TEXT")
    private String referenceText;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 关联的模块
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"retellItems"})
    private ExamModule examModule;

    /**
     * 关联的音频资源
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_asset_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"retellItems"})
    private MediaAsset audioAsset;
    

    /**
     * 获取音频时长的分钟表示
     */
    public String getAudioDurationMinutes() {
        if (audioDurationSec == null) {
            return "0:00";
        }
        int minutes = audioDurationSec / 60;
        int seconds = audioDurationSec % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * 获取答题时长的分钟表示
     */
    public String getAnswerTimeMinutes() {
        if (answerSeconds == null) {
            return "0:00";
        }
        int minutes = answerSeconds / 60;
        int seconds = answerSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * 检查是否有参考文本
     */
    public boolean hasReferenceText() {
        return referenceText != null && !referenceText.trim().isEmpty();
    }

    /**
     * 获取参考文本字数
     */
    public int getReferenceTextWordCount() {
        if (!hasReferenceText()) {
            return 0;
        }
        return referenceText.trim().split("\\s+").length;
    }
}