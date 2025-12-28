package traderalchemy.analyst.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.Const;
import traderalchemy.analyst.dao.InsightDao;
import traderalchemy.analyst.dao.StrategyMetadataDao;
import traderalchemy.analyst.dao.UserOpinionDao;
import traderalchemy.analyst.dto.ResponseWrapper;
import traderalchemy.analyst.dto.UserOpinionDto;
import traderalchemy.analyst.po.UserOpinionPo;

import java.time.ZonedDateTime;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/user-opinion")
@RequiredArgsConstructor
public class UserOpinionController {
    
    private final UserOpinionDao userOpinionDao;
    private final StrategyMetadataDao strategyMetadataDao;
    private final InsightDao insightDao;

    @PostMapping
    public ResponseWrapper<Void> newUserOpinion(@RequestBody @Valid UserOpinionDto userOpinion) {
        try {
            int currentMinute = ZonedDateTime.now().getMinute();
            if (currentMinute == 59 || currentMinute <= 5) {
                return ResponseWrapper.failure(ResponseWrapper.CODE_OPERATION_NOT_ALLOWED, 
                    "Cannot add opinions between minute 59 to 05 as analyst module is analyzing opinions");
            }
            
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
    
    @DeleteMapping("/{id}")
    public ResponseWrapper<Void> deleteUserOpinion(@PathVariable Integer id) {
        try {
            int currentMinute = ZonedDateTime.now().getMinute();
            if (currentMinute == 59 || currentMinute <= 5) {
                return ResponseWrapper.failure(ResponseWrapper.CODE_OPERATION_NOT_ALLOWED, 
                    "Cannot delete opinions between minute 59 to 05 as analyst module is analyzing opinions");
            }
            
            var opinion = userOpinionDao.query(repo -> repo.findById(id));
            if (opinion.isEmpty()) {
                return ResponseWrapper.failure(ResponseWrapper.CODE_INVALID_ARGUMENT, "Opinion not found");
            }
            
            boolean existingInsight = insightDao.query(repo -> 
                repo.existsByCategoryAndSourceId(Const.InsightCategory.user, id));
            if (existingInsight) {
                return ResponseWrapper.failure(ResponseWrapper.CODE_OPERATION_NOT_ALLOWED, 
                    "Cannot delete opinion that has associated insight");
            }
            
            userOpinionDao.txn((repo) -> {
                repo.deleteById(id);
            });
        } catch (Exception e) {
            return ResponseWrapper.failure(ResponseWrapper.CODE_DB_ERROR, e.getMessage());
        }
        return ResponseWrapper.success(null);
    }
    
}
