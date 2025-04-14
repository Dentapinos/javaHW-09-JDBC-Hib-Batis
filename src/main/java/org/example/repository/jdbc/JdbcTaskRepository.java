package org.example.repository.jdbc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.Employee;
import org.example.entity.Task;
import org.example.enums.SessionName;
import org.example.enums.TypeTask;
import org.example.exception.*;
import org.example.repository.ITaskRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcTaskRepository implements ITaskRepository {

    public void save(Task entity) throws EntitySaveException {
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            if (entity.getEmployee() != null) {
                saveEmployee(connection, entity);
            }
            saveTask(connection, entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public Task findById(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT t.*, e.name as em_name, e.id as em_id, e.birth_date as em_date
                    FROM tasks t
                    LEFT JOIN employees e on t.employee_id = e.id
                    WHERE t.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    Task assembledTask = convertResultSetToTask(rs);
                    Employee assembledEmployee = null;

                    if (!rs.wasNull()) assembledEmployee = convertResultSetToEmployee(rs);

                    assembledTask = joinTaskWitchEmployee(assembledTask, assembledEmployee);

                    log.info("Tasks получена: {}", assembledTask);
                    return assembledTask;
                }
                log.warn("Task с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Task c id=" + id + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Task по id={}", id, e);
            throw new RepositoryException("Ошибка получения Task по id=" + id, e);
        }
    }

    public Task findByIdLazy(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT * FROM tasks WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    Task collectingTask = convertResultSetToTask(rs);

                    log.info("Tasks получена: {}", collectingTask);
                    return collectingTask;
                }
                log.warn("Task с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Task c id=" + id + " не найден");
            }
        } catch (Exception e) {
            log.error("Ошибка получения Task по id={}", id, e);
            throw new RepositoryException("Ошибка получения Task по id=" + id, e);
        }
    }

    public List<Task> findAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT t.*, e.name as em_name, e.id as em_id, e.birth_date as em_date
                    FROM tasks t
                    LEFT JOIN employees e on t.employee_id = e.id
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();

                List<Task> tasks = new ArrayList<>();

                while (rs.next()) {
                    Task collectingTask = convertResultSetToTask(rs);
                    Employee collectingEmployee = null;

                    if (!rs.wasNull()) collectingEmployee = convertResultSetToEmployee(rs);

                    collectingTask = joinTaskWitchEmployee(collectingTask, collectingEmployee);

                    if (collectingTask != null) tasks.add(collectingTask);
                }
                log.info("Получены все Tasks: {}", tasks);
                return tasks;
            }
        } catch (Exception e) {
            log.error("Ошибка получения всех Tasks:", e);
            throw new RepositoryException("Ошибка получения всех Tasks:", e);
        }
    }

    public void update(Task task) throws EntityUpdateException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            Task taskFromDB = findById(task.getId());

            if (task.getEmployee() != null) {
                String sql = "UPDATE employees SET name = ?, birth_date = ? WHERE id = ?";
                @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
                fillQueryEmployeeFields(task.getEmployee(), statement);
                statement.setLong(3, task.getEmployee().getId());

                statement.executeUpdate();
                log.info("{} обновлен: {}", task.getEmployee().getClass().getSimpleName(), task.getEmployee());

                if (taskFromDB.getEmployee().getId() != task.getEmployee().getId()) {
                    saveEmployee(connection, task);
                }
            }

            String sql = "UPDATE tasks SET deadline = ?, description = ?, name = ?, type = ?, employee_id = ? WHERE id = ?";
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            fillQueryTasksFields(task, statement);

            statement.setLong(6, task.getId());

            statement.executeUpdate();
            log.info("Task обновлена: {}", task);

        } catch (Exception e) {
            log.error("Ошибка обновления Task {}:", task, e);
            throw new EntitySaveException("Ошибка обновления Task", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
//          ищем в базе Task с таким id и если нет то выбрасываем исключение
            findByIdLazy(id);
            String sqlDelete = "DELETE FROM tasks WHERE id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete)) {
                deleteStatement.setLong(1, id);
                deleteStatement.executeUpdate();
                log.info("Task c id={} удалена", id);
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Task по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Task по id=" + id, e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sqlDelete = "DELETE FROM tasks";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.executeUpdate();
            log.info("все Task удалены");
        } catch (Exception e) {
            log.error("Ошибка удаления всех Tasks:", e);
            throw new RepositoryException("Ошибка удаления всех Tasks:", e);
        }
    }

    public List<Employee> getRelatedEntityByParentId(long id) {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT t.employee_id, e.name as em_name, e.id as em_id, e.birth_date as em_date
                    FROM tasks t
                    LEFT JOIN employees e on t.employee_id = e.id
                    WHERE t.id = ?
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            List<Employee> employees = new ArrayList<>();
            while (rs.next()) {
                Employee employee = convertResultSetToEmployee(rs);
                if (employee != null) employees.add(employee);
            }
            if (!employees.isEmpty()) {
                log.info("Employee для Task получен: {}", employees.size());
                return employees;
            }
            log.warn("Task с таким id не найдена: {}", id);
            throw new EntityNotFoundException("Task c id=" + id + " не найдена");
        } catch (Exception e) {
            log.error("Ошибка получения Employee по Task id={}", id, e);
            throw new RepositoryException("Ошибка получения Employee по Task id=" + id, e);
        }
    }

    /*
        Доп методы
     */

    private Task convertResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setName(rs.getString("name"));
        task.setDescription(rs.getString("description"));

        String typesTask = rs.getString("type");
        task.setType((typesTask != null) ? TypeTask.valueOf(rs.getString("type")) : null);

        Date sqlDate = rs.getDate("deadline");
        task.setDeadline((sqlDate != null) ? sqlDate.toLocalDate() : null);
        return task;
    }

    private Employee convertResultSetToEmployee(ResultSet rs) throws SQLException {
        long employeeId = rs.getLong("employee_id");
        String employeeName = rs.getString("em_name");
        Date empBirthDateSql = rs.getDate("em_date");
        return Employee.builder()
                .id(employeeId)
                .name(employeeName)
                .birthDate((empBirthDateSql != null) ? empBirthDateSql.toLocalDate() : null)
                .build();
    }

    private void fillQueryEmployeeFields(Employee employee, PreparedStatement statement) throws SQLException {
        statement.setString(1, employee.getName());
        if (employee.getBirthDate() != null) {
            statement.setDate(2, Date.valueOf(employee.getBirthDate()));
        } else {
            statement.setNull(2, Types.DATE);
        }
    }

    private void fillQueryTasksFields(Task entity, PreparedStatement statement) throws SQLException {
        if (entity.getDeadline() != null) {
            statement.setDate(1, Date.valueOf(entity.getDeadline()));
        } else {
            statement.setNull(1, Types.DATE);
        }
        statement.setString(2, entity.getDescription());
        statement.setString(3, entity.getName());
        if (entity.getType() != null) {
            statement.setString(4, entity.getType().name());
        } else {
            statement.setNull(4, Types.VARCHAR);
        }
        if (entity.getEmployee() != null) {
            statement.setLong(5, entity.getEmployee().getId());
        } else {
            statement.setNull(5, Types.BIGINT);
        }
    }

    private void saveTask(Connection connection, Task task) throws EntitySaveException, SQLException {
        String sql = "INSERT INTO tasks (deadline, description, name, type, employee_id) VALUES (?, ?, ?, ?, ?)";
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        fillQueryTasksFields(task, statement);
        statement.executeUpdate();
        log.info("{} сохранен: {}", task.getClass().getSimpleName(), task);

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                task.setId(generatedKeys.getLong(1));
                log.info("ID установлен на ID={}", task.getId());
            }
        } catch (Exception e) {
            log.warn("Ошибка генерации id");
            throw new GeneratedKeyException("Ошибка генерации id", e);
        }
    }

    private void saveEmployee(Connection connection, Task task) throws EntitySaveException {
        String sql = "INSERT INTO employees ( name, birth_date) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, task.getEmployee().getName());

            if (task.getEmployee().getBirthDate() != null) {
                statement.setDate(2, Date.valueOf(task.getEmployee().getBirthDate()));
            } else {
                statement.setNull(2, Types.DATE);
            }
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.getEmployee().setId(generatedKeys.getLong(1));
                }
            } catch (Exception e) {
                log.warn("Ошибка генерации id");
                throw new GeneratedKeyException("Ошибка генерации id", e);
            }
            log.info("{} сохранен: {}", task.getClass().getSimpleName(), task);
        } catch (Exception e) {
            log.warn("Ошибка сохранения Employee для Task");
            throw new EntitySaveException("Ошибка сохранения Employee для Task: ", e);
        }
    }

    private Task joinTaskWitchEmployee(Task task, Employee employee) {
        if (task != null) {
            if (employee != null) task.setEmployeeWithLinks(employee);
            log.info("Task найден: {}", task);
        }
        return task;
    }
}
