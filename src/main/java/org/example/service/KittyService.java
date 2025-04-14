package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.exception.*;
import org.example.repository.EntityRepository;

import java.util.List;

@Slf4j
public class KittyService extends AbstractEntityService<Kitty, Master> {

    public KittyService(EntityRepository<Kitty, Master> entityRepository) {
        super(entityRepository);
    }

    public Kitty save(Kitty kitty) {
        log.info("Сохранение {}", kitty.getClass().getSimpleName());
        try {
            repository.save(kitty);
            log.info("{} сохранен: {}", kitty.getClass().getSimpleName(), kitty);
            return kitty;
        } catch (EntitySaveException e) {
            log.error("Ошибка сохранения {}:", kitty.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public void deleteById(long id) {
        if (id < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
        log.info("Удаление Kitty по id={}", id);
        try {
            repository.delete(id);
            log.info("Kitty удалена по id={}", id);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Kitty по id={}:", id, e);
            throw e;
        }
    }

    public void deleteByEntity(Kitty kitty) {
        if (kitty.getId() < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
        log.info("Удаление Kitty по по объекту {}", kitty);
        try {
            repository.delete(kitty.getId());
            log.info("Kitty удалена по объекту {}", kitty);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Kitty по объекту {}:", kitty, e);
            throw e;
        }
    }

    public void deleteAll() {
        log.info("Удаление всех Kitties");
        try {
            repository.deleteAll();
            log.info("Все Kitties удалены");
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления всех Kitties:", e);
            throw e;
        }
    }

    public Kitty update(Kitty kitty) {
        log.info("Обновление Kitty: {}", kitty);
        try {
            Kitty loadedEmployee = repository.findById(kitty.getId());
            if (loadedEmployee != null) {
                repository.update(kitty);
                log.info("Kitty обновлен: {}", loadedEmployee);
                return loadedEmployee;
            } else {
                log.warn("Kitty с таким id={} не найден", kitty.getId());
                throw new EntityNotFoundException("Kitty с таким id=" + kitty.getId() + " не найден");
            }
        } catch (EntityUpdateException e) {
            log.error("Ошибка обновления Kitty: {}", kitty, e);
            throw e;
        }
    }

    public Kitty getById(long id) {
        log.info("Получение Kitty по id={}", id);
        try {
            Kitty kitty = repository.findById(id);
            log.info("Kitty получена: {}", kitty);
            return kitty;
        } catch (EntityNotFoundException e) {
            log.error("Kitty c id={} не найдена:", id, e);
            throw e;
        }
    }

    public List<Kitty> getAll() {
        log.info("Получение всех Kitties");
        try {
            List<Kitty> kitties = repository.findAll();
            log.info("Все Kitties получены: {}", kitties);
            return kitties;
        } catch (RepositoryException e) {
            log.error("Ошибка получения всех Kitties", e);
            throw e;
        }
    }

    public List<Master> getRelatedEntityByParentId(long id) {
        log.info("Получение связанных объектов Master по id={} Kitty", id);
        try {
            List<Master> masterList = repository.getRelatedEntityByParentId(id);
            log.info("Связанные объекты Master для Kitty получены: {}", masterList.size());
            return masterList;
        } catch (EntityNotFoundException e) {
            log.error("Ошибка получения Master для Kitty: ", e);
            throw e;
        }
    }
}
