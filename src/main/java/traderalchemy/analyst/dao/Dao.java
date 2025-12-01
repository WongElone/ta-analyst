package traderalchemy.analyst.dao;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Dao<Repo extends JpaRepository<?, ?>> {
    
    protected final Repo repo;

    @Transactional
    public void txn(Consumer<Repo> consumer) {
        consumer.accept(repo);
    }

    @Transactional
    public <R> R txn(Function<Repo, R> function) {
        return function.apply(repo);
    }

    public <R> R query(Function<Repo, R> function) {
        return function.apply(repo);
    }
}
