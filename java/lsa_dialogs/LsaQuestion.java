package org.icao4.eqasbackend2.entity.lsa_dialogs;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 听力理解问题实体类
 */
@Data
@Entity
@Table(name = "lsa_questions", indexes = {
    @Index(name = "idx_lsa_questions_dialog_id", columnList = "dialog_id"),
    @Index(name = "idx_lsa_questions_question_type", columnList = "question_type"),
    @Index(name = "idx_lsa_questions_display_order", columnList = "display_order"),
    @Index(name = "idx_lsa_questions_is_active", columnList = "is_active"),
    @Index(name = "idx_lsa_questions_created_at", columnList = "created_at"),
    @Index(name = "idx_lsa_questions_dialog_order", columnList = "dialog_id, display_order")
})
public class LsaQuestion {

    /**
     * 问题ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 对话ID
     */
    @NotNull(message = "Dialog ID cannot be null")
    @Column(name = "dialog_id", nullable = false)
    private Long dialogId;

    /**
     * 问题类型
     */
    @NotBlank(message = "Question type cannot be blank")
    @Size(max = 50, message = "Question type cannot exceed 50 characters")
    @Column(name = "question_type", nullable = false, length = 50)
    private String questionType;

    /**
     * 问题文本
     */
    @NotBlank(message = "Question text cannot be blank")
    @Size(max = 2000, message = "Question text cannot exceed 2000 characters")
    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    /**
     * 选项A
     */
    @Size(max = 500, message = "Option A cannot exceed 500 characters")
    @Column(name = "option_a", length = 500)
    private String optionA;

    /**
     * 选项B
     */
    @Size(max = 500, message = "Option B cannot exceed 500 characters")
    @Column(name = "option_b", length = 500)
    private String optionB;

    /**
     * 选项C
     */
    @Size(max = 500, message = "Option C cannot exceed 500 characters")
    @Column(name = "option_c", length = 500)
    private String optionC;

    /**
     * 选项D
     */
    @Size(max = 500, message = "Option D cannot exceed 500 characters")
    @Column(name = "option_d", length = 500)
    private String optionD;

    /**
     * 正确答案
     */
    @Size(max = 10, message = "Correct answer cannot exceed 10 characters")
    @Column(name = "correct_answer", length = 10)
    private String correctAnswer;

    /**
     * 答案解析
     */
    @Size(max = 1000, message = "Answer explanation cannot exceed 1000 characters")
    @Column(name = "answer_explanation", length = 1000)
    private String answerExplanation;

    /**
     * 分值
     */
    @DecimalMin(value = "0.0", message = "Points must be non-negative")
    @DecimalMax(value = "100.0", message = "Points cannot exceed 100")
    @Column(name = "points", precision = 5)
    private Double points;

    /**
     * 显示顺序
     */
    @Min(value = 1, message = "Display order must be positive")
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * 难度等级
     */
    @Min(value = 1, message = "Difficulty level must be at least 1")
    @Max(value = 5, message = "Difficulty level cannot exceed 5")
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    /**
     * 标签
     */
    @Size(max = 500, message = "Tags cannot exceed 500 characters")
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * 是否激活
     */
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
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 关联的对话
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialog_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LsaDialog dialog;

    /**
     * 关联的回答列表
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<LsaResponse> responses;

    /**
     * JPA生命周期回调 - 持久化前
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        
        if (this.isActive == null) {
            this.isActive = true;
        }
        
        if (this.points == null) {
            this.points = 1.0;
        }
        
        if (this.difficultyLevel == null) {
            this.difficultyLevel = 3;
        }
    }

    /**
     * JPA生命周期回调 - 更新前
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 辅助方法

    /**
     * 检查是否为选择题
     */
    public boolean isMultipleChoice() {
        return "MULTIPLE_CHOICE".equalsIgnoreCase(this.questionType) ||
               "MC".equalsIgnoreCase(this.questionType);
    }

    /**
     * 检查是否为填空题
     */
    public boolean isFillInBlank() {
        return "FILL_IN_BLANK".equalsIgnoreCase(this.questionType) ||
               "FIB".equalsIgnoreCase(this.questionType);
    }

    /**
     * 检查是否为简答题
     */
    public boolean isShortAnswer() {
        return "SHORT_ANSWER".equalsIgnoreCase(this.questionType) ||
               "SA".equalsIgnoreCase(this.questionType);
    }

    /**
     * 检查是否为判断题
     */
    public boolean isTrueFalse() {
        return "TRUE_FALSE".equalsIgnoreCase(this.questionType) ||
               "TF".equalsIgnoreCase(this.questionType);
    }

    /**
     * 获取选项数量
     */
    public int getOptionCount() {
        int count = 0;
        if (StringUtils.hasText(this.optionA)) count++;
        if (StringUtils.hasText(this.optionB)) count++;
        if (StringUtils.hasText(this.optionC)) count++;
        if (StringUtils.hasText(this.optionD)) count++;
        return count;
    }

    /**
     * 检查是否有选项
     */
    public boolean hasOptions() {
        return getOptionCount() > 0;
    }

    /**
     * 检查是否有正确答案
     */
    public boolean hasCorrectAnswer() {
        return StringUtils.hasText(this.correctAnswer);
    }

    /**
     * 检查是否有答案解析
     */
    public boolean hasAnswerExplanation() {
        return StringUtils.hasText(this.answerExplanation);
    }

