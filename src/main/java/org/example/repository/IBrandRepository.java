package org.example.repository;

import org.example.entity.BrandCar;
import org.example.entity.ModelCar;

import java.util.List;

public interface IBrandRepository extends EntityRepository<BrandCar, ModelCar> {
    void save(BrandCar brandCar);

    void delete(long id);

    void deleteAll();

    void update(BrandCar entity);

    BrandCar findById(long id);

    List<BrandCar> findAll();

    List<ModelCar> getRelatedEntityByParentId(long id);
}
