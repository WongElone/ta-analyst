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
@Table(name = "t_search_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SearchResultPo implements PoInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    
    @Column(name = "strategy_class_name", nullable = false)
    private String strategyClassName;
    
    @Column(name = "query", nullable = false, columnDefinition = "TEXT") // add columnDefinition = "TEXT" to avoid "Data truncation: Data too long for column" error
    private String query;
    
    @Column(name = "tool", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Const.SearchTool tool;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "content_md5", nullable = false)
    private String contentMd5;
    
    @Column(name = "url", columnDefinition = "TEXT")
    private String url;
    
    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Builder.Default
    private ZonedDateTime createTime = ZonedDateTime.now();
}
