package org.icao4.eqasbackend2.entity.listening_mcq;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 听力理解选择题选项实体类
 * 对应数据库表：mcq_choices
 */
@Entity
@Table(name = "mcq_choices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class McqChoice {
    
    /**
     * 选项ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 题目ID
     */
    @Column(name = "question_id", nullable = false)
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    
    /**
     * 选项标签 A/B/C/D
     */
    @Column(name = "label", nullable = false, length = 1)
    @NotBlank(message = "选项标签不能为空")
    @Pattern(regexp = "[ABCD]", message = "选项标签必须是A、B、C、D中的一个")
    private String label;
    
    /**
     * 选项内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "选项内容不能为空")
    @Size(max = 1000, message = "选项内容不能超过1000个字符")
    private String content;
    
    /**
     * 是否为正确答案
     * 1-正确答案，0-错误答案
     */
    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;
    
    /**
     * 关联的题目实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private McqQuestion question;
    
    /**
     * 获取选项的显示文本
     * @return 格式化的选项文本
     */
    public String getDisplayText() {
        return label + ". " + content;
    }
    
    /**
     * 检查是否为正确答案
     * @return 是否为正确答案
     */
    public boolean isCorrectAnswer() {
        return Boolean.TRUE.equals(isCorrect);
    }
    
    /**
     * 设置为正确答案
     */
    public void setAsCorrect() {
        this.isCorrect = true;
    }
    
    /**
     * 设置为错误答案
     */
    public void setAsIncorrect() {
        this.isCorrect = false;
    }
    
    @Override
    public String toString() {
        return "McqChoice{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", label='" + label + '\'' +
                ", content='" + content + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}