package traderalchemy.analyst.vo;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.node.ObjectNode;

sealed public interface InductionOrInsightVo permits InductionVo, InsightVo {
    
    default int compareTo(InductionOrInsightVo other) {
        if (this instanceof InductionVo) {
            if (other instanceof InductionVo) {
                return ((InductionVo) this).getFromTime().compareTo(((InductionVo) other).getFromTime());
            } else {
                return ((InductionVo) this).getFromTime().compareTo(((InsightVo) other).getCreateTime());
            }
        } else {
            if (other instanceof InductionVo) {
                return ((InsightVo) this).getCreateTime().compareTo(((InductionVo) other).getFromTime());
            } else {
                return ((InsightVo) this).getCreateTime().compareTo(((InsightVo) other).getCreateTime());
            }
        }
    }

    default String getVoId() {
        return switch (this) {
            case InductionVo inductionVo -> inductionVo.getId() != null ? "ind_" + inductionVo.getId() : null;
            case InsightVo insightVo -> insightVo.getId() != null ? "ins_" + insightVo.getId() : null;
        };
    }

    Integer getId();

    String getStrategyClassName();

    String getTopic();

    Integer getInstructionId();

    ObjectNode getConclusion();

    String getReason();

    ZonedDateTime getCreateTime();

    String getPredicate();

    String getAnalysis();

    String getAnswer();

    String getInstructionMd5();

    ZonedDateTime getFromTime();

    ZonedDateTime getToTime();

    ZonedDateTime getSourceTimeLowerBound();

    ZonedDateTime getSourceTimeUpperBound();
}
