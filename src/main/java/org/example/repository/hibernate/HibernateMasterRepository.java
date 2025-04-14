package org.example.repository.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IMasterRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class HibernateMasterRepository implements IMasterRepository {

    public void save(Master master) throws EntitySaveException {
        if (master.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", master.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + master.getId());
        }
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                session.persist(master);
                transaction.commit();
                log.info("{} сохранен: {}", master.getClass().getSimpleName(), master);
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", master.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + master.getClass().getSimpleName(), e);
        }
    }

    public Master findById(long id) throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            Master loadedEntity = session.get(Master.class, id);
            if (loadedEntity == null) {
                log.warn("Master с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Master c id=" + id + " не найден");
            }
            log.info("Master найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Master по id={}", id, e);
            throw new RepositoryException("Ошибка получения Master по id=" + id, e);
        }
    }

    public List<Master> findAll() throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            List<Master> masters = session.createQuery("from Master", Master.class).list();
            log.info("Получены все Masters: {}", masters);
            return masters;
        } catch (Exception e) {
            log.error("Ошибка получения всех Masters:", e);
            throw new RepositoryException("Ошибка получения всех Masters:", e);
        }
    }

    public void update(Master master) throws EntityUpdateException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.merge(master);
            transaction.commit();
            log.info("Master обновлена: {}", master);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка обновления Master {}:", master, e);
            throw new EntitySaveException("Ошибка обновления Master", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                Master master = session.get(Master.class, id);
                if (master != null) {
                    session.remove(master);
                    log.info("Master удалена: {}", master);
                } else {
                    log.warn("Master с id={} не найдена", id);
                    throw new EntityNotFoundException("Master с id=" + id + " не найдена");
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Master по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Master по id", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();

            String deleteHql = "DELETE FROM Master";
            int deleteResult = session.createQuery(deleteHql).executeUpdate();
            transaction.commit();
            log.info("Все {} Masters удалены", deleteResult);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка удаления всех Masters:", e);
            throw new RepositoryException("Ошибка удаления всех Masters:", e);
        }
    }

    public List<Kitty> getRelatedEntityByParentId(long id) {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            session.beginTransaction();
            String jpql = """
                       SELECT k FROM Master m JOIN m.kitties k
                       WHERE m.id = :id
                    """;

            Query<Kitty> query = session.createQuery(jpql, Kitty.class);
            query.setParameter("id", id);
            query.setMaxResults(5);
            session.getTransaction().commit();
            List<Kitty> kitties = query.list();
            log.info("{} Kitties для Master получены", kitties.size());
            return kitties;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Kitties для Master", e);
            throw new RepositoryException("Ошибка получения Kitties для Master", e);
        }
    }
}
