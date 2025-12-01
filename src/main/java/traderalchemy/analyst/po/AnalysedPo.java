package traderalchemy.analyst.po;

import traderalchemy.analyst.Const;

import java.time.ZonedDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "t_analysed")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnalysedPo implements PoInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    
    @Column(name = "strategy_class_name", nullable = false)
    private String strategyClassName;
    
    @Column(name = "category", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Const.InsightCategory category;
    
    @Column(name = "source_id", nullable = false)
    private Integer sourceId;
    
    @Column(name = "instruction_id", nullable = false)
    private Integer instructionId;
    
    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Builder.Default
    private ZonedDateTime createTime = ZonedDateTime.now();
}
