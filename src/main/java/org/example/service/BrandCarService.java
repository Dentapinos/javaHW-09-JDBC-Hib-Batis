package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.exception.*;
import org.example.repository.EntityRepository;

import java.util.List;

@Slf4j
public class BrandCarService extends AbstractEntityService<BrandCar, ModelCar> {

    public BrandCarService(EntityRepository<BrandCar, ModelCar> entityRepository) {
        super(entityRepository);
    }

    public BrandCar save(BrandCar brandCar) {
        log.info("Сохранение {}", brandCar.getClass().getSimpleName());
        try {
            repository.save(brandCar);
            log.info("{} сохранен: {}", brandCar.getClass().getSimpleName(), brandCar);
            return brandCar;
        } catch (EntitySaveException e) {
            log.error("Ошибка сохранения {}:", brandCar.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public void deleteById(long id) {
        if (id < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
        log.info("Удаление BrandCar по id={}", id);
        try {
            repository.delete(id);
            log.info("BrandCar удалена по id={}", id);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления BrandCar по id={}:", id, e);
            throw e;
        }
    }

    public void deleteByEntity(BrandCar brandCar) {
        if (brandCar.getId() < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
        log.info("Удаление BrandCar по по объекту {}", brandCar);
        try {
            repository.delete(brandCar.getId());
            log.info("BrandCar удалена по объекту {}", brandCar);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления BrandCar по объекту {}:", brandCar, e);
            throw e;
        }
    }

    public void deleteAll() {
        log.info("Удаление всех BrandCars");
        try {
            repository.deleteAll();
            log.info("Все BrandCars удалены");
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления всех BrandCars:", e);
            throw e;
        }
    }

    public BrandCar update(BrandCar brandCar) {
        log.info("Обновление BrandCar: {}", brandCar);
        try {
            BrandCar loadedEmployee = repository.findById(brandCar.getId());
            if (loadedEmployee != null) {
                repository.update(brandCar);
                log.info("BrandCar обновлен: {}", loadedEmployee);
                return loadedEmployee;
            } else {
                log.warn("BrandCar с таким id={} не найден", brandCar.getId());
                throw new EntityNotFoundException("BrandCar с таким id=" + brandCar.getId() + " не найден");
            }
        } catch (EntityUpdateException e) {
            log.error("Ошибка обновления BrandCar: {}", brandCar, e);
            throw e;
        }
    }

    public BrandCar getById(long id) {
        log.info("Получение BrandCar по id={}", id);
        try {
            BrandCar brandCar = repository.findById(id);
            log.info("BrandCar получена: {}", brandCar);
            return brandCar;
        } catch (EntityNotFoundException e) {
            log.error("BrandCar c id={} не найдена:", id, e);
            throw e;
        }
    }

    public List<BrandCar> getAll() {
        log.info("Получение всех BrandCars");
        try {
            List<BrandCar> brandCars = repository.findAll();
            log.info("Все BrandCars получены: {}", brandCars);
            return brandCars;
        } catch (RepositoryException e) {
            log.error("Ошибка получения всех BrandCars", e);
            throw e;
        }
    }

    public List<ModelCar> getRelatedEntityByParentId(long id) {
        log.info("Получение связанных объектов ModelsCar по id={} BrandCar", id);
        try {
            List<ModelCar> modelCarList = repository.getRelatedEntityByParentId(id);
            log.info("Связанные объекты ModelsCar для BrandCar получены: {}", modelCarList.size());
            return modelCarList;
        } catch (EntityNotFoundException e) {
            log.error("Ошибка получения ModelsCar для BrandCar: ", e);
            throw e;
        }
    }
}
