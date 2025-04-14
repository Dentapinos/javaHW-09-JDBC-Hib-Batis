package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.exception.*;
import org.example.repository.EntityRepository;

import java.util.List;

@Slf4j
public class ModelCarService extends AbstractEntityService<ModelCar, BrandCar> {

    public ModelCarService(EntityRepository<ModelCar, BrandCar> repository) {
        super(repository);
    }

    public ModelCar save(ModelCar model) {
        log.info("Сохранение {}", model.getClass().getSimpleName());
        try {
            repository.save(model);
            log.info("{} сохранен: {}", model.getClass().getSimpleName(), model);
            return model;
        } catch (EntitySaveException e) {
            log.error("Ошибка сохранения {}:", model.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public void deleteById(long id) {
        if (id < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
        log.info("Удаление ModelCar по id={}", id);
        try {
            repository.delete(id);
            log.info("ModelCar удалена по id={}", id);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления ModelCar по id={}:", id, e);
            throw e;
        }
    }

    public void deleteByEntity(ModelCar model) {
        if (model.getId() < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
        log.info("Удаление ModelCar по по объекту {}", model);
        try {
            repository.delete(model.getId());
            log.info("ModelCar удалена по объекту {}", model);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления ModelCar по объекту {}:", model, e);
            throw e;
        }
    }

    public void deleteAll() {
        log.info("Удаление всех ModelCars");
        try {
            repository.deleteAll();
            log.info("Все ModelCars удалены");
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления всех ModelCars:", e);
            throw e;
        }
    }

    public ModelCar update(ModelCar model) {
        log.info("Обновление ModelCar: {}", model);
        try {
            ModelCar loadedModelCar = repository.findById(model.getId());
            if (loadedModelCar != null) {
                repository.update(model);
                log.info("ModelCar обновлен: {}", loadedModelCar);
                return loadedModelCar;
            } else {
                log.warn("ModelCar с таким id={} не найден", model.getId());
                throw new EntityNotFoundException("ModelCar с таким id=" + model.getId() + " не найден");
            }
        } catch (EntityUpdateException e) {
            log.error("Ошибка обновления ModelCar: {}", model, e);
            throw e;
        }
    }

    public ModelCar getById(long id) {
        log.info("Получение ModelCar по id={}", id);
        try {
            ModelCar model = repository.findById(id);
            log.info("ModelCar получена: {}", model);
            return model;
        } catch (EntityNotFoundException e) {
            log.error("ModelCar c id={} не найдена:", id, e);
            throw e;
        }
    }

    public List<ModelCar> getAll() {
        log.info("Получение всех ModelCars");
        try {
            List<ModelCar> models = repository.findAll();
            log.info("Все ModelCars получены: {}", models);
            return models;
        } catch (RepositoryException e) {
            log.error("Ошибка получения всех ModelCars", e);
            throw e;
        }
    }

    public List<BrandCar> getRelatedEntityByParentId(long id) {
        log.info("Получение связанных объектов BrandCar по id={} ModelCar", id);
        try {
            List<BrandCar> modelCarList = repository.getRelatedEntityByParentId(id);
            log.info("Связанные объекты BrandCar для ModelCar получены: {}", modelCarList.size());
            return modelCarList;
        } catch (EntityNotFoundException e) {
            log.error("Ошибка получения BrandCar для ModelCar: ", e);
            throw e;
        }
    }
}
