package traderalchemy.analyst.repositoryreadonly;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.Const;
import traderalchemy.analyst.po.ArticlePo;

@Slf4j
@Repository
@AllArgsConstructor
public class ArticleRepositoryRo {
    
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<ArticlePo> findAllToBeAnalysed(String strategyClassName, int maxCount) {
        return findAllToBeAnalysed(strategyClassName, ZonedDateTime.now().minusDays(7).plusMinutes(1), maxCount);
    } 

    public List<ArticlePo> findAllToBeAnalysed(String strategyClassName, ZonedDateTime publishAfter, int maxCount) {
        if (publishAfter.isBefore(ZonedDateTime.now().minusDays(7))) {
            throw new IllegalArgumentException("publishAfter must be within 7 days");
        }
        String sql = """
        SELECT
            s.id,
            s.source,
            s.site,
            s.title,
            s.title_md5,
            s.content,
            s.url,
            s.create_time,
            s.publish_time
        FROM
            t_article s
        WHERE
            NOT EXISTS (
                SELECT 1
                FROM t_analysed a
                WHERE a.source_id = s.id
                AND a.strategy_class_name = :strategyClassName
                AND a.category = 'article'
            )
            AND s.publish_time >= :publishAfter
        ORDER BY s.publish_time ASC
        LIMIT :maxCount
        """;
        SqlParameterSource parameters = new MapSqlParameterSource("strategyClassName", strategyClassName)
            .addValue("publishAfter", Timestamp.from(publishAfter.toInstant()))
            .addValue("maxCount", maxCount);
        List<ArticlePo> resultList = new ArrayList<>();
        namedParameterJdbcTemplate.query(sql, parameters, (rs, _) -> {
            resultList.add(new ArticlePo(
                rs.getInt("id"),
                Const.ArticleSource.valueOf(rs.getString("source")),
                Const.ArticleSite.valueOf(rs.getString("site")),
                rs.getString("title"),
                rs.getString("title_md5"),
                rs.getString("content"),
                rs.getString("url"),
                rs.getTimestamp("create_time").toInstant().atZone(ZoneId.systemDefault()),
                rs.getTimestamp("publish_time").toInstant().atZone(ZoneId.systemDefault())
            ));
            return null;
        });
        return resultList;
    }
}
