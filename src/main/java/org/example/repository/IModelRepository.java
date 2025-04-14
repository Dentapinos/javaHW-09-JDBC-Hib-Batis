package org.example.repository;

import org.example.entity.BrandCar;
import org.example.entity.ModelCar;

import java.util.List;

public interface IModelRepository extends EntityRepository<ModelCar, BrandCar> {
    void save(ModelCar entity);

    void delete(long id);

    void deleteAll();

    void update(ModelCar entity);

    ModelCar findById(long id);

    List<ModelCar> findAll();

    List<BrandCar> getRelatedEntityByParentId(long id);
}
