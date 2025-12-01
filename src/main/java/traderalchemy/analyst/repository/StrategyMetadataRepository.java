package traderalchemy.analyst.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.StrategyMetadataPo;

@Repository
public interface StrategyMetadataRepository extends JpaRepository<StrategyMetadataPo, Integer> {

    Optional<StrategyMetadataPo> findByStrategyClassName(String strategyClassName);
}
