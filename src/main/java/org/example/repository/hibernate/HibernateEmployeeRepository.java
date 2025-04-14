package org.example.repository.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.Employee;
import org.example.entity.Task;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IEmployeeRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class HibernateEmployeeRepository implements IEmployeeRepository {

    public void save(Employee employee) throws EntitySaveException {
        if (employee.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", employee.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + employee.getId());
        }
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                session.persist(employee);
                transaction.commit();
                log.info("{} сохранен: {}", employee.getClass().getSimpleName(), employee);
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", employee.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + employee.getClass().getSimpleName(), e);
        }
    }

    public Employee findById(long id) throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            Employee loadedEntity = session.get(Employee.class, id);
            if (loadedEntity == null) {
                log.warn("Employee с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Employee c id=" + id + " не найден");
            }
            log.info("Employee найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Employee по id={}", id, e);
            throw new RepositoryException("Ошибка получения Employee по id=" + id, e);
        }
    }

    public List<Employee> findAll() throws RepositoryException {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            List<Employee> employees = session.createQuery("from Employee", Employee.class).list();
            log.info("Получены все Employees: {}", employees);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка получения всех Employees:", e);
            throw new RepositoryException("Ошибка получения всех Employees:", e);
        }
    }

    public void update(Employee employee) throws EntityUpdateException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();
            session.merge(employee);
            transaction.commit();
            log.info("Employee обновлена: {}", employee);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка обновления Employee {}:", employee, e);
            throw new EntitySaveException("Ошибка обновления Employee", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            try {
                transaction = session.beginTransaction();
                Employee employee = session.get(Employee.class, id);
                if (employee != null) {
                    session.remove(employee);
                    log.info("Employee удалена: {}", employee);
                } else {
                    log.warn("Employee с id={} не найдена", id);
                    throw new EntityNotFoundException("Employee с id=" + id + " не найдена");
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Employee по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Employee по id", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        Transaction transaction = null;
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            transaction = session.beginTransaction();

            String updateHql = "UPDATE Task t SET t.employee = null WHERE t.employee IS NOT NULL";
            int updateResult = session.createQuery(updateHql).executeUpdate();
            log.info("Обновлено {} записей Task, установлено employee_id=null", updateResult);

            String deleteHql = "DELETE FROM Employee";
            int deleteResult = session.createQuery(deleteHql).executeUpdate();
            transaction.commit();
            log.info("Все {} Employees удалены", deleteResult);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка удаления всех Employees:", e);
            throw new RepositoryException("Ошибка удаления всех Employees:", e);
        }
    }

    public List<Task> getRelatedEntityByParentId(long id) {
        try (Session session = (Session) SessionManager.createSession(SessionName.HIBERNATE.name())) {
            session.beginTransaction();
            String jpql = """
                       SELECT t FROM Employee e JOIN e.task t
                       WHERE e.id = :id
                    """;

            Query<Task> query = session.createQuery(jpql, Task.class);
            query.setParameter("id", id);
            query.setMaxResults(5);
            session.getTransaction().commit();
            List<Task> task = query.list();
            log.info("Все Task для Employee получены");
            return task;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Task для Employee", e);
            throw new RepositoryException("Ошибка получения Task для Employee", e);
        }
    }
}
