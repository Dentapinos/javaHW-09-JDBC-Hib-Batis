package org.example.repository.jdbc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.House;
import org.example.entity.Street;
import org.example.enums.SessionName;
import org.example.enums.TypeBuilding;
import org.example.exception.*;
import org.example.repository.IStreetRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcStreetRepository implements IStreetRepository {

    public void save(Street street) throws EntitySaveException {
        if (street.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", street.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + street.getId());
        }
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            saveStreet(connection, street);
            if (street.getHouses() != null) {
                for (House house : street.getHouses()) {
                    saveHouse(connection, house);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", street.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + street.getClass().getSimpleName(), e);
        }
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

    private void saveStreet(Connection connection, Street street) throws EntitySaveException {
        String sql = "INSERT INTO streets ( name, postcode ) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillQueryStreetsFields(street, statement);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    street.setId(generatedKeys.getLong(1));
                }
            } catch (Exception e) {
                log.warn("Ошибка генерации id");
                throw new GeneratedKeyException("Ошибка генерации id", e);
            }
            log.info("{} сохранен: {}", street.getClass().getSimpleName(), street);
        } catch (Exception e) {
            log.warn("Ошибка сохранения House для House");
            throw new EntitySaveException("Ошибка сохранения House для House: ", e);
        }
    }

    public Street findById(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT h.*, s.name AS s_name, s.id AS s_id, s.postcode
                    FROM streets s
                    LEFT JOIN houses h ON h.street_id = s.id
                    WHERE s.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                Street assembledStreet = null;
                List<House> assembledHouses = new ArrayList<>();

                while (rs.next()) {
                    if (assembledStreet == null) {
                        assembledStreet = convertResultSetToStreet(rs);
                    }

                    long modelId = rs.getLong("id");
                    if (!rs.wasNull()) {
                        House assembledHouse = convertResultSetToHouse(rs);
                        assembledHouse.setId(modelId);
                        assembledHouses.add(assembledHouse);
                    }
                }

                if (assembledStreet != null) {
                    if (assembledHouses.size() > 0) assembledStreet.setHousesWithLinks(assembledHouses);
                    log.info("Streets получена: {}", assembledStreet);
                    return assembledStreet;
                }
                log.warn("Street с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Street c id=" + id + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Street по id={}", id, e);
            throw new RepositoryException("Ошибка получения Street по id=" + id, e);
        }
    }

    public Street findByIdLazy(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT * FROM streets WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    long streetId = rs.getLong("id");
                    String streetName = rs.getString("name");
                    int postcode = rs.getInt("postcode");
                    Street assembledstreet = Street.builder()
                            .id(streetId)
                            .name(streetName)
                            .postcode(postcode)
                            .build();

                    log.info("Streets получен: {}", assembledstreet);
                    return assembledstreet;
                }
                log.warn("Street с таким id не найден: {}", id);
                throw new EntityNotFoundException("Street c id=" + id + " не найден");
            }
        } catch (Exception e) {
            log.error("Ошибка получения Street по id={}", id, e);
            throw new RepositoryException("Ошибка получения Street по id=" + id, e);
        }
    }

    public List<Street> findAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT h.*, s.name AS s_name, s.id AS s_id, s.postcode
                    FROM streets s
                    LEFT JOIN houses h ON h.street_id = s.id
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();

                List<Street> resultStreets = new ArrayList<>();

                List<House> currentListHouses = new ArrayList<>();
                Street lastStreet = null;

                while (rs.next()) {
                    Street convertedStreet = convertResultSetToStreet(rs);
                    long houseId = rs.getLong("id");
                    House convertedHouse = null;
                    if (!rs.wasNull()) convertedHouse = convertResultSetToHouse(rs);

                    if (lastStreet == null) {
                        lastStreet = convertedStreet;
                    } else {
                        if (lastStreet.getId() != convertedStreet.getId()) {
                            if (!currentListHouses.isEmpty()) lastStreet.setHousesWithLinks(currentListHouses);
                            resultStreets.add(lastStreet);

                            lastStreet = convertedStreet;
                            currentListHouses = new ArrayList<>();
                        }
                    }
                    if (convertedHouse != null) {
                        convertedHouse.setId(houseId);
                        currentListHouses.add(convertedHouse);
                    }
                }
                log.info("Получены все Streets: {}", resultStreets);
                return resultStreets;
            }
        } catch (Exception e) {
            log.error("Ошибка получения всех Streets:", e);
            throw new RepositoryException("Ошибка получения всех Streets:", e);
        }
    }

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

    public void update(Street street) throws EntityUpdateException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = "UPDATE streets SET name = ?, postcode = ? WHERE id = ?";
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            fillQueryStreetsFields(street, statement);
            statement.setLong(3, street.getId());

            statement.executeUpdate();
            log.info("{} обновлен: {}", street.getClass().getSimpleName(), street);

            if (street.getHouses() != null) {
                List<House> loadedHouses = findById(street.getId()).getHouses();
                for (House house : loadedHouses) {
                    cleanLinkInBDForStreetById(connection, house.getStreet().getId());
                }
                for (House house : street.getHouses()) {
                    if (house.getId() == 0) {
                        saveHouse(connection, house);
                    } else {
                        String sqlUpdate = "UPDATE houses SET name = ?, date_building = ?, floors = ?, type = ?, street_id = ?  WHERE id = ?";
                        @Cleanup PreparedStatement statementUpdate = connection.prepareStatement(sqlUpdate);
                        fillQueryHouseFields(house, statementUpdate);
                        statementUpdate.setLong(6, house.getId());

                        statementUpdate.executeUpdate();
                    }
                }
                log.info("ModelsCar обновлены: {}", street.getHouses().size());
            }

        } catch (Exception e) {
            log.error("Ошибка обновления Street {}:", street, e);
            throw new EntitySaveException("Ошибка обновления Street", e);
        }
    }

    private void fillQueryStreetsFields(Street street, PreparedStatement statement) throws SQLException {
        statement.setString(1, street.getName());
        statement.setInt(2, street.getPostcode());
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

    public void delete(long id) throws EntityNotFoundException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            findByIdLazy(id);
            cleanLinkInBDForStreetById(connection, id);
            log.info("Связь Street c Task очищена");

            String sqlDelete = "DELETE FROM streets WHERE id = ?";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.setLong(1, id);
            deleteStatement.executeUpdate();
            log.info("Street c id={} удалена", id);
        } catch (Exception e) {
            log.error("Ошибка удаления Street по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Street по id=" + id, e);
        }
    }

    private void cleanLinkInBDForStreetById(Connection connection, long id) throws SQLException {
        String sql = """
                UPDATE houses h
                SET h.street_id = NULL
                WHERE h.street_id = ?;
                """;
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, id);
        statement.executeUpdate();
    }

    public void deleteAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    UPDATE houses h
                    SET h.street_id = NULL
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            log.info("Все связи Street c House очищены");

            String sqlDelete = "DELETE FROM streets";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.executeUpdate();
            log.info("все Street удалены");
        } catch (Exception e) {
            log.error("Ошибка удаления всех Streets:", e);
            throw new RepositoryException("Ошибка удаления всех Streets:", e);
        }
    }

    public List<House> getRelatedEntityByParentId(long id) {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT h.*
                    FROM streets s
                    LEFT JOIN houses h on h.street_id = s.id
                    WHERE s.id = ? LIMIT 5
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            List<House> houses = new ArrayList<>();
            while (rs.next()) {
                House house = convertResultSetToHouse(rs);
                if (house.getId() > 0) houses.add(house);
            }
            if (!houses.isEmpty()) {
                log.info("Houses для Street получен: {}", houses.size());
                return houses;
            }
            log.warn("Street с таким id не найдена: {}", id);
            throw new EntityNotFoundException("Street c id=" + id + " не найдена");
        } catch (Exception e) {
            log.error("Ошибка получения Task по Street id={}", id, e);
            throw new RepositoryException("Ошибка получения Task по Street id=" + id, e);
        }
    }
}
