package org.delcom.ifs25020springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Port generik CRUD + findBy. */
public interface IRepository<T, ID> {
    List<T> findAll();

    Optional<T> findById(ID id);

    List<T> findBy(Predicate<T> filter);

    T update(T entity);

    void deleteById(ID id);
}
