package org.example.repository.jdbc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.House;
import org.example.entity.Street;
import org.example.enums.SessionName;
import org.example.enums.TypeBuilding;
import org.example.exception.*;
import org.example.repository.IHouseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcHouseRepository implements IHouseRepository {

    public void save(House house) throws EntitySaveException {
        if (house.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", house.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + house.getId());
        }
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            if (house.getStreet() != null) {
                saveStreet(connection, house);
            }
            saveHouse(connection, house);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", house.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + house.getClass().getSimpleName(), e);
        }
    }

    public House findById(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT h.*, s.name AS s_name, s.id AS s_id, s.postcode
                    FROM houses h
                    LEFT JOIN streets s ON h.street_id = s.id
                    WHERE h.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    House assembledHouse = convertResultSetToHouse(rs);
                    Street assembledStreet = null;

                    if (!rs.wasNull()) assembledStreet = convertResultSetToStreet(rs);

                    assembledHouse = joinHouseWitchStreet(assembledHouse, assembledStreet);

                    log.info("Houses получена: {}", assembledHouse);
                    return assembledHouse;
                }
                log.warn("House с таким id не найдено: {}", id);
                throw new EntityNotFoundException("House c id=" + id + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения House по id={}", id, e);
            throw new RepositoryException("Ошибка получения House по id=" + id, e);
        }
    }

    public House findByIdLazy(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT * FROM houses WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    House assembledTask = convertResultSetToHouse(rs);

                    log.info("Houses получена: {}", assembledTask);
                    return assembledTask;
                }
                log.warn("House с таким id не найдено: {}", id);
                throw new EntityNotFoundException("House c id=" + id + " не найден");
            }
        } catch (Exception e) {
            log.error("Ошибка получения House по id={}", id, e);
            throw new RepositoryException("Ошибка получения House по id=" + id, e);
        }
    }

    public List<House> findAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT h.*, s.name AS s_name, s.id AS s_id, s.postcode
                    FROM houses h
                    LEFT JOIN streets s ON h.street_id = s.id
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();

                List<House> houses = new ArrayList<>();

                while (rs.next()) {
                    House assembledHouse = convertResultSetToHouse(rs);
                    Street assembledStreet = null;

                    if (!rs.wasNull()) assembledStreet = convertResultSetToStreet(rs);

                    assembledHouse = joinHouseWitchStreet(assembledHouse, assembledStreet);

                    if (assembledHouse != null) houses.add(assembledHouse);
                }
                log.info("Получены все Houses: {}", houses);
                return houses;
            }
        } catch (Exception e) {
            log.error("Ошибка получения всех Houses:", e);
            throw new RepositoryException("Ошибка получения всех Houses:", e);
        }
    }

    public void update(House house) throws EntityUpdateException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            House houseFromDB = findById(house.getId());

            if (house.getStreet() != null) {
                String sql = "UPDATE streets SET name = ?, postcode = ? WHERE id = ?";
                @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
                fillQueryStreetsFields(house, statement);
                statement.setLong(3, house.getStreet().getId());

                statement.executeUpdate();
                log.info("{} обновлен: {}", house.getStreet().getClass().getSimpleName(), house.getStreet());

                if (houseFromDB.getStreet().getId() != house.getStreet().getId()) {
                    saveStreet(connection, house);
                }
            }

            String sql = "UPDATE houses SET name = ?, date_building = ?, floors = ?, type = ?, street_id = ?  WHERE id = ?";
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            fillQueryHouseFields(house, statement);
            statement.setLong(6, house.getId());

            statement.executeUpdate();
            log.info("House обновлена: {}", house);

        } catch (Exception e) {
            log.error("Ошибка обновления House {}:", house, e);
            throw new EntitySaveException("Ошибка обновления House", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
//          ищем в базе House с таким id и если нет то выбрасываем исключение
            findByIdLazy(id);
            String sqlDelete = "DELETE FROM houses WHERE id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete)) {
                deleteStatement.setLong(1, id);
                deleteStatement.executeUpdate();
                log.info("House c id={} удалена", id);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления House по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления House по id=" + id, e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sqlDelete = "DELETE FROM houses";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.executeUpdate();
            log.info("Все House удалены");
        } catch (Exception e) {
            log.error("Ошибка удаления всех Houses:", e);
            throw new RepositoryException("Ошибка удаления всех Houses:", e);
        }
    }

    public List<Street> getRelatedEntityByParentId(long id) {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT s.name AS s_name, s.id AS s_id, s.postcode
                    FROM houses h
                    LEFT JOIN streets s ON h.street_id = s.id
                    WHERE h.id = ?
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            List<Street> streets = new ArrayList<>();
            while (rs.next()) {
                Street street = convertResultSetToStreet(rs);
                if (street != null) streets.add(street);
            }
            if (!streets.isEmpty()) {
                log.info("Street для House получен: {}", streets.size());
                return streets;
            }
            log.warn("House с таким id не найдена: {}", id);
            throw new EntityNotFoundException("House c id=" + id + " не найдена");
        } catch (Exception e) {
            log.error("Ошибка получения Street по House id={}", id, e);
            throw new RepositoryException("Ошибка получения Street по House id=" + id, e);
        }
    }

    /*
        Доп методы
     */

    private House convertResultSetToHouse(ResultSet rs) throws SQLException {
        House house = new House();
        house.setId(rs.getLong("id"));
        house.setName(rs.getString("name"));
        house.setNumberStoreys(rs.getInt("floors"));

        Date date = rs.getDate("date_building");
        house.setDateBuilding((date != null) ? date.toLocalDate() : null);

        String type = rs.getString("type");
        house.setType((type != null) ? TypeBuilding.valueOf(type) : null);

        return house;
    }

    private Street convertResultSetToStreet(ResultSet rs) throws SQLException {
        long streetId = rs.getLong("s_id");
        String streetName = rs.getString("s_name");
        int postcode = rs.getInt("postcode");
        return Street.builder()
                .id(streetId)
                .name(streetName)
                .postcode(postcode)
                .build();
    }

    private void fillQueryStreetsFields(House house, PreparedStatement statement) throws SQLException {
        statement.setString(1, house.getStreet().getName());
        statement.setInt(2, house.getStreet().getPostcode());
    }

    private void fillQueryHouseFields(House house, PreparedStatement statement) throws SQLException {
        statement.setString(1, house.getName());

        if (house.getDateBuilding() != null) statement.setDate(2, Date.valueOf(house.getDateBuilding()));
        else statement.setNull(2, Types.DATE);

        statement.setInt(3, house.getNumberStoreys());

        if (house.getType() != null) statement.setString(4, house.getType().name());
        else statement.setNull(4, Types.VARCHAR);

        if (house.getStreet() != null) statement.setLong(5, house.getStreet().getId());
        else statement.setNull(5, Types.BIGINT);
    }

    private void saveHouse(Connection connection, House house) throws EntitySaveException, SQLException {
        String sql = "INSERT INTO houses (name, date_building, floors, type, street_id) VALUES (?, ?, ?, ?, ?)";
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        fillQueryHouseFields(house, statement);
        statement.executeUpdate();
        log.info("{} сохранен: {}", house.getClass().getSimpleName(), house);

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                house.setId(generatedKeys.getLong(1));
                log.info("ID установлен на ID={}", house.getId());
            }
        } catch (Exception e) {
            log.warn("Ошибка генерации id");
            throw new GeneratedKeyException("Ошибка генерации id", e);
        }
    }

    private void saveStreet(Connection connection, House house) throws EntitySaveException {
        String sql = "INSERT INTO streets ( name, postcode ) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillQueryStreetsFields(house, statement);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    house.getStreet().setId(generatedKeys.getLong(1));
                }
            } catch (Exception e) {
                log.warn("Ошибка генерации id");
                throw new GeneratedKeyException("Ошибка генерации id", e);
            }
            log.info("{} сохранен: {}", house.getClass().getSimpleName(), house);
        } catch (Exception e) {
            log.warn("Ошибка сохранения House для House");
            throw new EntitySaveException("Ошибка сохранения House для House: ", e);
        }
    }

    private House joinHouseWitchStreet(House house, Street street) {
        if (house != null) {
            if (street != null) house.setStreetWithLink(street);
            log.info("House найден: {}", house);
        }
        return house;
    }
}
