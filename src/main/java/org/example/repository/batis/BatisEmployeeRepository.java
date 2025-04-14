package org.example.repository.batis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.example.configs.SessionManager;
import org.example.entity.Employee;
import org.example.entity.Task;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.mappers.EmployeeMapper;
import org.example.mappers.TaskMapper;
import org.example.repository.IEmployeeRepository;

import java.util.List;

@Slf4j
public class BatisEmployeeRepository implements IEmployeeRepository {

    public void save(Employee entity) throws EntitySaveException {
        if (entity == null) {
            log.error("Ошибка сохранения Employee null");
            throw new EntitySaveException("Ошибка сохранения Employee null");
        }
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }

        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            EmployeeMapper emplMapper = session.getMapper(EmployeeMapper.class);

            try {
                emplMapper.save(entity);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "Employee", e);
            }

            if (entity.getTask() != null) {
                try {
                    Task task = mapper.getById(entity.getTask().getId());
                    if (task != null) {
                        mapper.update(entity.getTask());
                        log.info("Task для Employee обновлен: {}", task);
                    } else {
                        mapper.save(entity.getTask());
                        log.info("Task для Employee сохранен: {}", entity.getTask());
                    }
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "TaskMapper", e);
                }
            }


            session.commit();
            log.info("Task сохранен: {}", entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public Employee findById(long id) throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee loadedEntity = null;
            try {
                loadedEntity = mapper.getById(id);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "EmployeeMapper", e);
            }
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
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            List<Employee> employees = mapper.getAll();
            log.info("Получены все Employees: {}", employees);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка получения всех Employees:", e);
            throw new RepositoryException("Ошибка получения всех Employees:", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            TaskMapper taskMapper = session.getMapper(TaskMapper.class);

            Employee employee = mapper.getById(id);
            if (employee != null) {

                if (employee.getTask() != null) {
                    try {
                        employee.getTask().setEmployee(null);
                        taskMapper.update(employee.getTask());
                        log.info("Связь с Task очищена");
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "EmployeeMapper", e);
                    }
                }

                try {
                    mapper.deleteById(id);
                    session.commit();
                    log.info("Employee удалена: {}", employee);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "EmployeeMapper", e);
                }
            } else {
                log.warn("Employee с id={} не найдена", id);
                throw new EntityNotFoundException("Employee с id=" + id + " не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Employee по id={} : {}", id, e.getMessage());
            throw new EntityDeleteException("Ошибка удаления Employee по id", e);
        }
    }

    public void update(Employee employee) throws EntityUpdateException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            EmployeeMapper emplMapper = session.getMapper(EmployeeMapper.class);

            Employee loadedEmployee = emplMapper.getById(employee.getId());
            if (loadedEmployee != null) {
                try {
                    emplMapper.update(employee);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "EmployeeMapper", e);
                }

                try {
                    Task task = mapper.getById(employee.getTask().getId());
                    if (task != null) {
                        mapper.update(employee.getTask());
                        log.info("Task для Employee обновлен: {}", employee.getTask());
                    } else {
                        mapper.save(employee.getTask());
                        log.info("Task для Employee сохранен: {}", employee.getTask());
                    }
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "TaskMapper", e);
                }
                session.commit();
                log.info("Employee обновлена: {}", employee);
            } else {
                log.warn("Employee с таким id не найдено: {}", employee.getId());
                throw new EntityNotFoundException("Employee c id=" + employee.getId() + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновления Employee {}:", employee, e);
            throw new EntitySaveException("Ошибка обновления Employee", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            try {
                EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
                mapper.deleteAllRelation();
                mapper.deleteAll();
                session.commit();
                log.info("Все Employees удалены");
            } catch (Exception e) {
                rollBackWitchMapperException(session, "EmployeeMapper", e);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления всех Employees:", e);
            throw new RepositoryException("Ошибка удаления всех Employees:", e);
        }
    }

    public List<Task> getRelatedEntityByParentId(long id) {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);

            try {
                Task task = mapper.getTaskByEmployeeId(id);
                if (task != null) {
                    log.info("Task для Task найден");
                    return List.of(task);
                }
            } catch (Exception e) {
                rollBackWitchMapperException(session, "TaskMapper", e);
            }
            log.warn("Employee с таким id не найдено: {}", id);
            throw new EntityNotFoundException("Employee c id=" + id + " не найден");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Task для Employee: {}", e.getMessage());
            throw new RepositoryException("Ошибка получения Task для Employee", e);
        }
    }

    private void rollBackWitchMapperException(SqlSession session, String mapperName, Exception e) throws MapperException {
        session.rollback();
        log.error("Ошибка в {}: {}", mapperName, e.getMessage());
        throw new MapperException("Ошибка в " + mapperName + " :", e);
    }

}