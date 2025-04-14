package org.example.repository;

import org.example.entity.House;
import org.example.entity.Street;

import java.util.List;

public interface IStreetRepository extends EntityRepository<Street, House> {
    void save(Street street);

    void delete(long id);

    void deleteAll();

    void update(Street street);

    Street findById(long id);

    List<Street> findAll();

    List<House> getRelatedEntityByParentId(long id);
}
