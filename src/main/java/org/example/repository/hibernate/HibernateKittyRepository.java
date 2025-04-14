package org.example.repository.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IKittyRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class HibernateKittyRepository implements IKittyRepository {

    public void save(Kitty kitty) throws EntitySaveException {
        if (kitty.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", kitty.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + kitty.getId());
        }
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                session.persist(kitty);
                transaction.commit();
                log.info("{} сохранен: {}", kitty.getClass().getSimpleName(), kitty);
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", kitty.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + kitty.getClass().getSimpleName(), e);
        }
    }

    public Kitty findById(long id) throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            Kitty loadedEntity = session.get(Kitty.class, id);
            if (loadedEntity == null) {
                log.warn("Kitty с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Kitty c id=" + id + " не найден");
            }
            log.info("Kitty найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Kitty по id={}", id, e);
            throw new RepositoryException("Ошибка получения Kitty по id=" + id, e);
        }
    }

    public List<Kitty> findAll() throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            List<Kitty> kitties = session.createQuery("from Kitty", Kitty.class).list();
            log.info("Получены все Kitties: {}", kitties);
            return kitties;
        } catch (Exception e) {
            log.error("Ошибка получения всех Kitties:", e);
            throw new RepositoryException("Ошибка получения всех Kitties:", e);
        }
    }

    public void update(Kitty kitty) throws EntityUpdateException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.merge(kitty);
            transaction.commit();
            log.info("Kitty обновлена: {}", kitty);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка обновления Kitty {}:", kitty, e);
            throw new EntitySaveException("Ошибка обновления Kitty", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                Kitty kitty = session.get(Kitty.class, id);
                if (kitty != null) {
                    session.remove(kitty);
                    log.info("Kitty удалена: {}", kitty);
                } else {
                    log.warn("Kitty с id={} не найдена", id);
                    throw new EntityNotFoundException("Kitty с id=" + id + " не найдена");
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Kitty по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Kitty по id", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();

            String deleteHql = "DELETE FROM Kitty";
            int deleteResult = session.createQuery(deleteHql).executeUpdate();
            transaction.commit();
            log.info("Все {} Kitties удалены", deleteResult);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка удаления всех Kitties:", e);
            throw new RepositoryException("Ошибка удаления всех Kitties:", e);
        }
    }

    public List<Master> getRelatedEntityByParentId(long id) {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            session.beginTransaction();
            String jpql = """
                       SELECT m FROM Kitty k JOIN k.masters m
                       WHERE k.id = :id
                    """;

            Query<Master> query = session.createQuery(jpql, Master.class);
            query.setParameter("id", id);
            query.setMaxResults(5);
            session.getTransaction().commit();
            List<Master> masters = query.list();
            log.info("{} Masters для Kitty получены", masters.size());
            return masters;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Masters для Kitty", e);
            throw new RepositoryException("Ошибка получения Masters для Kitty", e);
        }
    }
}
