package org.icao4.eqasbackend2.entity.lsa_dialogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.icao4.eqasbackend2.entity.MediaAsset;
import org.icao4.eqasbackend2.entity.exam.ExamModule;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 听力理解对话实体类
 * 对应数据库表：lsa_dialogs
 */
@Entity
@Table(name = "lsa_dialogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LsaDialog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联考试模块ID
     */
    @Column(name = "module_id", nullable = false)
    @NotNull(message = "考试模块ID不能为空")
    private Long moduleId;

    /**
     * 对话标题
     */
    @Column(name = "title", nullable = false, length = 255)
    @NotBlank(message = "对话标题不能为空")
    @Size(max = 255, message = "对话标题长度不能超过255个字符")
    private String title;

    /**
     * 对话描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 对话音频资源ID
     */
    @Column(name = "audio_id")
    private Long audioId;

    /**
     * 对话文本内容
     */
    @Column(name = "dialog_text", columnDefinition = "TEXT")
    private String dialogText;

    /**
     * 音频时长（秒）
     */
    @Column(name = "audio_duration_seconds")
    @PositiveOrZero(message = "音频时长不能为负数")
    private Integer audioDurationSeconds;

    /**
     * 答题时长限制（秒）
     */
    @Column(name = "time_limit_seconds")
    @PositiveOrZero(message = "答题时长限制不能为负数")
    private Integer timeLimitSeconds;

    /**
     * 显示顺序
     */
    @Column(name = "display_order", nullable = false)
    @NotNull(message = "显示顺序不能为空")
    @PositiveOrZero(message = "显示顺序不能为负数")
    private Integer displayOrder;

    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 标签（JSON格式）
     */
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    /**
     * 元数据（JSON格式）
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 关联的考试模块
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"lsaDialogs"})
    private ExamModule examModule;

    /**
     * 关联的音频资源
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"lsaDialogs"})
    private MediaAsset audioAsset;

    /**
     * 关联的听力理解问题列表
     */
    @OneToMany(mappedBy = "dialog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"dialog"})
    private List<LsaQuestion> lsaQuestions;

    /**
     * 获取音频时长的分钟表示
     */
    public String getAudioDurationFormatted() {
        if (audioDurationSeconds == null) {
            return "未知";
        }
        int minutes = audioDurationSeconds / 60;
        int seconds = audioDurationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * 获取答题时长限制的分钟表示
     */
    public String getTimeLimitFormatted() {
        if (timeLimitSeconds == null) {
            return "无限制";
        }
        int minutes = timeLimitSeconds / 60;
        int seconds = timeLimitSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * 检查是否有音频
     */
    public boolean hasAudio() {
        return audioId != null;
    }

    /**
     * 检查是否有对话文本
     */
    public boolean hasDialogText() {
        return dialogText != null && !dialogText.trim().isEmpty();
    }

    /**
     * 检查是否有描述
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * 获取对话文本字数
     */
    public int getDialogTextWordCount() {
        if (!hasDialogText()) {
            return 0;
        }
        return dialogText.trim().split("\\s+").length;
    }

    /**
     * 检查是否有时间限制
     */
    public boolean hasTimeLimit() {
        return timeLimitSeconds != null && timeLimitSeconds > 0;
    }

    /**
     * 检查是否有标签
     */
    public boolean hasTags() {
        return tags != null && !tags.trim().isEmpty();
    }

    /**
     * 检查是否有元数据
     */
    public boolean hasMetadata() {
        return metadata != null && !metadata.trim().isEmpty();
    }

    /**
     * 获取问题数量
     */
    public int getQuestionCount() {
        if (lsaQuestions == null) {
            return 0;
        }
        return lsaQuestions.size();
    }

    /**
     * 检查是否有问题
     */
    public boolean hasQuestions() {
        return getQuestionCount() > 0;
    }

    /**
     * 获取激活状态的文本表示
     */
    public String getActiveStatusText() {
        return isActive ? "激活" : "未激活";
    }

    /**
     * 获取对话类型（根据音频时长判断）
     */
    public String getDialogType() {
        if (audioDurationSeconds == null) {
            return "未知";
        }
        
        if (audioDurationSeconds <= 60) {
            return "短对话";
        } else if (audioDurationSeconds <= 300) {
            return "中等对话";
        } else {
            return "长对话";
        }
    }

    /**
     * 获取难度等级（根据对话文本长度和音频时长判断）
     */
    public String getDifficultyLevel() {
        int wordCount = getDialogTextWordCount();
        int duration = audioDurationSeconds != null ? audioDurationSeconds : 0;
        
        // 简单的难度判断逻辑
        if (wordCount <= 50 && duration <= 60) {
            return "简单";
        } else if (wordCount <= 150 && duration <= 180) {
            return "中等";
        } else {
            return "困难";
        }
    }

    /**
     * 检查对话是否完整（有标题、音频或文本）
     */
    public boolean isComplete() {
        return title != null && !title.trim().isEmpty() && 
               (hasAudio() || hasDialogText());
    }

    /**
     * 获取对话摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("标题: ").append(title);
        
        if (hasAudio()) {
            summary.append(", 音频时长: ").append(getAudioDurationFormatted());
        }
        
        if (hasDialogText()) {
            summary.append(", 文本字数: ").append(getDialogTextWordCount());
        }
        
        summary.append(", 问题数: ").append(getQuestionCount());
        summary.append(", 状态: ").append(getActiveStatusText());
        
        return summary.toString();
    }
}