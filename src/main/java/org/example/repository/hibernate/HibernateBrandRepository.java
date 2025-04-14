package org.example.repository.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IBrandRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class HibernateBrandRepository implements IBrandRepository {

    public void save(BrandCar brandCar) throws EntitySaveException {
        if (brandCar.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", brandCar.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + brandCar.getId());
        }
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                session.persist(brandCar);
                transaction.commit();
                log.info("{} сохранен: {}", brandCar.getClass().getSimpleName(), brandCar);
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", brandCar.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + brandCar.getClass().getSimpleName(), e);
        }
    }

    public BrandCar findById(long id) throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            BrandCar loadedEntity = session.get(BrandCar.class, id);
            if (loadedEntity == null) {
                log.warn("BrandCar с таким id не найдено: {}", id);
                throw new EntityNotFoundException("BrandCar c id=" + id + " не найден");
            }
            log.info("BrandCar найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения BrandCar по id={}", id, e);
            throw new RepositoryException("Ошибка получения BrandCar по id=" + id, e);
        }
    }

    public List<BrandCar> findAll() throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            List<BrandCar> brandCars = session.createQuery("from BrandCar", BrandCar.class).list();
            log.info("Получены все BrandCars: {}", brandCars);
            return brandCars;
        } catch (Exception e) {
            log.error("Ошибка получения всех BrandCars:", e);
            throw new RepositoryException("Ошибка получения всех BrandCars:", e);
        }
    }

    public void update(BrandCar brandCar) throws EntityUpdateException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.merge(brandCar);
            transaction.commit();
            log.info("BrandCar обновлена: {}", brandCar);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка обновления BrandCar {}:", brandCar, e);
            throw new EntitySaveException("Ошибка обновления BrandCar", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                BrandCar brandCar = session.get(BrandCar.class, id);
                if (brandCar != null) {
                    session.remove(brandCar);
                    log.info("BrandCar удалена: {}", brandCar);
                } else {
                    log.warn("BrandCar с id={} не найдена", id);
                    throw new EntityNotFoundException("BrandCar с id=" + id + " не найдена");
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления BrandCar по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления BrandCar по id", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();

            String updateHql = "DELETE FROM ModelCar";
            int updateResult = session.createQuery(updateHql).executeUpdate();
            log.info("Все {} BrandCar удалены", updateResult);

            String deleteHql = "DELETE FROM BrandCar";
            int deleteResult = session.createQuery(deleteHql).executeUpdate();
            transaction.commit();
            log.info("Все {} BrandCars удалены", deleteResult);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка удаления всех BrandCars:", e);
            throw new RepositoryException("Ошибка удаления всех BrandCars:", e);
        }
    }

    public List<ModelCar> getRelatedEntityByParentId(long id) {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            session.beginTransaction();
            String jpql = """
                       SELECT bcm FROM BrandCar bc JOIN bc.models bcm
                       WHERE bc.id = :id
                    """;

            Query<ModelCar> query = session.createQuery(jpql, ModelCar.class);
            query.setParameter("id", id);
            query.setMaxResults(5);
            session.getTransaction().commit();
            List<ModelCar> modelCars = query.list();
            log.info("{} Models для BrandCar получены", modelCars.size());
            return modelCars;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Models для BrandCar", e);
            throw new RepositoryException("Ошибка получения  Models для BrandCar", e);
        }
    }
}
