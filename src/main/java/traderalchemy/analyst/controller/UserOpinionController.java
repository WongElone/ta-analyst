package traderalchemy.analyst.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.dao.StrategyMetadataDao;
import traderalchemy.analyst.dao.UserOpinionDao;
import traderalchemy.analyst.dto.ResponseWrapper;
import traderalchemy.analyst.dto.UserOpinionDto;
import traderalchemy.analyst.po.UserOpinionPo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/user-opinion")
@RequiredArgsConstructor
public class UserOpinionController {
    
    private final UserOpinionDao userOpinionDao;
    private final StrategyMetadataDao strategyMetadataDao;

    @PostMapping
    public ResponseWrapper<Void> newUserOpinion(@RequestBody @Valid UserOpinionDto userOpinion) {
        try {
            var strategy = strategyMetadataDao.query(repo -> repo.findByStrategyClassName(userOpinion.getStrategyClassName()));
            if (strategy.isEmpty()) {
                return ResponseWrapper.failure(ResponseWrapper.CODE_INVALID_ARGUMENT, "Strategy not found");
            }
            userOpinionDao.txn(repo -> {
                repo.save(UserOpinionPo.builder()
                    .strategyClassName(userOpinion.getStrategyClassName())
                    .opinion(userOpinion.getOpinion())
                    .build());
            });
        } catch (Exception e) {
            return ResponseWrapper.failure(ResponseWrapper.CODE_DB_ERROR, e.getMessage());
        }
        return ResponseWrapper.success(null);
    }
    
}
