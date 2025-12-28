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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.Const;
import traderalchemy.analyst.po.FlashNewsPo;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FlashNewsRepositoryRo {
    
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<FlashNewsPo> findAllToBeAnalysed(String strategyClassName, int maxCount) {
        return findAllToBeAnalysed(strategyClassName, ZonedDateTime.now().minusDays(1).plusMinutes(1), maxCount);
    } 

    public List<FlashNewsPo> findAllToBeAnalysed(String strategyClassName, ZonedDateTime publishAfter, int maxCount) {
        if (publishAfter.isBefore(ZonedDateTime.now().minusDays(1))) {
            throw new IllegalArgumentException("publishAfter must be within 1 day");
        }
        String sql = """
        SELECT
            f.id,
            f.source,
            f.site,
            f.title,
            f.title_md5,
            f.description,
            f.url,
            f.create_time,
            f.publish_time
        FROM
            t_flash_news f
        WHERE
            NOT EXISTS (
                SELECT 1
                FROM t_analysed a
                WHERE a.source_id = f.id
                AND a.strategy_class_name = :strategyClassName
                AND a.category = 'flashnews'
            )
            AND f.publish_time >= :publishAfter
        ORDER BY f.publish_time ASC
        LIMIT :maxCount
        """;
        SqlParameterSource parameters = new MapSqlParameterSource("strategyClassName", strategyClassName)
            .addValue("publishAfter", Timestamp.from(publishAfter.toInstant()))
            .addValue("maxCount", maxCount);
        List<FlashNewsPo> resultList = new ArrayList<>();
        namedParameterJdbcTemplate.query(sql, parameters, (rs, _) -> {
            resultList.add(new FlashNewsPo(
                rs.getInt("id"),
                Const.FlashNewsSource.valueOf(rs.getString("source")),
                Const.FlashNewsSite.valueOf(rs.getString("site")),
                rs.getString("title"),
                rs.getString("title_md5"),
                rs.getString("description"),
                rs.getString("url"),
                rs.getTimestamp("create_time").toInstant().atZone(ZoneId.systemDefault()),
                rs.getTimestamp("publish_time").toInstant().atZone(ZoneId.systemDefault())
            ));
            return null;
        });
        return resultList;
    }
}
