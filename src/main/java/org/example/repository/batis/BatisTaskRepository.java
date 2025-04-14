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
import org.example.repository.ITaskRepository;

import java.util.List;

@Slf4j
public class BatisTaskRepository implements ITaskRepository {

    public void save(Task entity) throws EntitySaveException {
        if (entity == null) {
            log.error("Ошибка сохранения Task null");
            throw new EntitySaveException("Ошибка сохранения Task null");
        }
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }

        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            EmployeeMapper emplMapper = session.getMapper(EmployeeMapper.class);

            if (entity.getEmployee() != null) {
                try {
                    emplMapper.save(entity.getEmployee());
                    log.info("Employee сохранен: {}", entity.getEmployee());
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "EmployeeMapper", e);
                }
            }
            try {
                mapper.save(entity);
                session.commit();
            } catch (Exception e) {
                rollBackWitchMapperException(session, "TaskMapper", e);
            }
            log.info("Task сохранен: {}", entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public Task findById(long id) throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            Task loadedEntity = null;
            try {
                loadedEntity = mapper.getById(id);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "TaskMapper", e);
            }
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
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            List<Task> tasks = mapper.getAll();
            log.info("Получены все Tasks: {}", tasks);
            return tasks;
        } catch (Exception e) {
            log.error("Ошибка получения всех Tasks:", e);
            throw new RepositoryException("Ошибка получения всех Tasks:", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);

            Task task = mapper.getById(id);
            if (task != null) {
                try {
                    mapper.deleteById(id);
                    session.commit();
                    log.info("Task удалена: {}", task);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "TaskMapper", e);
                }
            } else {
                log.warn("Task с id={} не найдена", id);
                throw new EntityNotFoundException("Task с id=" + id + " не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Task по id={} : {}", id, e.getMessage());
            throw new EntityDeleteException("Ошибка удаления Task по id", e);
        }
    }

    public void update(Task task) throws EntityUpdateException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            EmployeeMapper emplMapper = session.getMapper(EmployeeMapper.class);

            Task loadedTask = mapper.getById(task.getId());
            if (loadedTask != null) {
                try {
                    Employee employee = emplMapper.getById(task.getEmployee().getId());
                    if (employee != null) {
                        emplMapper.update(task.getEmployee());
                        log.info("Employee для Task обновлен: {}", task.getEmployee());
                    } else {
                        emplMapper.save(task.getEmployee());
                        log.info("Employee для Task сохранен: {}", task.getEmployee());
                    }
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "EmployeeException", e);
                }

                try {
                    mapper.update(task);
                    session.commit();
                    log.info("Task обновлена: {}", task);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "TaskMapper", e);
                }

            } else {
                log.warn("Task с таким id не найдено: {}", task.getId());
                throw new EntityNotFoundException("Task c id=" + task.getId() + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновления Task {}:", task, e);
            throw new EntitySaveException("Ошибка обновления Task", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            try {
                TaskMapper mapper = session.getMapper(TaskMapper.class);
                mapper.deleteAll();
                session.commit();
                log.info("Все Tasks удалены");
            } catch (Exception e) {
                rollBackWitchMapperException(session, "TaskMapper", e);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления всех Tasks:", e);
            throw new RepositoryException("Ошибка удаления всех Tasks:", e);
        }
    }

    public List<Employee> getRelatedEntityByParentId(long id) {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);

            try {
                Employee employee = mapper.getEmployeeByTaskId(id);
                if (employee != null) {
                    log.info("Employee для Task найден");
                    return List.of(employee);
                }
            } catch (Exception e) {
                rollBackWitchMapperException(session, "EmployeeMapper", e);
            }
            log.warn("Task с таким id не найдено: {}", id);
            throw new EntityNotFoundException("Task c id=" + id + " не найден");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Employee для Task: {}", e.getMessage());
            throw new RepositoryException("Ошибка получения Employee для Task", e);
        }
    }

    private void rollBackWitchMapperException(SqlSession session, String mapperName, Exception e) throws MapperException {
        session.rollback();
        log.error("Ошибка в {}: {}", mapperName, e.getMessage());
        throw new MapperException("Ошибка в " + mapperName + " :", e);
    }

}