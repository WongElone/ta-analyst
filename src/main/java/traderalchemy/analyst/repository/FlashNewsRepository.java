package traderalchemy.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.FlashNewsPo;

@Repository
public interface FlashNewsRepository extends JpaRepository<FlashNewsPo, Integer> {
}
