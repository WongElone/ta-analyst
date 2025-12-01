package traderalchemy.analyst.po;

import traderalchemy.analyst.Const;
import traderalchemy.analyst.Global;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.HashMap;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Immutable;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Immutable
@Table(name = "t_strategy_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StrategyMetadataPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "strategy_class_name", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String strategyClassName;

    @Column(name = "exchange_map_account_id", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'{}'")
    @Builder.Default
    private Map<Const.Exchange, String> exchangeMapAccountId = new HashMap<>();

    @Column(name = "exchange_map_currency", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'{}'")
    @Builder.Default
    private Map<Const.Exchange, String> exchangeMapCurrency = new HashMap<>();

    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Const.StrategyStatus status;

    @Column(name = "memories", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'{}'")
    @Builder.Default
    private ObjectNode memories = Global.createObjectNode();

    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Builder.Default
    private ZonedDateTime createTime = ZonedDateTime.now();

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Builder.Default
    private ZonedDateTime updateTime = ZonedDateTime.now();
}
