package org.icao4.eqasbackend2.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 媒体资源实体类，用于映射数据库中的 'media_assets' 表。
 * 该实体类代表了系统中的各种媒体资产，例如音频、图像、视频和文档。
 */
@Entity
@Table(name = "media_assets")
@Data
@EqualsAndHashCode(callSuper = false)
public class MediaAsset {

    /**
     * 实体唯一标识符，主键，采用数据库自增策略。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 媒体类型，使用枚举类型存储，映射到数据库的 'media_type' 列。
     * EnumType.STRING 表示枚举的名称（如 "audio"）将被存储为字符串。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    /**
     * 媒体资源的统一资源标识符 (URI)。
     * 存储资源的路径或 URL，长度限制为500个字符。
     */
    @Column(nullable = false, length = 500)
    private String uri;

    /**
     * 媒体持续时长，单位为毫秒。
     * 对于音频和视频文件，该字段存储其播放时长。
     */
    @Column(name = "duration_ms")
    private Integer durationMs;

    /**
     * 文本转录内容。
     * 针对音频或视频的文字转录，使用 TEXT 类型以便存储较长的文本。
     */
    @Column(columnDefinition = "TEXT")
    private String transcript;

    /**
     * 额外的元数据，以 JSON 格式存储。
     * 使用 @JdbcTypeCode(SqlTypes.JSON) 注解指定数据库列类型为 JSON。
     * 适用于存储不固定、灵活的额外信息。
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_meta_json", columnDefinition = "JSON")
    private String extraMetaJson;

    /**
     * 记录创建时间。
     * @CreationTimestamp 注解会在实体首次持久化时自动设置当前时间。
     * updatable = false 确保该字段在更新时不会被修改。
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 媒体类型枚举。
     * 定义了系统支持的四种媒体类型：音频 (audio)、图像 (image)、视频 (video) 和文档 (doc)。
     */
    public enum MediaType {
        audio, image, video, doc
    }
}