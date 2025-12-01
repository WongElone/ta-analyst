package traderalchemy.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.UserOpinionPo;

@Repository
public interface UserOpinionRepository extends JpaRepository<UserOpinionPo, Integer> {
}
