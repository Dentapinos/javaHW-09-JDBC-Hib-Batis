package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.House;
import org.example.entity.Street;
import org.example.exception.*;
import org.example.repository.EntityRepository;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class StreetService {

    private EntityRepository<Street, House> repository;

    public Street save(Street brandCar) {
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
        log.info("Удаление Street по id={}", id);
        try {
            checkingValueIsLessThanZero(id);
            repository.delete(id);
            log.info("Street удалена по id={}", id);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Street по id={}:", id, e);
            throw e;
        }
    }

    public void deleteByEntity(Street street) {
        log.info("Удаление Street по по объекту {}", street);
        try {
            checkingValueIsLessThanZero(street.getId());
            repository.delete(street.getId());
            log.info("Street удалена по объекту {}", street);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Street по объекту {}:", street, e);
            throw e;
        }
    }

    private void checkingValueIsLessThanZero(long id) throws EntityDeleteException {
        if (id < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
    }

    public void deleteAll() {
        log.info("Удаление всех Streets");
        try {
            repository.deleteAll();
            log.info("Все Streets удалены");
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления всех Streets:", e);
            throw e;
        }
    }

    public Street update(Street street) {
        log.info("Обновление Street: {}", street);
        try {
            Street loadedBrand = repository.findById(street.getId());
            if (loadedBrand != null) {
                repository.update(street);
                log.info("Street обновлен: {}", loadedBrand);
                return loadedBrand;
            } else {
                log.warn("Street с таким id={} не найден", street.getId());
                throw new EntityNotFoundException("Street с таким id=" + street.getId() + " не найден");
            }
        } catch (EntityUpdateException e) {
            log.error("Ошибка обновления Street: {}", street, e);
            throw e;
        }
    }

    public Street getById(long id) {
        log.info("Получение Street по id={}", id);
        try {
            Street street = repository.findById(id);
            log.info("Street получена: {}", street);
            return street;
        } catch (RepositoryException e) {
            log.error("Street c id={} не найдена:", id, e);
            throw e;
        }
    }

    public List<Street> getAll() {
        log.info("Получение всех Streets");
        try {
            List<Street> brandCarList = repository.findAll();
            log.info("Все Streets получены: {}", brandCarList);
            return brandCarList;
        } catch (RepositoryException e) {
            log.error("Ошибка получения всех Streets", e);
            throw e;
        }
    }

    public List<House> getRelatedEntityByParentId(long id) {
        log.info("Получение связанных объектов Houses по Street id={}", id);
        try {
            List<House> houseList = repository.getRelatedEntityByParentId(id);
            log.info("Связанные объекты Houses для Street получены: {}", houseList.size());
            return houseList;
        } catch (EntityNotFoundException e) {
            log.error("Ошибка получения Houses для Street: ", e);
            throw e;
        }
    }
}
