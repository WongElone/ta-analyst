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
@Table(name = "t_article")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ArticlePo implements PoInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    
    @Column(name = "source", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Const.ArticleSource source;
    
    @Column(name = "site", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Const.ArticleSite site;
    
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;
    
    @Column(name = "title_md5", nullable = false)
    private String titleMd5;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;
    
    @Column(name = "create_time", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Builder.Default
    private ZonedDateTime createTime = ZonedDateTime.now();
    
    @Column(name = "publish_time", nullable = false)
    private ZonedDateTime publishTime;
}
