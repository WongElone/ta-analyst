package traderalchemy.analyst.repositoryreadonly;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.po.UserOpinionPo;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserOpinionRepositoryRo {
    
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<UserOpinionPo> findAllToBeAnalysed(String strategyClassName, int maxCount) {
        return findAllToBeAnalysed(strategyClassName, ZonedDateTime.now().minusMonths(3).plusMinutes(1), maxCount);
    }

    public List<UserOpinionPo> findAllToBeAnalysed(String strategyClassName, ZonedDateTime createAfter, int maxCount) {
        if (createAfter == null) {
            createAfter = ZonedDateTime.now().minusMonths(3).plusMinutes(1); 
        } else if (createAfter.isBefore(ZonedDateTime.now().minusMonths(3))) {
            throw new IllegalArgumentException("createAfter must be within 3 months");
        }
        
        String sql = """
        SELECT
            u.id,
            u.strategy_class_name,
            u.opinion,
            u.create_time
        FROM
            t_user_opinion u
        WHERE
            NOT EXISTS (
                SELECT 1
                FROM t_analysed a
                WHERE a.source_id = u.id
                AND a.strategy_class_name = :strategyClassName
                AND a.category = 'user'
            )
            AND u.strategy_class_name = :strategyClassName
            AND u.create_time >= :createAfter
        ORDER BY u.create_time ASC
        LIMIT :maxCount
        """;
        
        MapSqlParameterSource parameters = new MapSqlParameterSource("strategyClassName", strategyClassName)
            .addValue("createAfter", Timestamp.from(createAfter.toInstant()))
            .addValue("maxCount", maxCount);
        
        List<UserOpinionPo> resultList = new ArrayList<>();
        namedParameterJdbcTemplate.query(sql, parameters, (rs, _) -> {
            resultList.add(new UserOpinionPo(
                rs.getInt("id"),
                rs.getString("strategy_class_name"),
                rs.getString("opinion"),
                rs.getTimestamp("create_time").toInstant().atZone(ZoneId.systemDefault())
            ));
            return null;
        });
        return resultList;
    }
}
