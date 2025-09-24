package org.icao4.eqasbackend2.entity.term;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.icao4.eqasbackend2.entity.MediaAsset;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 航空术语/词汇实体类
 * 对应数据库表：av_terms
 * 支持通用语言学字段和多媒体资源关联
 */
@Entity
@Table(name = "av_terms", indexes = {
    @Index(name = "idx_headword", columnList = "headword"),
    @Index(name = "idx_cefr", columnList = "cefr_level"),
    @Index(name = "idx_freq", columnList = "freq_rank")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvTerm {
    
    /**
     * 术语ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 词条/术语
     * 如："line up and wait"
     */
    @Column(name = "headword", nullable = false, length = 200)
    @NotBlank(message = "词条不能为空")
    @Size(max = 200, message = "词条不能超过200个字符")
    private String headword;
    
    /**
     * 词形还原
     * 语言学概念，词汇的基本形式
     */
    @Column(name = "lemma", length = 200)
    @Size(max = 200, message = "词形还原不能超过200个字符")
    private String lemma;
    
    /**
     * 词性
     * 如：verb, noun, abbr, phrase
     */
    @Column(name = "pos", length = 50)
    @Size(max = 50, message = "词性不能超过50个字符")
    private String pos;
    
    /**
     * 国际音标
     * 发音标注
     */
    @Column(name = "ipa", length = 100)
    @Size(max = 100, message = "音标不能超过100个字符")
    private String ipa;
    
    /**
     * 英文释义
     */
    @Column(name = "definition_en", columnDefinition = "TEXT")
    @Size(max = 5000, message = "英文释义不能超过5000个字符")
    private String definitionEn;
    
    /**
     * 中文释义/术语说明
     */
    @Column(name = "definition_zh", columnDefinition = "TEXT")
    @Size(max = 5000, message = "中文释义不能超过5000个字符")
    private String definitionZh;
    
    /**
     * 英文例句
     */
    @Column(name = "example_en", columnDefinition = "TEXT")
    @Size(max = 2000, message = "英文例句不能超过2000个字符")
    private String exampleEn;
    
    /**
     * 例句中文翻译
     */
    @Column(name = "example_zh", columnDefinition = "TEXT")
    @Size(max = 2000, message = "中文例句不能超过2000个字符")
    private String exampleZh;
    
    /**
     * CEFR等级
     * 欧洲共同语言参考标准
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level")
    private CefrLevel cefrLevel;
    
    /**
     * 频次排名
     * 数值越小表示越常见/重要
     */
    @Column(name = "freq_rank")
    @Min(value = 1, message = "频次排名必须大于0")
    private Integer freqRank;
    
    /**
     * 来源
     * 如：DOC4444, 教材, 题库
     */
    @Column(name = "source", length = 200)
    @Size(max = 200, message = "来源不能超过200个字符")
    private String source;
    
    /**
     * 发音音频资源ID
     * 关联 media_assets 表
     */
    @Column(name = "audio_asset_id")
    private Long audioAssetId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) // LAZY 表示懒加载，只有在需要时才加载关联对象
    @JoinColumn(name = "audio_asset_id", insertable = false, updatable = false)
    private MediaAsset audioAsset;
    
    /**
     * 扩展JSON字段
     * 存储同义词、反义词等复合信息
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_json", columnDefinition = "JSON")
    private Map<String, Object> extraJson;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // ==================== 枚举定义 ====================
    
    /**
     * CEFR等级枚举
     */
    public enum CefrLevel {
        A1("初级入门"),
        A2("初级进阶"),
        B1("中级入门"),
        B2("中级进阶"),
        C1("高级入门"),
        C2("高级精通");
        
        private final String description;
        
        CefrLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * 检查是否为初级水平
         */
        public boolean isBasicLevel() {
            return this == A1 || this == A2;
        }
        
        /**
         * 检查是否为中级水平
         */
        public boolean isIntermediateLevel() {
            return this == B1 || this == B2;
        }
        
        /**
         * 检查是否为高级水平
         */
        public boolean isAdvancedLevel() {
            return this == C1 || this == C2;
        }
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 检查是否有词形还原
     */
    public boolean hasLemma() {
        return lemma != null && !lemma.trim().isEmpty();
    }
    
    /**
     * 检查是否有词性标注
     */
    public boolean hasPos() {
        return pos != null && !pos.trim().isEmpty();
    }
    
    /**
     * 检查是否有音标
     */
    public boolean hasIpa() {
        return ipa != null && !ipa.trim().isEmpty();
    }
    
    /**
     * 检查是否有英文释义
     */
    public boolean hasDefinitionEn() {
        return definitionEn != null && !definitionEn.trim().isEmpty();
    }
    
    /**
     * 检查是否有中文释义
     */
    public boolean hasDefinitionZh() {
        return definitionZh != null && !definitionZh.trim().isEmpty();
    }
    
    /**
     * 检查是否有英文例句
     */
    public boolean hasExampleEn() {
        return exampleEn != null && !exampleEn.trim().isEmpty();
    }
    
    /**
     * 检查是否有中文例句
     */
    public boolean hasExampleZh() {
        return exampleZh != null && !exampleZh.trim().isEmpty();
    }
    
    /**
     * 检查是否有CEFR等级
     */
    public boolean hasCefrLevel() {
        return cefrLevel != null;
    }
    
    /**
     * 检查是否有频次排名
     */
    public boolean hasFreqRank() {
        return freqRank != null && freqRank > 0;
    }
    
    /**
     * 检查是否有来源信息
     */
    public boolean hasSource() {
        return source != null && !source.trim().isEmpty();
    }
    
    /**
     * 检查是否有音频资源
     */
    public boolean hasAudioAsset() {
        return audioAssetId != null;
    }
    
    /**
     * 检查是否有扩展信息
     */
    public boolean hasExtraJson() {
        return extraJson != null && !extraJson.isEmpty();
    }
    
    /**
     * 检查词条是否匹配
     */
    public boolean isHeadword(String headword) {
        return this.headword != null && this.headword.equals(headword);
    }
    
    /**
     * 检查词条是否匹配（忽略大小写）
     */
    public boolean isHeadwordIgnoreCase(String headword) {
        return this.headword != null && this.headword.equalsIgnoreCase(headword);
    }
    
    /**
     * 检查是否为指定词性
     */
    public boolean isPos(String pos) {
        return this.pos != null && this.pos.equalsIgnoreCase(pos);
    }
    
    /**
     * 检查是否为指定CEFR等级
     */
    public boolean isCefrLevel(CefrLevel level) {
        return this.cefrLevel == level;
    }
    
    /**
     * 检查是否为基础词汇（A1-A2）
     */
    public boolean isBasicVocabulary() {
        return cefrLevel != null && cefrLevel.isBasicLevel();
    }
    
    /**
     * 检查是否为中级词汇（B1-B2）
     */
    public boolean isIntermediateVocabulary() {
        return cefrLevel != null && cefrLevel.isIntermediateLevel();
    }
    
    /**
     * 检查是否为高级词汇（C1-C2）
     */
    public boolean isAdvancedVocabulary() {
        return cefrLevel != null && cefrLevel.isAdvancedLevel();
    }
    
    /**
     * 检查是否为高频词汇（排名前1000）
     */
    public boolean isHighFrequencyTerm() {
        return freqRank != null && freqRank <= 1000;
    }
    
    /**
     * 检查是否为低频词汇（排名5000以后）
     */
    public boolean isLowFrequencyTerm() {
        return freqRank != null && freqRank > 5000;
    }
    
    /**
     * 检查是否来自指定来源
     */
    public boolean isFromSource(String source) {
        return this.source != null && this.source.equalsIgnoreCase(source);
    }
    
    /**
     * 获取显示用的词条（带音标）
     */
    public String getDisplayHeadword() {
        if (hasIpa()) {
            return headword + " [" + ipa + "]";
        }
        return headword;
    }
    
    /**
     * 获取完整释义（中英文）
     */
    public String getFullDefinition() {
        StringBuilder sb = new StringBuilder();
        if (hasDefinitionZh()) {
            sb.append(definitionZh);
        }
        if (hasDefinitionEn()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(definitionEn);
        }
        return sb.toString();
    }
    
    /**
     * 获取完整例句（中英文）
     */
    public String getFullExample() {
        StringBuilder sb = new StringBuilder();
        if (hasExampleEn()) {
            sb.append(exampleEn);
        }
        if (hasExampleZh()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(exampleZh);
        }
        return sb.toString();
    }
    
    /**
     * 验证术语数据完整性
     */
    public boolean isValid() {
        return headword != null && !headword.trim().isEmpty()
            && (hasDefinitionEn() || hasDefinitionZh());
    }
    
    /**
     * 检查是否为完整术语（有释义和例句）
     */
    public boolean isCompleteTerm() {
        return isValid() && (hasExampleEn() || hasExampleZh());
    }
    
    /**
     * 清空音频资源关联
     */
    public void clearAudioAsset() {
        this.audioAssetId = null;
    }
    
    /**
     * 清空扩展信息
     */
    public void clearExtraJson() {
        this.extraJson = null;
    }
    
    /**
     * 重置频次排名
     */
    public void resetFreqRank() {
        this.freqRank = null;
    }
    
    /**
     * 从扩展JSON中获取同义词
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getSynonyms() {
        if (extraJson != null && extraJson.containsKey("synonyms")) {
            Object synonyms = extraJson.get("synonyms");
            if (synonyms instanceof java.util.List) {
                return (java.util.List<String>) synonyms;
            }
        }
        return java.util.Collections.emptyList();
    }
    
    /**
     * 从扩展JSON中获取反义词
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getAntonyms() {
        if (extraJson != null && extraJson.containsKey("antonyms")) {
            Object antonyms = extraJson.get("antonyms");
            if (antonyms instanceof java.util.List) {
                return (java.util.List<String>) antonyms;
            }
        }
        return java.util.Collections.emptyList();
    }
    
    @Override
    public String toString() {
        return "AvTerm{" +
                "id=" + id +
                ", headword='" + headword + '\'' +
                ", pos='" + pos + '\'' +
                ", cefrLevel=" + cefrLevel +
                ", freqRank=" + freqRank +
                '}';
    }
}