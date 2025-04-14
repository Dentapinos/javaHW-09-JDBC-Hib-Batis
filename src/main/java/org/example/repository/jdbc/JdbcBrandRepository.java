package org.example.repository.jdbc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.enums.SessionName;
import org.example.enums.TypeBody;
import org.example.exception.*;
import org.example.repository.IBrandRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcBrandRepository implements IBrandRepository {

    public void save(BrandCar brand) throws EntitySaveException {
        if (brand.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", brand.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + brand.getId());
        }
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            saveBrand(connection, brand);
            if (brand.getModels() != null) {
                for (ModelCar modelCar : brand.getModels()) {
                    saveModel(connection, modelCar);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", brand.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + brand.getClass().getSimpleName(), e);
        }
    }

    private void saveModel(Connection connection, ModelCar modelCar) throws EntitySaveException, SQLException {
        String sql = "INSERT INTO models_car (name, length, width, body, brand_id) VALUES (?, ?, ?, ?, ?)";
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        fillQueryModelsFields(modelCar, statement);
        statement.executeUpdate();
        log.info("{} сохранен: {}", modelCar.getClass().getSimpleName(), modelCar);

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                modelCar.setId(generatedKeys.getLong(1));
                log.info("ID установлен на ID={}", modelCar.getId());
            }
        } catch (Exception e) {
            log.warn("Ошибка генерации id");
            throw new GeneratedKeyException("Ошибка генерации id", e);
        }
    }

    private void saveBrand(Connection connection, BrandCar brandCar) throws EntitySaveException {
        String sql = "INSERT INTO brands_car ( name, date ) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, brandCar.getName());

            if (brandCar.getDateFoundation() != null) {
                statement.setDate(2, Date.valueOf(brandCar.getDateFoundation()));
            } else {
                statement.setNull(2, Types.DATE);
            }
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    brandCar.setId(generatedKeys.getLong(1));
                }
            } catch (Exception e) {
                log.warn("Ошибка генерации id");
                throw new GeneratedKeyException("Ошибка генерации id", e);
            }
            log.info("{} сохранен: {}", brandCar.getClass().getSimpleName(), brandCar);
        } catch (Exception e) {
            log.warn("Ошибка сохранения BrandCar для ModelCar");
            throw new EntitySaveException("Ошибка сохранения BrandCar для ModelCar: ", e);
        }
    }

    public BrandCar findById(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT mc.*, bc.name AS bc_name, bc.id AS bc_id, bc.date AS bc_date
                    FROM brands_car bc
                    LEFT JOIN models_car mc ON mc.brand_id = bc.id
                    WHERE bc.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                BrandCar assembledBrand = null;
                List<ModelCar> assembledModels = new ArrayList<>();

                while (rs.next()) {
                    if (assembledBrand == null) {
                        assembledBrand = convertResultSetToBrand(rs);
                    }

                    long modelId = rs.getLong("id");
                    if (!rs.wasNull()) {
                        ModelCar assembledModel = convertResultSetToModel(rs);
                        assembledModel.setId(modelId);
                        assembledModels.add(assembledModel);
                    }
                }

                if (assembledBrand != null) {
                    if (assembledModels.size() > 0) assembledBrand.setModelsWithLinks(assembledModels);
                    log.info("BrandsCar получена: {}", assembledBrand);
                    return assembledBrand;
                }
                log.warn("BrandCar с таким id не найдено: {}", id);
                throw new EntityNotFoundException("BrandCar c id=" + id + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения BrandCar по id={}", id, e);
            throw new RepositoryException("Ошибка получения BrandCar по id=" + id, e);
        }
    }

    public BrandCar findByIdLazy(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT * FROM brands_car WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    long brandId = rs.getLong("id");
                    String employeeName = rs.getString("name");
                    Date empBirthDateSql = rs.getDate("date");
                    BrandCar assembledBrand = BrandCar.builder()
                            .id(brandId)
                            .name(employeeName)
                            .dateFoundation((empBirthDateSql != null) ? empBirthDateSql.toLocalDate() : null)
                            .build();

                    log.info("BrandsCar получен: {}", assembledBrand);
                    return assembledBrand;
                }
                log.warn("BrandCar с таким id не найден: {}", id);
                throw new EntityNotFoundException("BrandCar c id=" + id + " не найден");
            }
        } catch (Exception e) {
            log.error("Ошибка получения BrandCar по id={}", id, e);
            throw new RepositoryException("Ошибка получения BrandCar по id=" + id, e);
        }
    }

    public List<BrandCar> findAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT mc.*, bc.name AS bc_name, bc.id AS bc_id, bc.date AS bc_date
                    FROM brands_car bc
                    LEFT JOIN models_car mc ON mc.brand_id = bc.id
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();

                List<BrandCar> resultBrandsCar = new ArrayList<>();

                List<ModelCar> currentListModels = new ArrayList<>();
                BrandCar lastBrand = null;

                while (rs.next()) {
                    BrandCar convertedBrand = convertResultSetToBrand(rs);
                    long modelId = rs.getLong("id");
                    ModelCar convertedModel = null;
                    if (!rs.wasNull()) convertedModel = convertResultSetToModel(rs);

                    if (lastBrand == null) {
                        lastBrand = convertedBrand;
                    } else {
                        if (lastBrand.getId() != convertedBrand.getId()) {
                            if (!currentListModels.isEmpty()) lastBrand.setModelsWithLinks(currentListModels);
                            resultBrandsCar.add(lastBrand);

                            lastBrand = convertedBrand;
                            currentListModels = new ArrayList<>();
                        }
                    }
                    if (convertedModel != null) {
                        convertedModel.setId(modelId);
                        currentListModels.add(convertedModel);
                    }
                }
                log.info("Получены все BrandsCar: {}", resultBrandsCar);
                return resultBrandsCar;
            }
        } catch (Exception e) {
            log.error("Ошибка получения всех BrandsCar:", e);
            throw new RepositoryException("Ошибка получения всех BrandsCar:", e);
        }
    }

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
        long id = rs.getLong("bc_id");
        String employeeName = rs.getString("bc_name");
        Date empBirthDateSql = rs.getDate("bc_date");
        return BrandCar.builder()
                .id(id)
                .name(employeeName)
                .dateFoundation((empBirthDateSql != null) ? empBirthDateSql.toLocalDate() : null)
                .build();
    }

    public void update(BrandCar brand) throws EntityUpdateException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = "UPDATE brands_car SET name = ?, date = ? WHERE id = ?";
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            fillQueryBrandsFields(brand, statement);
            statement.setLong(3, brand.getId());

            statement.executeUpdate();
            log.info("{} обновлен: {}", brand.getClass().getSimpleName(), brand);

            if (brand.getModels() != null) {
                List<ModelCar> loadedModels = findById(brand.getId()).getModels();
                for (ModelCar modelCar : loadedModels) {
                    cleanLinkInBDForBrandById(connection, modelCar.getBrand().getId());
                }
                for (ModelCar modelCar : brand.getModels()) {
                    if (modelCar.getId() == 0) {
                        saveModel(connection, modelCar);
                    } else {
                        String sqlUpdate = "UPDATE models_car SET name = ?, length = ?, width = ?, body = ?, brand_id = ? WHERE id = ?";
                        @Cleanup PreparedStatement statementUpdate = connection.prepareStatement(sqlUpdate);
                        fillQueryModelsFields(modelCar, statementUpdate);
                        statementUpdate.setLong(6, modelCar.getId());

                        statementUpdate.executeUpdate();
                    }
                }
                log.info("ModelsCar обновлены: {}", brand.getModels().size());
            }

        } catch (Exception e) {
            log.error("Ошибка обновления BrandCar {}:", brand, e);
            throw new EntitySaveException("Ошибка обновления BrandCar", e);
        }
    }

    private void fillQueryBrandsFields(BrandCar brandCar, PreparedStatement statement) throws SQLException {
        statement.setString(1, brandCar.getName());
        if (brandCar.getDateFoundation() != null) {
            statement.setDate(2, Date.valueOf(brandCar.getDateFoundation()));
        } else {
            statement.setNull(2, Types.DATE);
        }
    }


    private void fillQueryModelsFields(ModelCar entity, PreparedStatement statement) throws SQLException {
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

    public void delete(long id) throws EntityNotFoundException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            findByIdLazy(id);
            cleanLinkInBDForBrandById(connection, id);
            log.info("Связь BrandCar c Task очищена");

            String sqlDelete = "DELETE FROM brands_car WHERE id = ?";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.setLong(1, id);
            deleteStatement.executeUpdate();
            log.info("BrandCar c id={} удалена", id);
        } catch (Exception e) {
            log.error("Ошибка удаления BrandCar по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления BrandCar по id=" + id, e);
        }
    }

    private void cleanLinkInBDForBrandById(Connection connection, long id) throws SQLException {
        String sql = """
                UPDATE models_car mc
                SET mc.brand_id = NULL
                WHERE mc.brand_id = ?;
                """;
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, id);
        statement.executeUpdate();
    }

    public void deleteAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    UPDATE models_car mc
                    SET mc.brand_id = NULL
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            log.info("Все связи BrandCar c Task очищены");

            String sqlDelete = "DELETE FROM brands_car";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.executeUpdate();
            log.info("все BrandCar удалены");
        } catch (Exception e) {
            log.error("Ошибка удаления всех BrandsCar:", e);
            throw new RepositoryException("Ошибка удаления всех BrandsCar:", e);
        }
    }

    public List<ModelCar> getRelatedEntityByParentId(long id) {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT mc.*
                    FROM brands_car bc
                    LEFT JOIN models_car mc on mc.brand_id = bc.id
                    WHERE bc.id = ?
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            List<ModelCar> modelCars = new ArrayList<>();
            while (rs.next()) {
                ModelCar modelCar = convertResultSetToModel(rs);
                if (modelCar.getId() > 0) modelCars.add(modelCar);
            }
            if (!modelCars.isEmpty()) {
                log.info("Task для BrandCar получен: {}", modelCars.size());
                return modelCars;
            }
            log.warn("BrandCar с таким id не найдена: {}", id);
            throw new EntityNotFoundException("BrandCar c id=" + id + " не найдена");
        } catch (Exception e) {
            log.error("Ошибка получения Task по BrandCar id={}", id, e);
            throw new RepositoryException("Ошибка получения Task по BrandCar id=" + id, e);
        }
    }
}
