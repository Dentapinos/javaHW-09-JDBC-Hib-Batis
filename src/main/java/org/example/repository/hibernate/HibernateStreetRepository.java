package org.example.repository.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.House;
import org.example.entity.Street;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IStreetRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class HibernateStreetRepository implements IStreetRepository {

    public void save(Street street) throws EntitySaveException {
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

    public Street findById(long id) throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            Street loadedEntity = session.get(Street.class, id);
            if (loadedEntity == null) {
                log.warn("Street с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Street c id=" + id + " не найден");
            }
            log.info("Street найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Street по id={}", id, e);
            throw new RepositoryException("Ошибка получения Street по id=" + id, e);
        }
    }

    public List<Street> findAll() throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            List<Street> streets = session.createQuery("from Street", Street.class).list();
            log.info("Получены все Streets: {}", streets);
            return streets;
        } catch (Exception e) {
            log.error("Ошибка получения всех Streets:", e);
            throw new RepositoryException("Ошибка получения всех Streets:", e);
        }
    }

    public void update(Street street) throws EntityUpdateException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.merge(street);
            transaction.commit();
            log.info("Street обновлена: {}", street);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка обновления Street {}:", street, e);
            throw new EntitySaveException("Ошибка обновления Street", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                Street street = session.get(Street.class, id);
                if (street != null) {
                    session.remove(street);
                    log.info("Street удалена: {}", street);
                } else {
                    log.warn("Street с id={} не найдена", id);
                    throw new EntityNotFoundException("Street с id=" + id + " не найдена");
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Street по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Street по id", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();

            String updateHql = "UPDATE House h SET h.street = null WHERE h.street IS NOT NULL";
            int updateResult = session.createQuery(updateHql).executeUpdate();
            log.info("Обновлено {} записей House, установлено street_id=null", updateResult);

            String deleteHql = "DELETE FROM Street";
            int deleteResult = session.createQuery(deleteHql).executeUpdate();
            transaction.commit();
            log.info("Все {} Streets удалены", deleteResult);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка удаления всех Streets:", e);
            throw new RepositoryException("Ошибка удаления всех Streets:", e);
        }
    }

    public List<House> getRelatedEntityByParentId(long id) {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            session.beginTransaction();
            String jpql = """
                       SELECT h FROM Street s JOIN s.houses h
                       WHERE s.id = :id
                    """;

            Query<House> query = session.createQuery(jpql, House.class);
            query.setParameter("id", id);
            query.setMaxResults(5);
            session.getTransaction().commit();
            List<House> house = query.list();
            log.info("{} House для Street получены", house.size());
            return house;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения House для Street", e);
            throw new RepositoryException("Ошибка получения House для Street", e);
        }
    }
}
