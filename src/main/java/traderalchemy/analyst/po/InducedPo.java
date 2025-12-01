package traderalchemy.analyst.po;

import java.time.ZonedDateTime;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "t_induced")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InducedPo implements PoInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    
    @Column(name = "strategy_class_name", nullable = false)
    private String strategyClassName;
    
    @Column(name = "instruction_id", nullable = false)
    private Integer instructionId;
    
    @Column(name = "from_time", nullable = false)
    private ZonedDateTime fromTime;
    
    @Column(name = "to_time", nullable = false)
    private ZonedDateTime toTime;
    
    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Builder.Default
    private ZonedDateTime createTime = ZonedDateTime.now();
}
