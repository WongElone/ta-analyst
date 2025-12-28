package traderalchemy.analyst.po;

import java.time.ZonedDateTime;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import traderalchemy.analyst.util.TimeRangeUtils.TimeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "t_induction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InductionPo implements TimeRange, PoInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    
    @Column(name = "show", nullable = false)
    @ColumnDefault("true")
    @Builder.Default
    private boolean show = true;
    
    @Column(name = "deleted", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean deleted = false;
    
    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Builder.Default
    private ZonedDateTime createTime = ZonedDateTime.now();
}
