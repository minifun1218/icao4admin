package org.icao4.eqasbackend2.entity.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exam_record")
public class ExamRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_paper_id", nullable = false)
    @NotNull(message = "考试id不能为空")
    private Long examPaperId;

    @Column(name = "user_id")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "is_finished")
    private Boolean isFinished;


}
