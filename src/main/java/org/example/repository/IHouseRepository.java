package org.example.repository;

import org.example.entity.House;
import org.example.entity.Street;

import java.util.List;

public interface IHouseRepository extends EntityRepository<House, Street> {
    void save(House house);

    void delete(long id);

    void deleteAll();

    void update(House house);

    House findById(long id);

    List<House> findAll();

    List<Street> getRelatedEntityByParentId(long id);
}
