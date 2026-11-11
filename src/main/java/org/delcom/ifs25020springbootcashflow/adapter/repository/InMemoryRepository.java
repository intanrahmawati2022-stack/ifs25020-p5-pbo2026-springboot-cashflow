package org.delcom.ifs25020springbootcashflow.adapter.repository;

import org.delcom.ifs25020springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Implementasi generik in-memory (untuk unit test). */
public abstract class InMemoryRepository<T> implements IRepository<T, Integer> {
    protected final Map<Integer, T> store = new LinkedHashMap<>();
    private int sequence = 0;

    protected abstract int idOf(T entity);

    protected abstract void assignId(T entity, int id);

    protected int nextId() {
        return ++sequence;
    }

    protected T put(T entity) {
        assignId(entity, nextId());
        store.put(idOf(entity), entity);
        return entity;
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findBy(Predicate<T> filter) {
        return store.values().stream().filter(filter).toList();
    }

    @Override
    public T update(T entity) {
        store.put(idOf(entity), entity);
        return entity;
    }

    @Override
    public void deleteById(Integer id) {
        store.remove(id);
    }
}
