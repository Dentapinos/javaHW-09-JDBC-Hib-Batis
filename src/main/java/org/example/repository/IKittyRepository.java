package org.example.repository;

import org.example.entity.Kitty;
import org.example.entity.Master;

import java.util.List;

public interface IKittyRepository extends EntityRepository<Kitty, Master> {
    void save(Kitty kitty);

    void delete(long id);

    void deleteAll();

    void update(Kitty kitty);

    Kitty findById(long id);

    List<Kitty> findAll();

    List<Master> getRelatedEntityByParentId(long id);
}
