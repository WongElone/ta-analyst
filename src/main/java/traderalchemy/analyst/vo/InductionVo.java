package traderalchemy.analyst.vo;

import java.time.ZonedDateTime;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Immutable;

import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import traderalchemy.analyst.util.TimeRangeUtils.TimeRange;

@Data
@Builder
@Entity
@Immutable
@Table(name = "v_induction")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
non-sealed public class InductionVo implements TimeRange, InductionOrInsightVo {
    @Id
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "strategy_class_name", nullable = false)
    private String strategyClassName;
    
    @Column(name = "topic", nullable = false)
    private String topic;
    
    /**
     * the start of the time range of the insights used to generate the induction
     */
    @Column(name = "from_time", nullable = false)
    private ZonedDateTime fromTime;
    
    /**
     * the end of the time range of the insights used to generate the induction
     */
    @Column(name = "to_time", nullable = false)
    private ZonedDateTime toTime;
    
    /**
     * the ids of the insights used to generate the induction
     */
    @Column(name = "insight_ids", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<Integer> insightIds;

    /**
     * the ids of the inductions that matches instruction md5, covers the time range and replace the insights used to generate the induction
     */
    @Column(name = "induction_ids", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<Integer> inductionIds;
    
    @Column(name = "instruction_id", nullable = false)
    private Integer instructionId;
    
    @Column(name = "conclusion", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private ObjectNode conclusion;
    
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "source_time_lower_bound", nullable = false)
    private ZonedDateTime sourceTimeLowerBound;
    
    @Column(name = "source_time_upper_bound", nullable = false)
    private ZonedDateTime sourceTimeUpperBound;
    
    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private ZonedDateTime createTime;

    @Column(name = "show", nullable = false)
    @ColumnDefault("true")
    private boolean show;
    
    @Column(name = "deleted", nullable = false)
    @ColumnDefault("false")
    private boolean deleted;

    /* the following fields are from t_induction_instruction */

    @Column(name = "predicate", nullable = false, columnDefinition = "TEXT")
    private String predicate;
    
    @Column(name = "analysis", nullable = false, columnDefinition = "TEXT")
    private String analysis;
    
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;
    
    @Column(name = "instruction_md5", nullable = false)
    private String instructionMd5;
}
