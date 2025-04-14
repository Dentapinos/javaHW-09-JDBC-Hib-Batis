package org.example.repository.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.House;
import org.example.entity.Street;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IHouseRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class HibernateHouseRepository implements IHouseRepository {

    public void save(House street) throws EntitySaveException {
        if (street.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", street.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + street.getId());
        }
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                session.persist(street);
                transaction.commit();
                log.info("{} сохранен: {}", street.getClass().getSimpleName(), street);
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", street.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + street.getClass().getSimpleName(), e);
        }
    }

    public House findById(long id) throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            House loadedEntity = session.get(House.class, id);
            if (loadedEntity == null) {
                log.warn("House с таким id не найдено: {}", id);
                throw new EntityNotFoundException("House c id=" + id + " не найден");
            }
            log.info("House найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения House по id={}", id, e);
            throw new RepositoryException("Ошибка получения House по id=" + id, e);
        }
    }

    public List<House> findAll() throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            List<House> houses = session.createQuery("from House", House.class).list();
            log.info("Получены все Houses: {}", houses);
            return houses;
        } catch (Exception e) {
            log.error("Ошибка получения всех Houses:", e);
            throw new RepositoryException("Ошибка получения всех Houses:", e);
        }
    }

    public void update(House house) throws EntityUpdateException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.merge(house);
            transaction.commit();
            log.info("House обновлена: {}", house);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка обновления House {}:", house, e);
            throw new EntitySaveException("Ошибка обновления House", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                House street = session.get(House.class, id);
                if (street != null) {
                    session.remove(street);
                    log.info("House удалена: {}", street);
                } else {
                    log.warn("House с id={} не найдена", id);
                    throw new EntityNotFoundException("House с id=" + id + " не найдена");
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления House по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления House по id", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();

            String updateHql = "UPDATE House h SET h.street = null WHERE h.street IS NOT NULL";
            int updateResult = session.createQuery(updateHql).executeUpdate();
            log.info("Обновлено {} записей House, установлено street_id=null", updateResult);

            String deleteHql = "DELETE FROM House";
            int deleteResult = session.createQuery(deleteHql).executeUpdate();
            transaction.commit();
            log.info("Все {} Houses удалены", deleteResult);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка удаления всех Houses:", e);
            throw new RepositoryException("Ошибка удаления всех Houses:", e);
        }
    }

    public List<Street> getRelatedEntityByParentId(long id) {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            session.beginTransaction();
            String jpql = """
                       SELECT s FROM House h JOIN h.street s
                       WHERE h.id = :id
                    """;

            Query<Street> query = session.createQuery(jpql, Street.class);
            query.setParameter("id", id);
            query.setMaxResults(5);
            session.getTransaction().commit();
            List<Street> streets = query.list();
            log.info("{} Street для House получены", streets.size());
            return streets;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Street для House", e);
            throw new RepositoryException("Ошибка получения Street для House", e);
        }
    }
}
