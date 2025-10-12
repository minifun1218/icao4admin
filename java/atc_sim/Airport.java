package org.icao4.eqasbackend2.entity.atc_sim;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 机场实体类，用于存储机场的基本信息。
 * 对应数据库表：airports
 */
@Entity
@Table(name = "airports")
@Data
@EqualsAndHashCode(callSuper = false)
public class Airport {

    /**
     * 机场ID，主键，采用数据库自增策略。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 国际民航组织（ICAO）四字代码。
     * 唯一标识机场，例如："ZUUU"（成都双流国际机场）。
     */
    @Column(nullable = false, unique = true, length = 8)
    private String icao;

    /**
     * 国际航空运输协会（IATA）三字代码。
     * 通常用于票务，例如："CTU"（成都双流国际机场）。
     */
    @Column(length = 8)
    private String iata;

    /**
     * 机场名称。
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 机场所在城市。
     */
    @Column(length = 100)
    private String city;

    /**
     * 机场所在国家。
     */
    @Column(length = 100)
    private String country;

    /**
     * 实体创建时间。
     * 由数据库自动设置，不可更新。
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}