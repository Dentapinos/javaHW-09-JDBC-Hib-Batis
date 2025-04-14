package org.example.service;


import lombok.AllArgsConstructor;
import org.example.repository.EntityRepository;

import java.util.List;

@AllArgsConstructor
public abstract class AbstractEntityService<T, R> {
    protected EntityRepository<T, R> repository;

    public abstract T save(T entity);

    public abstract void deleteById(long id);

    public abstract void deleteByEntity(T entity);

    public abstract List<T> getAll();

    public abstract void deleteAll();

    public abstract T update(T entity);

    public abstract T getById(long id);

    public abstract List<R> getRelatedEntityByParentId(long id);
}
