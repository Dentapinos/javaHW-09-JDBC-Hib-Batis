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
public class HouseService {

    private EntityRepository<House, Street> repository;

    public House save(House house) {
        log.info("Сохранение {}", house.getClass().getSimpleName());
        try {
            repository.save(house);
            log.info("{} сохранен: {}", house.getClass().getSimpleName(), house);
            return house;
        } catch (EntitySaveException e) {
            log.error("Ошибка сохранения {}:", house.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public void deleteById(long id) {
        log.info("Удаление House по id={}", id);
        try {
            checkingValueIsLessThanZero(id);
            repository.delete(id);
            log.info("House удалена по id={}", id);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления House по id={}:", id, e);
            throw e;
        }
    }

    public void deleteByEntity(House house) {
        log.info("Удаление House по по объекту {}", house);
        try {
            checkingValueIsLessThanZero(house.getId());
            repository.delete(house.getId());
            log.info("House удалена по объекту {}", house);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления House по объекту {}:", house, e);
            throw e;
        }
    }

    private void checkingValueIsLessThanZero(long id) throws EntityDeleteException {
        if (id < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
    }

    public void deleteAll() {
        log.info("Удаление всех Houses");
        try {
            repository.deleteAll();
            log.info("Все Houses удалены");
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления всех Houses:", e);
            throw e;
        }
    }

    public House update(House house) {
        log.info("Обновление House: {}", house);
        try {
            House loadedTask = repository.findById(house.getId());
            if (loadedTask != null) {
                repository.update(house);
                log.info("House обновлен: {}", loadedTask);
                return loadedTask;
            } else {
                log.warn("House с таким id={} не найден", house.getId());
                throw new EntityNotFoundException("House с таким id=" + house.getId() + " не найден");
            }
        } catch (EntityUpdateException e) {
            log.error("Ошибка обновления House: {}", house, e);
            throw e;
        }
    }

    public House getById(long id) {
        log.info("Получение House по id={}", id);
        try {
            House house = repository.findById(id);
            log.info("House получена: {}", house);
            return house;
        } catch (RepositoryException e) {
            log.error("House c id={} не найдена:", id, e);
            throw e;
        }
    }

    public List<House> getAll() {
        log.info("Получение всех Houses");
        try {
            List<House> houses = repository.findAll();
            log.info("Все Houses получены: {}", houses);
            return houses;
        } catch (RepositoryException e) {
            log.error("Ошибка получения всех Houses", e);
            throw e;
        }
    }

    public List<Street> getRelatedEntityByParentId(long id) {
        log.info("Получение связанных объектов Street по House id={}", id);
        try {
            List<Street> streetList = repository.getRelatedEntityByParentId(id);
            log.info("Связанные объекты Street для House получены: {}", streetList.size());
            return streetList;
        } catch (EntityNotFoundException e) {
            log.error("Ошибка получения Street для House: ", e);
            throw e;
        }
    }
}
