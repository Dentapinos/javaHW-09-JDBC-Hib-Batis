package org.example.repository.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.Employee;
import org.example.entity.Task;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.ITaskRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class HibernateTaskRepository implements ITaskRepository {

    public void save(Task entity) throws EntitySaveException {
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            log.info("{} сохранен: {}", entity.getClass().getSimpleName(), entity);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public Task findById(long id) throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            Task loadedEntity = session.get(Task.class, id);
            if (loadedEntity == null) {
                log.warn("Task с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Task c id=" + id + " не найден");
            }
            log.info("Task найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Task по id={}", id, e);
            throw new RepositoryException("Ошибка получения Task по id=" + id, e);
        }
    }

    public List<Task> findAll() throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            List<Task> tasks = session.createQuery("from Task", Task.class).list();
            log.info("Получены все Tasks: {}", tasks);
            return tasks;
        } catch (Exception e) {
            log.error("Ошибка получения всех Tasks:", e);
            throw new RepositoryException("Ошибка получения всех Tasks:", e);
        }
    }

    public void update(Task task) throws EntityUpdateException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.merge(task);
            transaction.commit();
            log.info("Task обновлена: {}", task);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка обновления Task {}:", task, e);
            throw new EntitySaveException("Ошибка обновления Task", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                Task task = session.get(Task.class, id);
                if (task != null) {
                    session.remove(task);
                    log.info("Task удалена: {}", task);
                } else {
                    log.warn("Task с id={} не найдена", id);
                    throw new EntityNotFoundException("Task с id=" + id + " не найдена");
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Task по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Task по id", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            int deletedTasks = session.createQuery("delete from Task").executeUpdate();
            transaction.commit();
            log.info("Все {} Tasks удалены", deletedTasks);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка удаления всех Tasks:", e);
            throw new RepositoryException("Ошибка удаления всех Tasks:", e);
        }
    }

    public List<Employee> getRelatedEntityByParentId(long id) {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            session.beginTransaction();
            String jpql = """
                       SELECT e FROM Task t JOIN t.employee e
                       WHERE t.id = :id
                    """;

            Query<Employee> query = session.createQuery(jpql, Employee.class);
            query.setParameter("id", id);
            query.setMaxResults(5);
            session.getTransaction().commit();
            List<Employee> modelCars = query.list();
            log.info("Все Employee для Task получены");
            return modelCars;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Employee для Task", e);
            throw new RepositoryException("Ошибка получения Employee для Task", e);
        }
    }
}