    /**
     * 检查是否有标签
     */
    public boolean hasTags() {
        return StringUtils.hasText(this.tags);
    }

    /**
     * 获取标签列表
     */
    public String[] getTagArray() {
        if (!hasTags()) {
            return new String[0];
        }
        return this.tags.split(",");
    }

    /**
     * 设置标签列表
     */
    public void setTagArray(String[] tags) {
        if (tags == null || tags.length == 0) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tags);
        }
    }

    /**
     * 检查答案是否正确
     */
    public boolean isAnswerCorrect(String answer) {
        if (!hasCorrectAnswer() || !StringUtils.hasText(answer)) {
            return false;
        }
        return this.correctAnswer.trim().equalsIgnoreCase(answer.trim());
    }

    /**
     * 获取难度等级描述
     */
    public String getDifficultyDescription() {
        if (this.difficultyLevel == null) {
            return "未设置";
        }
        switch (this.difficultyLevel) {
            case 1: return "非常简单";
            case 2: return "简单";
            case 3: return "中等";
            case 4: return "困难";
            case 5: return "非常困难";
            default: return "未知";
        }
    }

    /**
     * 检查是否为简单题目
     */
    public boolean isEasy() {
        return this.difficultyLevel != null && this.difficultyLevel <= 2;
    }

    /**
     * 检查是否为中等题目
     */
    public boolean isMedium() {
        return this.difficultyLevel != null && this.difficultyLevel == 3;
    }

    /**
     * 检查是否为困难题目
     */
    public boolean isHard() {
        return this.difficultyLevel != null && this.difficultyLevel >= 4;
    }

    /**
     * 获取问题文本的字数
     */
    public int getQuestionTextWordCount() {
        if (!StringUtils.hasText(this.questionText)) {
            return 0;
        }
        return this.questionText.trim().split("\\s+").length;
    }

    /**
     * 获取问题文本的字符数
     */
    public int getQuestionTextCharCount() {
        if (!StringUtils.hasText(this.questionText)) {
            return 0;
        }
        return this.questionText.length();
    }

    /**
     * 检查问题是否完整
     */
    public boolean isComplete() {
        // 基本信息完整性检查
        if (!StringUtils.hasText(this.questionText) || 
            !StringUtils.hasText(this.questionType)) {
            return false;
        }
        
        // 选择题需要有选项和正确答案
        if (isMultipleChoice()) {
            return hasOptions() && hasCorrectAnswer();
        }
        
        // 判断题需要有正确答案
        if (isTrueFalse()) {
            return hasCorrectAnswer();
        }
        
        // 其他类型的题目至少需要有问题文本
        return true;
    }

    /**
     * 验证选择题的正确答案是否有效
     */
    public boolean isValidMultipleChoiceAnswer() {
        if (!isMultipleChoice() || !hasCorrectAnswer()) {
            return false;
        }
        
        String answer = this.correctAnswer.trim().toUpperCase();
        
        // 检查答案是否在有效选项范围内
        switch (answer) {
            case "A":
                return StringUtils.hasText(this.optionA);
            case "B":
                return StringUtils.hasText(this.optionB);
            case "C":
                return StringUtils.hasText(this.optionC);
            case "D":
                return StringUtils.hasText(this.optionD);
            default:
                return false;
        }
    }

    /**
     * 验证判断题的正确答案是否有效
     */
    public boolean isValidTrueFalseAnswer() {
        if (!isTrueFalse() || !hasCorrectAnswer()) {
            return false;
        }
        
        String answer = this.correctAnswer.trim().toUpperCase();
        return "TRUE".equals(answer) || "FALSE".equals(answer) || 
               "T".equals(answer) || "F".equals(answer) ||
               "是".equals(answer) || "否".equals(answer);
    }

    /**
     * 获取格式化的分值
     */
    public String getFormattedPoints() {
        if (this.points == null) {
            return "0分";
        }
        double pointsValue = this.points;
        if (pointsValue == Math.round(pointsValue)) {
            return String.format("%.0f分", pointsValue);
        } else {
            return String.format("%.1f分", pointsValue);
        }
    }

    /**
     * 检查是否为高分值题目
     */
    public boolean isHighValue() {
        return this.points != null && this.points >= 5.0;
    }



    /**
     * 检查是否为低分值题目
     */
    public boolean isLowValue() {
        return this.points != null && this.points <= 1.0;
    }

    /**
     * 获取问题类型的中文描述
     */
    public String getQuestionTypeDescription() {
        if (!StringUtils.hasText(this.questionType)) {
            return "未知类型";
        }
        
        switch (this.questionType.toUpperCase()) {
            case "MULTIPLE_CHOICE":
            case "MC":
                return "选择题";
            case "FILL_IN_BLANK":
            case "FIB":
                return "填空题";
            case "SHORT_ANSWER":
            case "SA":
                return "简答题";
            case "TRUE_FALSE":
            case "TF":
                return "判断题";
            case "ESSAY":
                return "论述题";
            case "LISTENING":
                return "听力题";
            default:
                return this.questionType;
        }
    }

    /**
     * 生成问题摘要
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(getQuestionTypeDescription());
        
        if (this.points != null) {
            summary.append(" (").append(getFormattedPoints()).append(")");
        }
        
        if (StringUtils.hasText(this.questionText)) {
            String text = this.questionText.length() > 50 ? 
                         this.questionText.substring(0, 50) + "..." : 
                         this.questionText;
            summary.append(": ").append(text);
        }
        
        return summary.toString();
    }
}