package org.example.repository;

import java.util.List;

public interface EntityRepository<T, R> {
    void save(T entity);

    void delete(long id);

    void deleteAll();

    void update(T entity);

    T findById(long id);

    List<T> findAll();

    List<R> getRelatedEntityByParentId(long id);
}
