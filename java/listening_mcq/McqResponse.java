package org.icao4.eqasbackend2.entity.listening_mcq;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mcq_responses")
public class McqResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", insertable = false, updatable = false)
    @NotNull(message = "question_id不能为空")
    private Long questionId;

    @Column(name = "selected_choice_id", insertable = false, updatable = false)
    @NotNull(message = "selected_choice_id不能为空")
    private Long selectedChoiceId;

    // 问题本体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private McqQuestion question;

    // 被选中的选项，可以为 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_choice_id")
    private McqChoice selectedChoice;


    // 是否答对（可为 null，待判分）
    @Column(name = "is_correct")
    private Boolean isCorrect;

    // 回答时间
    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    // 耗时（毫秒）
    @Column(name = "elapsed_ms", nullable = false)
    private Integer elapsedMs;

    // 创建时间
    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
