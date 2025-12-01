package traderalchemy.analyst.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.dao.AnalysedDao;
import traderalchemy.analyst.dao.InducedDao;
import traderalchemy.analyst.dao.InductionDao;
import traderalchemy.analyst.dao.InsightDao;
import traderalchemy.analyst.po.AnalysedPo;
import traderalchemy.analyst.po.InducedPo;
import traderalchemy.analyst.po.InductionPo;
import traderalchemy.analyst.po.InsightPo;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradebotDbService {
    
    private final InducedDao inducedDao;
    private final InductionDao inductionDao;
    private final AnalysedDao analysedDao;
    private final InsightDao insightDao;

    /**
     * Insert analysis records and insights with transaction
     * @param inducedPos
     * @param inductionPos
     */
    @Transactional
    public void insertInduceRecordsAndInductions(List<InducedPo> inducedPos, List<InductionPo> inductionPos) {
        inducedDao.txn(repo -> {
            repo.saveAll(inducedPos);
        });
        inductionDao.txn(repo -> {
            repo.saveAll(inductionPos);
        });
    }

    /**
     * Insert analysis records and insights with transaction
     * @param analysedPos
     * @param insightPos
     */
    @Transactional
    public void insertAnalysisRecordsAndInsights(List<AnalysedPo> analysedPos, List<InsightPo> insightPos) {
        analysedDao.txn(repo -> {
            repo.saveAll(analysedPos);
        });
        insightDao.txn(repo -> {
            repo.saveAll(insightPos);
        });
    }
}
