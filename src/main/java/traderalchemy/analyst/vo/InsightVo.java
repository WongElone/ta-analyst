package traderalchemy.analyst.vo;

import java.time.ZonedDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Immutable;

import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import traderalchemy.analyst.Const;

@Data
@Entity
@Immutable
@Table(name = "v_insight")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
non-sealed public class InsightVo implements InductionOrInsightVo {
    @Id
    @EqualsAndHashCode.Include
    private Integer id;
    
    @Column(name = "strategy_class_name", nullable = false)
    private String strategyClassName;
    
    @Column(name = "topic", nullable = false)
    private String topic;
    
    @Column(name = "category", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Const.InsightCategory category;
    
    @Column(name = "source_id", nullable = false)
    private Integer sourceId;
    
    @Column(name = "instruction_id", nullable = false)
    private Integer instructionId;
    
    @Column(name = "conclusion", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private ObjectNode conclusion;
    
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "source_time", nullable = false)
    private ZonedDateTime sourceTime;

    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private ZonedDateTime createTime;

    @Column(name = "show", nullable = false)
    @ColumnDefault("true")
    private boolean show;
    
    @Column(name = "deleted", nullable = false)
    @ColumnDefault("false")
    private boolean deleted;

    /* the following fields are from t_analysis_instruction */

    @Column(name = "predicate", nullable = false, columnDefinition = "TEXT")
    private String predicate;
    
    @Column(name = "analysis", nullable = false, columnDefinition = "TEXT")
    private String analysis;
    
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;
    
    @Column(name = "instruction_md5", nullable = false)
    private String instructionMd5;

    public ZonedDateTime getFromTime() {
        return null;
    }

    public ZonedDateTime getToTime() {
        return null;
    }

    public ZonedDateTime getSourceTimeLowerBound() {
        return sourceTime;
    }

    public ZonedDateTime getSourceTimeUpperBound() {
        return sourceTime;
    }
}
