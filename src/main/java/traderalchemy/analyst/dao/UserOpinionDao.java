package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.UserOpinionRepository;

@Component
public class UserOpinionDao extends Dao<UserOpinionRepository> {
    
    public UserOpinionDao(UserOpinionRepository repo) {
        super(repo);
    }
}
