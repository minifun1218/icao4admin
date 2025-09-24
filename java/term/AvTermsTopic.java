package org.icao4.eqasbackend2.entity.term;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 航空术语主题分类实体类
 * 对应数据库表：av_terms_topics
 * 支持层级结构的主题分类管理
 */
@Entity
@Table(name = "av_terms_topics", indexes = {
    @Index(name = "idx_code", columnList = "code"),
    @Index(name = "idx_parent_id", columnList = "parent_id"),
    @Index(name = "idx_display_order", columnList = "display_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvTermsTopic {
    
    /**
     * 主题ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 父级主题ID
     * 支持层级结构，根节点为null
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 主题代码
     * 如："ATC_CLEARANCE", "METAR"
     */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    @NotBlank(message = "主题代码不能为空")
    @Size(max = 100, message = "主题代码不能超过100个字符")
    private String code;
    
    /**
     * 中文名称
     * 如："放行", "气象报文"
     */
    @Column(name = "name_zh", nullable = false, length = 200)
    @NotBlank(message = "中文名称不能为空")
    @Size(max = 200, message = "中文名称不能超过200个字符")
    private String nameZh;
    
    /**
     * 英文名称
     * 如："Clearance", "METAR"
     */
    @Column(name = "name_en", length = 200)
    @Size(max = 200, message = "英文名称不能超过200个字符")
    private String nameEn;
    
    /**
     * 主题描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 5000, message = "描述不能超过5000个字符")
    private String description;
    
    /**
     * 显示顺序
     */
    @Column(name = "display_order", nullable = false)
    @NotNull(message = "显示顺序不能为空")
    @Min(value = 0, message = "显示顺序不能小于0")
    private Integer displayOrder = 0;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 父级主题（延迟加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private AvTermsTopic parent;
    
    /**
     * 子级主题列表（延迟加载）
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("displayOrder ASC, id ASC")
    private List<AvTermsTopic> children;
    
    // ==================== 业务方法 ====================
    
    /**
     * 是否有父级主题
     */
    public boolean hasParent() {
        return parentId != null;
    }
    
    /**
     * 是否为根节点
     */
    public boolean isRoot() {
        return parentId == null;
    }
    
    /**
     * 是否有英文名称
     */
    public boolean hasNameEn() {
        return nameEn != null && !nameEn.trim().isEmpty();
    }
    
    /**
     * 是否有描述
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }
    
    /**
     * 是否有子级主题
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
    
    /**
     * 获取显示名称（优先中文）
     */
    public String getDisplayName() {
        return nameZh;
    }
    
    /**
     * 获取完整显示名称（中英文）
     */
    public String getFullDisplayName() {
        if (hasNameEn()) {
            return nameZh + " (" + nameEn + ")";
        }
        return nameZh;
    }
    
    /**
     * 检查代码是否匹配
     */
    public boolean isCode(String code) {
        return this.code != null && this.code.equals(code);
    }
    
    /**
     * 检查代码是否匹配（忽略大小写）
     */
    public boolean isCodeIgnoreCase(String code) {
        return this.code != null && this.code.equalsIgnoreCase(code);
    }
    
    /**
     * 检查是否为指定父级的子主题
     */
    public boolean isChildOf(Long parentId) {
        return this.parentId != null && this.parentId.equals(parentId);
    }
    
    /**
     * 检查是否为指定主题的子主题
     */
    public boolean isChildOf(AvTermsTopic parent) {
        return parent != null && isChildOf(parent.getId());
    }
    
    /**
     * 获取层级深度（根节点为0）
     */
    public int getDepth() {
        if (isRoot()) {
            return 0;
        }
        // 这里简化处理，实际应用中可能需要递归计算
        return 1;
    }
    
    /**
     * 验证主题数据是否有效
     */
    public boolean isValid() {
        return code != null && !code.trim().isEmpty() && 
               nameZh != null && !nameZh.trim().isEmpty() &&
               displayOrder != null && displayOrder >= 0;
    }
    
    /**
     * 清空描述
     */
    public void clearDescription() {
        this.description = null;
    }
    
    /**
     * 清空英文名称
     */
    public void clearNameEn() {
        this.nameEn = null;
    }
    
    @Override
    public String toString() {
        return "AvTermsTopic{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", code='" + code + '\'' +
                ", nameZh='" + nameZh + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", displayOrder=" + displayOrder +
                '}';
    }
}