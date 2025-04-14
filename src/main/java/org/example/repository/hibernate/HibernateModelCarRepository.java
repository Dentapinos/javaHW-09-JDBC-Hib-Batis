package org.example.repository.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IModelRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class HibernateModelCarRepository implements IModelRepository {

    public void save(ModelCar model) throws EntitySaveException {
        if (model.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", model.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + model.getId());
        }
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                session.persist(model);
                transaction.commit();
                log.info("{} сохранен: {}", model.getClass().getSimpleName(), model);
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", model.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + model.getClass().getSimpleName(), e);
        }
    }

    public ModelCar findById(long id) throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            ModelCar loadedEntity = session.get(ModelCar.class, id);
            if (loadedEntity == null) {
                log.warn("ModelCar с таким id не найдено: {}", id);
                throw new EntityNotFoundException("ModelCar c id=" + id + " не найден");
            }
            log.info("ModelCar найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения ModelCar по id={}", id, e);
            throw new RepositoryException("Ошибка получения ModelCar по id=" + id, e);
        }
    }

    public List<ModelCar> findAll() throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            List<ModelCar> models = session.createQuery("from ModelCar", ModelCar.class).list();
            log.info("Получены все ModelCars: {}", models);
            return models;
        } catch (Exception e) {
            log.error("Ошибка получения всех ModelCars:", e);
            throw new RepositoryException("Ошибка получения всех ModelCars:", e);
        }
    }

    public void update(ModelCar model) throws EntityUpdateException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.merge(model);
            transaction.commit();
            log.info("ModelCar обновлена: {}", model);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка обновления ModelCar {}:", model, e);
            throw new EntitySaveException("Ошибка обновления ModelCar", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                ModelCar model = session.get(ModelCar.class, id);
                if (model != null) {
                    session.remove(model);
                    log.info("ModelCar удалена: {}", model);
                } else {
                    log.warn("ModelCar с id={} не найдена", id);
                    throw new EntityNotFoundException("ModelCar с id=" + id + " не найдена");
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления ModelCar по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления ModelCar по id", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();

            String deleteHql = "DELETE FROM ModelCar";
            int deleteResult = session.createQuery(deleteHql).executeUpdate();
            transaction.commit();
            log.info("Все {} ModelsCar удалены", deleteResult);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка удаления всех ModelsCar:", e);
            throw new RepositoryException("Ошибка удаления всех ModelsCar:", e);
        }
    }

    public List<BrandCar> getRelatedEntityByParentId(long id) {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            session.beginTransaction();
            String jpql = """
                       SELECT b FROM ModelCar mc JOIN mc.brand b
                       WHERE mc.id = :id
                    """;
            Query<BrandCar> query = session.createQuery(jpql, BrandCar.class);
            query.setParameter("id", id);
            query.setMaxResults(5);
            session.getTransaction().commit();
            List<BrandCar> brandCars = query.list();
            log.info("{} BrandCar для Models получены", brandCars.size());
            return brandCars;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения BrandCar для Models", e);
            throw new RepositoryException("Ошибка получения  BrandCar для Models", e);
        }
    }
}
