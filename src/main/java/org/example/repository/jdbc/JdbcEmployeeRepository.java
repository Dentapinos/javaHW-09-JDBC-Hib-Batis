package org.example.repository.jdbc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.Employee;
import org.example.entity.Task;
import org.example.enums.SessionName;
import org.example.enums.TypeTask;
import org.example.exception.*;
import org.example.repository.IEmployeeRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcEmployeeRepository implements IEmployeeRepository {

    public void save(Employee entity) throws EntitySaveException {
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {

            saveEmployee(connection, entity);

            if (entity.getTask() != null) {
                saveTask(connection, entity.getTask());
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
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

    private void saveEmployee(Connection connection, Employee entity) throws EntitySaveException {
        String sql = "INSERT INTO employees ( name, birth_date) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());

            if (entity.getBirthDate() != null) {
                statement.setDate(2, Date.valueOf(entity.getBirthDate()));
            } else {
                statement.setNull(2, Types.DATE);
            }
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            } catch (Exception e) {
                log.warn("Ошибка генерации id");
                throw new GeneratedKeyException("Ошибка генерации id", e);
            }
            log.info("{} сохранен: {}", entity.getClass().getSimpleName(), entity);
        } catch (Exception e) {
            log.warn("Ошибка сохранения Employee для Task");
            throw new EntitySaveException("Ошибка сохранения Employee для Task: ", e);
        }
    }

    public Employee findById(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT t.*, e.name as em_name, e.id as em_id, e.birth_date as em_date
                    FROM employees e
                    LEFT JOIN tasks t on t.employee_id = e.id
                    WHERE e.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    Employee assembledEmployee = convertResultSetToEmployee(rs);

                    Task collectedTask = null;

                    if (!rs.wasNull()) collectedTask = convertResultSetToTask(rs);

                    assembledEmployee = joinEmployeeWitchTask(assembledEmployee, collectedTask);

                    log.info("Employees получена: {}", assembledEmployee);
                    return assembledEmployee;
                }
                log.warn("Employee с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Employee c id=" + id + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Employee по id={}", id, e);
            throw new RepositoryException("Ошибка получения Employee по id=" + id, e);
        }
    }

    public Employee findByIdLazy(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT * FROM employees WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    long empId = rs.getLong("id");
                    String employeeName = rs.getString("name");
                    Date empBirthDateSql = rs.getDate("birth_date");
                    Employee assembledEmployee = Employee.builder()
                            .id(empId)
                            .name(employeeName)
                            .birthDate((empBirthDateSql != null) ? empBirthDateSql.toLocalDate() : null)
                            .build();

                    log.info("Employees получена: {}", assembledEmployee);
                    return assembledEmployee;
                }
                log.warn("Employee с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Employee c id=" + id + " не найден");
            }
        } catch (Exception e) {
            log.error("Ошибка получения Employee по id={}", id, e);
            throw new RepositoryException("Ошибка получения Employee по id=" + id, e);
        }
    }

    public List<Employee> findAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT t.*, e.name as em_name, e.id as em_id, e.birth_date as em_date
                    FROM employees e
                    LEFT JOIN tasks t on t.employee_id = e.id
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();

                List<Employee> employees = new ArrayList<>();

                while (rs.next()) {
                    Employee assembledEmployee = convertResultSetToEmployee(rs);
                    Task assembledTask = null;

                    if (!rs.wasNull()) assembledTask = convertResultSetToTask(rs);

                    assembledEmployee = joinEmployeeWitchTask(assembledEmployee, assembledTask);

                    if (assembledEmployee != null) employees.add(assembledEmployee);
                }
                log.info("Получены все Employees: {}", employees);
                return employees;
            }
        } catch (Exception e) {
            log.error("Ошибка получения всех Employees:", e);
            throw new RepositoryException("Ошибка получения всех Employees:", e);
        }
    }

    private Employee joinEmployeeWitchTask(Employee employee, Task task) {
        if (employee != null) {
            if (task != null) employee.setTaskWithLink(task);
            log.info("Employee найден: {}", employee);
        }
        return employee;
    }

    private Task convertResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setName(rs.getString("name"));
        task.setDescription(rs.getString("description"));

        String typesTask = rs.getString("type");
        task.setType((typesTask != null) ? TypeTask.valueOf(rs.getString("type")) : null);

        Date sqlDate = rs.getDate("deadline");
        task.setDeadline((sqlDate != null) ? sqlDate.toLocalDate() : null);
        if (task.getId() != 0) return task;
        return null;
    }

    private Employee convertResultSetToEmployee(ResultSet rs) throws SQLException {
        long id = rs.getLong("em_id");
        String employeeName = rs.getString("em_name");
        Date empBirthDateSql = rs.getDate("em_date");
        return Employee.builder()
                .id(id)
                .name(employeeName)
                .birthDate((empBirthDateSql != null) ? empBirthDateSql.toLocalDate() : null)
                .build();
    }

    public void update(Employee employee) throws EntityUpdateException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = "UPDATE employees SET name = ?, birth_date = ? WHERE id = ?";
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            fillQueryEmployeeFields(employee, statement);
            statement.setLong(3, employee.getId());

            statement.executeUpdate();

            log.info("{} обновлен: {}", employee.getClass().getSimpleName(), employee);

            if (employee.getTask() != null) {
                String sqlTask = "UPDATE tasks SET deadline = ?, description = ?, name = ?, type = ?, employee_id = ? WHERE id = ?";
                @Cleanup PreparedStatement statementTask = connection.prepareStatement(sqlTask);
                fillQueryTasksFields(employee.getTask(), statementTask);
                statementTask.setLong(6, employee.getTask().getId());
                statementTask.executeUpdate();
                log.info("Task обновлена: {}", employee.getTask());
            }
        } catch (Exception e) {
            log.error("Ошибка обновления Employee {}:", employee, e);
            throw new EntitySaveException("Ошибка обновления Employee", e);
        }
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

    public void delete(long id) throws EntityNotFoundException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    UPDATE tasks t
                    SET t.employee_id = NULL
                    WHERE t.employee_id = (
                        SELECT e.id
                        FROM employees e
                        WHERE e.id = ?
                    );
                    """;
            findByIdLazy(id);
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            statement.executeUpdate();
            log.info("Связь Employee c Task очищена");

            String sqlDelete = "DELETE FROM employees WHERE id = ?";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.setLong(1, id);
            deleteStatement.executeUpdate();
            log.info("Employee c id={} удалена", id);
        } catch (Exception e) {
            log.error("Ошибка удаления Employee по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Employee по id=" + id, e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    UPDATE tasks t
                    SET t.employee_id = NULL
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            log.info("Все связи Employee c Task очищены");

            String sqlDelete = "DELETE FROM employees";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.executeUpdate();
            log.info("все Employee удалены");
        } catch (Exception e) {
            log.error("Ошибка удаления всех Employees:", e);
            throw new RepositoryException("Ошибка удаления всех Employees:", e);
        }
    }

    public List<Task> getRelatedEntityByParentId(long id) {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT t.*
                    FROM employees e
                    LEFT JOIN tasks t on t.employee_id = e.id
                    WHERE e.id = ?
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            List<Task> taskList = new ArrayList<>();
            while (rs.next()) {
                Task task = convertResultSetToTask(rs);
                if (task != null) taskList.add(task);
            }
            if (!taskList.isEmpty()) {
                log.info("Task для Employee получен: {}", taskList.size());
                return taskList;
            }
            log.warn("Employee с таким id не найдена: {}", id);
            throw new EntityNotFoundException("Employee c id=" + id + " не найдена");
        } catch (Exception e) {
            log.error("Ошибка получения Task по Employee id={}", id, e);
            throw new RepositoryException("Ошибка получения Task по Employee id=" + id, e);
        }
    }
}
