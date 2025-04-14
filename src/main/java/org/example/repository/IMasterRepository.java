package org.example.repository;

import org.example.entity.Kitty;
import org.example.entity.Master;

import java.util.List;

public interface IMasterRepository extends EntityRepository<Master, Kitty> {
    void save(Master master);

    void delete(long id);

    void deleteAll();

    void update(Master master);

    Master findById(long id);

    List<Master> findAll();

    List<Kitty> getRelatedEntityByParentId(long id);
}
