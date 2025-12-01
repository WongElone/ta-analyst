package traderalchemy.analyst.po;

import traderalchemy.analyst.Const;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.node.ObjectNode;

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
@Table(name = "t_analysis_instruction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnalysisInstructionPo implements PoInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    
    @Column(name = "strategy_class_name", nullable = false)
    private String strategyClassName;
    
    @Column(name = "category", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Const.InsightCategory category;
    
    @Column(name = "topic", nullable = false)
    private String topic;
    
    @Column(name = "predicate", nullable = false, columnDefinition = "TEXT")
    private String predicate;
    
    @Column(name = "analysis", nullable = false, columnDefinition = "TEXT")
    private String analysis;
    
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private ObjectNode answer;
    
    @Column(name = "instruction_md5", nullable = false)
    private String instructionMd5;
    
    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Builder.Default
    private ZonedDateTime createTime = ZonedDateTime.now();
}
