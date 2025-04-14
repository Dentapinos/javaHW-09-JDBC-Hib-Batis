package org.example.repository.jdbc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.enums.SessionName;
import org.example.enums.TypeBody;
import org.example.exception.*;
import org.example.repository.IModelRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcModelRepository implements IModelRepository {

    public void save(ModelCar entity) throws EntitySaveException {
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            if (entity.getBrand() != null) {
                saveBrand(connection, entity);
            }
            saveModel(connection, entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public ModelCar findById(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT mc.*, bc.name as bc_name, bc.id as bc_id, bc.date as bc_date
                    FROM models_car mc
                    LEFT JOIN brands_car bc on mc.brand_id = bc.id
                    WHERE mc.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    ModelCar assembledModel = convertResultSetToModel(rs);
                    BrandCar assembledBrand = null;

                    if (!rs.wasNull()) assembledBrand = convertResultSetToBrand(rs);

                    assembledModel = joinModelWitchBrand(assembledModel, assembledBrand);

                    log.info("ModelsCar получена: {}", assembledModel);
                    return assembledModel;
                }
                log.warn("ModelCar с таким id не найдено: {}", id);
                throw new EntityNotFoundException("ModelCar c id=" + id + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения ModelCar по id={}", id, e);
            throw new RepositoryException("Ошибка получения ModelCar по id=" + id, e);
        }
    }

    public ModelCar findByIdLazy(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT * FROM models_car WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    ModelCar collectingTask = convertResultSetToModel(rs);

                    log.info("ModelsCar получена: {}", collectingTask);
                    return collectingTask;
                }
                log.warn("ModelCar с таким id не найдено: {}", id);
                throw new EntityNotFoundException("ModelCar c id=" + id + " не найден");
            }
        } catch (Exception e) {
            log.error("Ошибка получения ModelCar по id={}", id, e);
            throw new RepositoryException("Ошибка получения ModelCar по id=" + id, e);
        }
    }

    public List<ModelCar> findAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT mc.*, bc.name as bc_name, bc.id as bc_id, bc.date as bc_date
                    FROM models_car mc
                    LEFT JOIN brands_car bc on mc.brand_id = bc.id
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();

                List<ModelCar> modelsCar = new ArrayList<>();

                while (rs.next()) {
                    ModelCar assembledModel = convertResultSetToModel(rs);
                    BrandCar assembledBrand = null;

                    if (!rs.wasNull()) assembledBrand = convertResultSetToBrand(rs);

                    assembledModel = joinModelWitchBrand(assembledModel, assembledBrand);

                    if (assembledModel != null) modelsCar.add(assembledModel);
                }
                log.info("Получены все ModelsCar: {}", modelsCar);
                return modelsCar;
            }
        } catch (Exception e) {
            log.error("Ошибка получения всех ModelsCar:", e);
            throw new RepositoryException("Ошибка получения всех ModelsCar:", e);
        }
    }

    public void update(ModelCar model) throws EntityUpdateException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            ModelCar taskFromDB = findById(model.getId());

            if (model.getBrand() != null) {
                String sql = "UPDATE brands_car SET name = ?, date = ? WHERE id = ?";
                @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
                fillQueryBrandsFields(model.getBrand(), statement);
                statement.setLong(3, model.getBrand().getId());

                statement.executeUpdate();
                log.info("{} обновлен: {}", model.getBrand().getClass().getSimpleName(), model.getBrand());

                if (taskFromDB.getBrand().getId() != model.getBrand().getId()) {
                    saveBrand(connection, model);
                }
            }

            String sql = "UPDATE models_car SET name = ?, length = ?, width = ?, body = ?, brand_id = ? WHERE id = ?";
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            fillQueryModelFields(model, statement);
            statement.setLong(6, model.getId());

            statement.executeUpdate();
            log.info("ModelCar обновлена: {}", model);

        } catch (Exception e) {
            log.error("Ошибка обновления ModelCar {}:", model, e);
            throw new EntitySaveException("Ошибка обновления ModelCar", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
//          ищем в базе ModelCar с таким id и если нет то выбрасываем исключение
            findByIdLazy(id);
            String sqlDelete = "DELETE FROM models_car WHERE id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete)) {
                deleteStatement.setLong(1, id);
                deleteStatement.executeUpdate();
                log.info("ModelCar c id={} удалена", id);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления ModelCar по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления ModelCar по id=" + id, e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sqlDelete = "DELETE FROM models_car";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.executeUpdate();
            log.info("Все ModelCar удалены");
        } catch (Exception e) {
            log.error("Ошибка удаления всех ModelsCar:", e);
            throw new RepositoryException("Ошибка удаления всех ModelsCar:", e);
        }
    }

    public List<BrandCar> getRelatedEntityByParentId(long id) {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT bc.name as bc_name, bc.id as bc_id, bc.date as bc_date
                    FROM models_car mc
                    LEFT JOIN brands_car bc on mc.brand_id = bc.id
                    WHERE mc.id = ?
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            List<BrandCar> employees = new ArrayList<>();
            while (rs.next()) {
                BrandCar brandCar = convertResultSetToBrand(rs);
                if (brandCar != null) employees.add(brandCar);
            }
            if (!employees.isEmpty()) {
                log.info("BrandCar для ModelCar получен: {}", employees.size());
                return employees;
            }
            log.warn("ModelCar с таким id не найдена: {}", id);
            throw new EntityNotFoundException("ModelCar c id=" + id + " не найдена");
        } catch (Exception e) {
            log.error("Ошибка получения BrandCar по ModelCar id={}", id, e);
            throw new RepositoryException("Ошибка получения BrandCar по ModelCar id=" + id, e);
        }
    }

    /*
        Доп методы
     */

    private ModelCar convertResultSetToModel(ResultSet rs) throws SQLException {
        ModelCar model = new ModelCar();
        model.setId(rs.getLong("id"));
        model.setName(rs.getString("name"));
        model.setLength(rs.getInt("length"));
        model.setWidth(rs.getInt("width"));

        String typeBody = rs.getString("body");
        model.setBody((typeBody != null) ? TypeBody.valueOf(typeBody) : null);
        return model;
    }

    private BrandCar convertResultSetToBrand(ResultSet rs) throws SQLException {
        long brandId = rs.getLong("bc_id");
        String employeeName = rs.getString("bc_name");
        Date empBirthDateSql = rs.getDate("bc_date");
        return BrandCar.builder()
                .id(brandId)
                .name(employeeName)
                .dateFoundation((empBirthDateSql != null) ? empBirthDateSql.toLocalDate() : null)
                .build();
    }

    private void fillQueryBrandsFields(BrandCar brandCar, PreparedStatement statement) throws SQLException {
        statement.setString(1, brandCar.getName());
        if (brandCar.getDateFoundation() != null) {
            statement.setDate(2, Date.valueOf(brandCar.getDateFoundation()));
        } else {
            statement.setNull(2, Types.DATE);
        }
    }

    private void fillQueryModelFields(ModelCar entity, PreparedStatement statement) throws SQLException {
        statement.setString(1, entity.getName());
        statement.setInt(2, entity.getLength());
        statement.setInt(3, entity.getWidth());

        if (entity.getBody() != null) {
            statement.setString(4, entity.getBody().name());
        } else {
            statement.setNull(4, Types.VARCHAR);
        }
        if (entity.getBrand() != null) {
            statement.setLong(5, entity.getBrand().getId());
        } else {
            statement.setNull(5, Types.BIGINT);
        }
    }

    private void saveModel(Connection connection, ModelCar model) throws EntitySaveException, SQLException {
        String sql = "INSERT INTO models_car (name, length, width, body, brand_id) VALUES (?, ?, ?, ?, ?)";
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        fillQueryModelFields(model, statement);
        statement.executeUpdate();
        log.info("{} сохранен: {}", model.getClass().getSimpleName(), model);

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                model.setId(generatedKeys.getLong(1));
                log.info("ID установлен на ID={}", model.getId());
            }
        } catch (Exception e) {
            log.warn("Ошибка генерации id");
            throw new GeneratedKeyException("Ошибка генерации id", e);
        }
    }

    private void saveBrand(Connection connection, ModelCar model) throws EntitySaveException {
        String sql = "INSERT INTO brands_car ( name, date ) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, model.getBrand().getName());

            if (model.getBrand().getDateFoundation() != null) {
                statement.setDate(2, Date.valueOf(model.getBrand().getDateFoundation()));
            } else {
                statement.setNull(2, Types.DATE);
            }
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    model.getBrand().setId(generatedKeys.getLong(1));
                }
            } catch (Exception e) {
                log.warn("Ошибка генерации id");
                throw new GeneratedKeyException("Ошибка генерации id", e);
            }
            log.info("{} сохранен: {}", model.getClass().getSimpleName(), model);
        } catch (Exception e) {
            log.warn("Ошибка сохранения BrandCar для ModelCar");
            throw new EntitySaveException("Ошибка сохранения BrandCar для ModelCar: ", e);
        }
    }

    private ModelCar joinModelWitchBrand(ModelCar model, BrandCar brandCar) {
        if (model != null) {
            if (brandCar != null) model.setBrandWithLinks(brandCar);
            log.info("ModelCar найден: {}", model);
        }
        return model;
    }
}
