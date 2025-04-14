package org.example.repository.jdbc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.enums.ColorKitty;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IKittyRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcKittyRepository implements IKittyRepository {

    public void save(Kitty kitty) throws EntitySaveException {
        checkedByZero(kitty);
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            if (kitty.getMasters() != null && !kitty.getMasters().isEmpty()) {
                saveAllMastersByKitty(kitty.getMasters(), connection);
                log.info("Master для Kitty успешно сохранены");
            }
            saveKitty(connection, kitty);
            if (kitty.getMasters() != null && !kitty.getMasters().isEmpty()) {
                saveRelation(connection, kitty);
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", kitty.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + kitty.getClass().getSimpleName(), e);
        }
    }

    private void saveAllMastersByKitty(List<Master> masters, Connection connection) throws EntitySaveException {
        for (Master master : masters) {
            saveMaster(connection, master);
        }
    }

    private void checkedByZero(Kitty kitty) throws EntitySaveException {
        if (kitty.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", kitty.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + kitty.getId());
        }
    }

    public Kitty findById(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT k.*, m.name AS m_name, m.id AS m_id, m.birthday AS m_birthday
                    FROM kitties k
                    LEFT JOIN master_kitty mk ON k.id = mk.kitty_id
                    LEFT JOIN masters m ON mk.master_id = m.id
                    WHERE k.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                Kitty gettedKitty = null;
                List<Master> gettedMasterList = new ArrayList<>();

                while (rs.next()) {
                    if (gettedKitty == null) {
                        gettedKitty = convertResultSetToKitty(rs);
                    }
                    long masterId = rs.getLong("id");
                    Master master = null;
                    if (!rs.wasNull()) master = convertResultSetToMaster(rs);
                    if (master != null) gettedMasterList.add(master);
                }

                if (gettedKitty != null) {
                    if (!gettedMasterList.isEmpty()) gettedKitty.setMasterWithLink(gettedMasterList);
                    log.info("Kitty с таким id не найдено: {}", id);
                    return gettedKitty;
                }
                log.warn("Kitty с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Kitty c id=" + id + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Kitty по id={}", id, e);
            throw new RepositoryException("Ошибка получения Kitty по id=" + id, e);
        }
    }

    public Kitty findByIdLazy(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT * FROM kitties WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    Kitty assembledTask = convertResultSetToKitty(rs);
                    log.info("Kitties получена: {}", assembledTask);
                    return assembledTask;
                }
                log.warn("Kitty с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Kitty c id=" + id + " не найден");
            }
        } catch (Exception e) {
            log.error("Ошибка получения Kitty по id={}", id, e);
            throw new RepositoryException("Ошибка получения Kitty по id=" + id, e);
        }
    }

    public List<Kitty> findAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT k.*, m.name AS m_name, m.id AS m_id, m.birthday AS m_birthday
                    FROM kitties k
                    LEFT JOIN master_kitty mk ON k.id = mk.kitty_id
                    LEFT JOIN masters m ON mk.master_id = m.id
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();

                List<Kitty> kitties = new ArrayList<>();

                Kitty lastKitty = null;
                List<Master> lastMasterList = new ArrayList<>();

                while (rs.next()) {
                    Kitty kitty = convertResultSetToKitty(rs);
                    long masterId = rs.getLong("m_id");
                    Master master = null;
                    if (!rs.wasNull()) master = convertResultSetToMaster(rs);


                    if (lastKitty != null) {
                        if (lastKitty.getId() != kitty.getId()) {
                            if (!lastMasterList.isEmpty()) {
                                lastKitty.setMasterWithLink(lastMasterList);
                            }
                            kitties.add(lastKitty);
                            lastMasterList = new ArrayList<>();
                            lastKitty = kitty;
                            if (master != null) lastMasterList.add(master);

                        } else {
                            if (master != null) lastMasterList.add(master);
                        }
                    } else {
                        lastKitty = kitty;
                        if (master != null) lastMasterList.add(master);
                    }
                }

                if (lastKitty != null) {
                    if (!lastMasterList.isEmpty()) lastKitty.setMasterWithLink(lastMasterList);
                    kitties.add(lastKitty);
                }

                if (!kitties.isEmpty()) {
                    log.info("Получены все Kitties: {}", kitties);
                    return kitties;
                }
                log.warn("Kitty не найдены");
                return kitties;
            }
        } catch (Exception e) {
            log.error("Ошибка получения всех Kitties:", e);
            throw new RepositoryException("Ошибка получения всех Kitties:", e);
        }
    }

    public void update(Kitty kitty) throws EntityUpdateException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            Kitty houseFromDB = findById(kitty.getId());

            if (kitty.getMasters() != null) {
                for (Master master : kitty.getMasters()) {
                    for (Master m2 : houseFromDB.getMasters()) {
                        if (master.getId() == m2.getId()) {
                            updateMaster(connection, master);
                            break;
                        }
                    }
                }

                for (Master master : kitty.getMasters()) {
                    for (Master m2 : houseFromDB.getMasters()) {
                        if (master.getId() == m2.getId()) {
                            saveMaster(connection, master);
                            saveRelation(connection, kitty.getId(), master.getId());
                            break;
                        }
                    }
                }
                log.info("Masters для Kitty обновлены");
            }

            String sql = "UPDATE kitties SET name = ?, birthday = ?, breed = ?, color = ?  WHERE id = ?";
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            fillQueryKittyFields(kitty, statement);
            statement.setLong(5, kitty.getId());

            statement.executeUpdate();
            log.info("Kitty обновлена: {}", kitty);

        } catch (Exception e) {
            log.error("Ошибка обновления Kitty {}:", kitty, e);
            throw new EntitySaveException("Ошибка обновления Kitty", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
//          ищем в базе Kitty с таким id и если нет то выбрасываем исключение
            findByIdLazy(id);

            String sqlDeleteRelation = "DELETE FROM master_kitty WHERE kitty_id = ?";
            @Cleanup PreparedStatement stDeleteRelation = connection.prepareStatement(sqlDeleteRelation);
            stDeleteRelation.setLong(1, id);
            stDeleteRelation.executeUpdate();
            log.info("Все связи c Kitty id={} удалены успешно", id);

            String sqlDelete = "DELETE FROM kitties WHERE id = ?";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.setLong(1, id);
            deleteStatement.executeUpdate();
            log.info("Kitty c id={} удалена", id);

        } catch (Exception e) {
            log.error("Ошибка удаления Kitty по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Kitty по id=" + id, e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sqlDeleteRelation = "DELETE FROM master_kitty";
            @Cleanup PreparedStatement stDeleteRelation = connection.prepareStatement(sqlDeleteRelation);
            stDeleteRelation.executeUpdate();
            log.info("Все связи удалены успешно");

            String sqlDelete = "DELETE FROM kitties";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.executeUpdate();
            log.info("Все Kitty удалены успешно");
        } catch (Exception e) {
            log.error("Ошибка удаления всех Kitties:", e);
            throw new RepositoryException("Ошибка удаления всех Kitties:", e);
        }
    }

    public List<Master> getRelatedEntityByParentId(long id) {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT m.name AS m_name, m.id AS m_id, m.birthday AS m_birthday
                    FROM kitties k
                    LEFT JOIN master_kitty mk ON k.id = mk.kitty_id
                    LEFT JOIN masters m ON mk.master_id = m.id
                    WHERE k.id = ? LIMIT 5
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            List<Master> masters = new ArrayList<>();
            while (rs.next()) {
                Master master = convertResultSetToMaster(rs);
                if (master != null) masters.add(master);
            }
            if (!masters.isEmpty()) {
                log.info("Master для Kitty получен: {}", masters.size());
                return masters;
            }
            log.warn("Kitty с таким id не найдена: {}", id);
            throw new EntityNotFoundException("Kitty c id=" + id + " не найдена");
        } catch (Exception e) {
            log.error("Ошибка получения Master по Kitty id={}", id, e);
            throw new RepositoryException("Ошибка получения Master по Kitty id=" + id, e);
        }
    }

    /*
        Доп методы
     */

    private Kitty convertResultSetToKitty(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String breed = rs.getString("breed");
        Date birthdate = rs.getDate("birthday");
        LocalDate localBirthDate = (birthdate != null) ? birthdate.toLocalDate() : null;
        String color = rs.getString("color");
        ColorKitty colorsKitties = (color != null) ? ColorKitty.valueOf(color) : null;

        Kitty kitty = Kitty.builder()
                .id(id)
                .name(name)
                .breed(breed)
                .birthday(localBirthDate)
                .color(colorsKitties)
                .build();

        return kitty;
    }

    private Master convertResultSetToMaster(ResultSet rs) throws SQLException {
        long masterId = rs.getLong("m_id");
        String streetName = rs.getString("m_name");
        Date birthdate = rs.getDate("m_birthday");
        LocalDate localBirthDate = (birthdate != null) ? birthdate.toLocalDate() : null;
        return Master.builder()
                .id(masterId)
                .name(streetName)
                .birthday(localBirthDate)
                .build();
    }

    private void fillQueryMasterFields(Master master, PreparedStatement statement) throws SQLException {
        if (master.getName() != null) statement.setString(1, master.getName());
        else statement.setNull(4, Types.VARCHAR);

        if (master.getBirthday() != null) statement.setDate(2, Date.valueOf(master.getBirthday()));
        else statement.setNull(2, Types.DATE);
    }

    private void fillQueryKittyFields(Kitty kitty, PreparedStatement statement) throws SQLException {
        if (kitty.getBreed() != null) statement.setString(1, kitty.getName());
        else statement.setNull(1, Types.VARCHAR);

        if (kitty.getBirthday() != null) statement.setDate(2, Date.valueOf(kitty.getBirthday()));
        else statement.setNull(2, Types.DATE);

        if (kitty.getBreed() != null) statement.setString(3, kitty.getBreed());
        else statement.setNull(3, Types.VARCHAR);

        if (kitty.getColor() != null) statement.setString(4, kitty.getColor().name());
        else statement.setNull(4, Types.VARCHAR);
    }

    private void saveKitty(Connection connection, Kitty kitty) throws EntitySaveException, SQLException {
        String sql = "INSERT INTO kitties (name, birthday, breed, color) VALUES (?, ?, ?, ?)";
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        fillQueryKittyFields(kitty, statement);
        statement.executeUpdate();
        log.info("{} сохранен: {}", kitty.getClass().getSimpleName(), kitty);

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                kitty.setId(generatedKeys.getLong(1));
                log.info("ID установлен на ID={}", kitty.getId());
            }
        } catch (Exception e) {
            log.warn("Ошибка генерации id");
            throw new GeneratedKeyException("Ошибка генерации id", e);
        }
    }

    private void saveMaster(Connection connection, Master master) throws EntitySaveException {
        String sql = "INSERT INTO masters ( name, birthday ) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillQueryMasterFields(master, statement);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    master.setId(generatedKeys.getLong(1));
                }
            } catch (Exception e) {
                log.warn("Ошибка генерации id");
                throw new GeneratedKeyException("Ошибка генерации id", e);
            }
            log.info("{} сохранен: {}", master.getClass().getSimpleName(), master);
        } catch (Exception e) {
            log.warn("Ошибка сохранения Master для Kitty");
            throw new EntitySaveException("Ошибка сохранения Master для Kitty: ", e);
        }
    }

    private void updateMaster(Connection connection, Master master) throws EntitySaveException {
        String sql = "UPDATE masters SET name = ?, birthday = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillQueryMasterFields(master, statement);
            statement.setLong(3, master.getId());
            statement.executeUpdate();
            log.info("{} обновлен: {}", master.getClass().getSimpleName(), master);
        } catch (Exception e) {
            log.warn("Ошибка обновления Master для Kitty");
            throw new EntitySaveException("Ошибка обновления Master для Kitty: ", e);
        }
    }

    private void saveRelation(Connection connection, Kitty kitty) throws SQLException {
        String sql = "INSERT INTO master_kitty (master_id, kitty_id) VALUES (?, ?)";

        for (Master master : kitty.getMasters()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, master.getId());
                statement.setLong(2, kitty.getId());

                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("Связь Master с Kitty установлена в таблице master_kitty");
    }

    private void saveRelation(Connection connection, long idKitty, long idMaster) throws SQLException {
        String sql = "INSERT INTO master_kitty (master_id, kitty_id) VALUES (?, ?)";

        @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, idMaster);
        statement.setLong(2, idKitty);

        statement.executeUpdate();

        log.info("Связь Master с Kitty установлена в таблице master_kitty");
    }
}
