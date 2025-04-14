package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.exception.*;
import org.example.repository.EntityRepository;

import java.util.List;

@Slf4j
public class MasterService extends AbstractEntityService<Master, Kitty> {

    public MasterService(EntityRepository<Master, Kitty> repository) {
        super(repository);
    }

    public Master save(Master master) {
        log.info("Сохранение {}", master.getClass().getSimpleName());
        try {
            repository.save(master);
            log.info("{} сохранен: {}", master.getClass().getSimpleName(), master);
            return master;
        } catch (EntitySaveException e) {
            log.error("Ошибка сохранения {}:", master.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public void deleteById(long id) {
        if (id < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
        log.info("Удаление Master по id={}", id);
        try {
            repository.delete(id);
            log.info("Master удалена по id={}", id);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Master по id={}:", id, e);
            throw e;
        }
    }

    public void deleteByEntity(Master master) {
        if (master.getId() < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
        log.info("Удаление Master по по объекту {}", master);
        try {
            repository.delete(master.getId());
            log.info("Master удалена по объекту {}", master);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Master по объекту {}:", master, e);
            throw e;
        }
    }

    public void deleteAll() {
        log.info("Удаление всех Masters");
        try {
            repository.deleteAll();
            log.info("Все Masters удалены");
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления всех Masters:", e);
            throw e;
        }
    }

    public Master update(Master master) {
        log.info("Обновление Master: {}", master);
        try {
            Master loadedEmployee = repository.findById(master.getId());
            if (loadedEmployee != null) {
                repository.update(master);
                log.info("Master обновлен: {}", loadedEmployee);
                return loadedEmployee;
            } else {
                log.warn("Master с таким id={} не найден", master.getId());
                throw new EntityNotFoundException("Master с таким id=" + master.getId() + " не найден");
            }
        } catch (EntityUpdateException e) {
            log.error("Ошибка обновления Master: {}", master, e);
            throw e;
        }
    }

    public Master getById(long id) {
        log.info("Получение Master по id={}", id);
        try {
            Master master = repository.findById(id);
            log.info("Master получена: {}", master);
            return master;
        } catch (EntityNotFoundException e) {
            log.error("Master c id={} не найдена:", id, e);
            throw e;
        }
    }

    public List<Master> getAll() {
        log.info("Получение всех Masters");
        try {
            List<Master> masters = repository.findAll();
            log.info("Все Masters получены: {}", masters);
            return masters;
        } catch (RepositoryException e) {
            log.error("Ошибка получения всех Masters", e);
            throw e;
        }
    }

    public List<Kitty> getRelatedEntityByParentId(long id) {
        log.info("Получение связанных объектов Kitty по id={} Master", id);
        try {
            List<Kitty> modelCarList = repository.getRelatedEntityByParentId(id);
            log.info("Связанные объекты Kitty для Master получены: {}", modelCarList.size());
            return modelCarList;
        } catch (EntityNotFoundException e) {
            log.error("Ошибка получения Kitty для Master: ", e);
            throw e;
        }
    }
}
