package traderalchemy.analyst.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import traderalchemy.analyst.Const.SearchDirection;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchInstruction {
    ZonedDateTime fromTime;
    ZonedDateTime toTime;
    SearchDirection direction;
    String subject;
    List<String> scopes;
}
