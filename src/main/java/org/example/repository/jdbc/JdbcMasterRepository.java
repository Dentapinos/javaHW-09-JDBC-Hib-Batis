package org.example.repository.jdbc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.example.configs.SessionManager;
import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.enums.ColorKitty;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.repository.IMasterRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcMasterRepository implements IMasterRepository {

    public void save(Master master) throws EntitySaveException {
        checkedByZero(master);
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            if (master.getKitties() != null && !master.getKitties().isEmpty()) {
                saveAllKittiesByMaster(master.getKitties(), connection);
                log.info("Kitty для Master успешно сохранены");
            }
            saveMaster(connection, master);
            if (master.getKitties() != null && !master.getKitties().isEmpty()) {
                saveRelation(connection, master);
            }
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", master.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + master.getClass().getSimpleName(), e);
        }
    }

    private void saveAllKittiesByMaster(List<Kitty> kittyList, Connection connection) throws EntitySaveException {
        for (Kitty kitty : kittyList) {
            saveKitty(connection, kitty);
        }
    }

    private void checkedByZero(Master master) throws EntitySaveException {
        if (master.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", master.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + master.getId());
        }
    }

    public Master findById(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT k.*, m.name AS m_name, m.id AS m_id, m.birthday AS m_birthday
                    FROM masters m
                    LEFT JOIN master_kitty mk ON m.id = mk.master_id
                    LEFT JOIN kitties k ON mk.kitty_id = k.id
                    WHERE m.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                Master gettedMaster = null;
                List<Kitty> gettedMasterBList = new ArrayList<>();

                while (rs.next()) {
                    if (gettedMaster == null) {
                        gettedMaster = convertResultSetToMaster(rs);
                    }
                    long masterBId = rs.getLong("id");
                    Kitty kitty = null;
                    if (!rs.wasNull()) kitty = convertResultSetToKitty(rs);
                    if (kitty != null) gettedMasterBList.add(kitty);
                }

                if (gettedMaster != null) {
                    if (!gettedMasterBList.isEmpty()) gettedMaster.setKittyWithLinks(gettedMasterBList);
                    log.info("Master с таким id не найдено: {}", id);
                    return gettedMaster;
                }

//                ----------------------------------

//                Kitty lastMasterB = null;
//                List<Master> lastMasterList = new ArrayList<>();
//
//                while (rs.next()) {
//                    if (lastMasterB == null) {
//                        lastMasterB = convertResultSetToMasterB(rs);
//                    }
//                    long masterId = rs.getLong("id");
//                    Master master = null;
//                    if (!rs.wasNull()) master = convertResultSetToMaster(rs);
//                    if (master != null) lastMasterList.add(master);
//                }
//
//                if (lastMasterB != null) {
//                    if (!lastMasterList.isEmpty()) lastMasterB.setMasterWithLinks(lastMasterList);
//                    log.info("Master с таким id не найдено: {}", id);
//                    return lastMasterB;
//                }

//                ---------------------------------------


                log.warn("Master с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Master c id=" + id + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Master по id={}", id, e);
            throw new RepositoryException("Ошибка получения Master по id=" + id, e);
        }
    }

    public Master findByIdLazy(long id) throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT m.id AS m_id, m.name AS m_name, m.birthday AS m_birthday
                    FROM masters m WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    Master assembledMaster = convertResultSetToMaster(rs);
                    log.info("Masters получена: {}", assembledMaster);
                    return assembledMaster;
                }
                log.warn("Master с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Master c id=" + id + " не найден");
            }
        } catch (Exception e) {
            log.error("Ошибка получения Master по id={}", id, e);
            throw new RepositoryException("Ошибка получения Master по id=" + id, e);
        }
    }

    public List<Master> findAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT k.*, m.name AS m_name, m.id AS m_id, m.birthday AS m_birthday
                    FROM masters m
                    LEFT JOIN master_kitty mk ON m.id = mk.master_id
                    LEFT JOIN kitties k ON mk.kitty_id = k.id
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();

                List<Master> masters = new ArrayList<>();

                Master lastMaster = null;
                List<Kitty> lastKittyList = new ArrayList<>();

                while (rs.next()) {
                    Master master = convertResultSetToMaster(rs);
                    long masterBId = rs.getLong("id");
                    Kitty kitty = null;
                    if (!rs.wasNull()) kitty = convertResultSetToKitty(rs);

                    if (lastMaster != null) {
                        if (lastMaster.getId() != master.getId()) {
                            if (!lastKittyList.isEmpty()) {
                                lastMaster.setKittyWithLinks(lastKittyList);
                            }
                            masters.add(lastMaster);
                            lastKittyList = new ArrayList<>();
                            lastMaster = master;
                            if (kitty != null) lastKittyList.add(kitty);
                        } else {
                            if (kitty != null) lastKittyList.add(kitty);
                        }
                    } else {
                        lastMaster = master;
                        if (kitty != null) lastKittyList.add(kitty);
                    }
                }

                if (lastMaster != null) {
                    if (!lastKittyList.isEmpty()) lastMaster.setKittyWithLinks(lastKittyList);
                    masters.add(lastMaster);
                }

                if (!masters.isEmpty()) {
                    log.info("Получены все Masters: {}", masters);
                    return masters;
                }
                log.warn("Master не найдены");
                return masters;
            }
        } catch (Exception e) {
            log.error("Ошибка получения всех Masters:", e);
            throw new RepositoryException("Ошибка получения всех Masters:", e);
        }
    }

    public void update(Master master) throws EntityUpdateException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            Master masterFromDB = findById(master.getId());

            if (master.getKitties() != null) {
                for (Kitty kitty : master.getKitties()) {
                    for (Kitty m2 : masterFromDB.getKitties()) {
                        if (kitty.getId() == m2.getId()) {
                            updateKitty(connection, kitty);
                            break;
                        }
                    }
                }

                for (Kitty kitty : master.getKitties()) {
                    for (Kitty m2 : masterFromDB.getKitties()) {
                        if (kitty.getId() == m2.getId()) {
                            saveKitty(connection, kitty);
                            saveRelation(connection, kitty.getId(), master.getId());
                            break;
                        }
                    }
                }
                log.info("Kitty для Master обновлены");
            }

            String sql = "UPDATE masters SET name = ?, birthday = ? WHERE id = ?";
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            fillQueryMasterFields(master, statement);
            statement.setLong(3, master.getId());

            statement.executeUpdate();
            log.info("Master обновлена: {}", master);

        } catch (Exception e) {
            log.error("Ошибка обновления Master {}:", master, e);
            throw new EntitySaveException("Ошибка обновления Master", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
//          ищем в базе Master с таким id и если нет то выбрасываем исключение
            findByIdLazy(id);

            String sqlDeleteRelation = "DELETE FROM master_kitty WHERE master_id = ?";
            @Cleanup PreparedStatement stDeleteRelation = connection.prepareStatement(sqlDeleteRelation);
            stDeleteRelation.setLong(1, id);
            stDeleteRelation.executeUpdate();
            log.info("Все связи c Master id={} удалены успешно", id);

            String sqlDelete = "DELETE FROM masters WHERE id = ?";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.setLong(1, id);
            deleteStatement.executeUpdate();
            log.info("Master c id={} удалена", id);

        } catch (Exception e) {
            log.error("Ошибка удаления Master по id={} :", id, e);
            throw new EntityDeleteException("Ошибка удаления Master по id=" + id, e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sqlDeleteRelation = "DELETE FROM master_kitty";
            @Cleanup PreparedStatement stDeleteRelation = connection.prepareStatement(sqlDeleteRelation);
            stDeleteRelation.executeUpdate();
            log.info("Все связи c Master удалены успешно");

            String sqlDelete = "DELETE FROM masters";
            @Cleanup PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            deleteStatement.executeUpdate();
            log.info("Все Master удалены успешно");
        } catch (Exception e) {
            log.error("Ошибка удаления всех Masters:", e);
            throw new RepositoryException("Ошибка удаления всех Masters:", e);
        }
    }

    public List<Kitty> getRelatedEntityByParentId(long id) {
        try (Connection connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName())) {
            String sql = """
                    SELECT k.*
                    FROM masters m
                    LEFT JOIN master_kitty mk ON m.id = mk.master_id
                    LEFT JOIN kitties k ON mk.kitty_id = k.id
                    WHERE m.id = ? LIMIT 5
                    """;
            @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            List<Kitty> kitties = new ArrayList<>();
            while (rs.next()) {
                Kitty kitty = convertResultSetToKitty(rs);
                if (kitty != null) kitties.add(kitty);
            }
            if (!kitties.isEmpty()) {
                log.info("Kitty для Master получен: {}", kitties.size());
                return kitties;
            }
            log.warn("Master с таким id не найдена: {}", id);
            throw new EntityNotFoundException("Master c id=" + id + " не найдена");
        } catch (Exception e) {
            log.error("Ошибка получения Kitty по Master id={}", id, e);
            throw new RepositoryException("Ошибка получения Kitty по Master id=" + id, e);
        }
    }

    /*
        Доп методы
     */

    private Master convertResultSetToMaster(ResultSet rs) throws SQLException {
        long masterBId = rs.getLong("m_id");
        String streetName = rs.getString("m_name");
        Date birthdate = rs.getDate("m_birthday");
        LocalDate localBirthDate = (birthdate != null) ? birthdate.toLocalDate() : null;
        return Master.builder()
                .id(masterBId)
                .name(streetName)
                .birthday(localBirthDate)
                .build();
    }

    private Kitty convertResultSetToKitty(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String breed = rs.getString("breed");
        Date birthdate = rs.getDate("birthday");
        LocalDate localBirthDate = (birthdate != null) ? birthdate.toLocalDate() : null;
        String color = rs.getString("color");
        ColorKitty colorKitty = (color != null) ? ColorKitty.valueOf(color) : null;

        Kitty kitty = Kitty.builder()
                .id(id)
                .name(name)
                .breed(breed)
                .birthday(localBirthDate)
                .color(colorKitty)
                .build();

        return kitty;


    }

    private void fillQueryMasterFields(Master master, PreparedStatement statement) throws SQLException {
        if (master.getName() != null) statement.setString(1, master.getName());
        else statement.setNull(1, Types.VARCHAR);

        if (master.getBirthday() != null) statement.setDate(2, Date.valueOf(master.getBirthday()));
        else statement.setNull(2, Types.DATE);
    }

    private void fillQueryKittyFields(Kitty kitty, PreparedStatement statement) throws SQLException {
        if (kitty.getName() != null) statement.setString(1, kitty.getName());
        else statement.setNull(1, Types.VARCHAR);

        if (kitty.getBirthday() != null) statement.setDate(2, Date.valueOf(kitty.getBirthday()));
        else statement.setNull(2, Types.DATE);

        if (kitty.getBreed() != null) statement.setString(3, kitty.getBreed());
        else statement.setNull(3, Types.VARCHAR);

        if (kitty.getColor() != null) statement.setString(4, kitty.getColor().name());
        else statement.setNull(4, Types.VARCHAR);
    }

    private void saveMaster(Connection connection, Master master) throws EntitySaveException, SQLException {
        String sql = "INSERT INTO masters (name, birthday) VALUES (?, ?)";
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        fillQueryMasterFields(master, statement);
        statement.executeUpdate();
        log.info("{} сохранен: {}", master.getClass().getSimpleName(), master);

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                master.setId(generatedKeys.getLong(1));
                log.info("ID установлен на ID={}", master.getId());
            }
        } catch (Exception e) {
            log.warn("Ошибка генерации id");
            throw new GeneratedKeyException("Ошибка генерации id", e);
        }
    }

    private void saveKitty(Connection connection, Kitty kitty) throws EntitySaveException {
        String sql = "INSERT INTO kitties ( name, birthday, breed, color ) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillQueryKittyFields(kitty, statement);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    kitty.setId(generatedKeys.getLong(1));
                }
            } catch (Exception e) {
                log.warn("Ошибка генерации id");
                throw new GeneratedKeyException("Ошибка генерации id", e);
            }
            log.info("{} сохранен: {}", kitty.getClass().getSimpleName(), kitty);
        } catch (Exception e) {
            log.warn("Ошибка сохранения Kitty для Master");
            throw new EntitySaveException("Ошибка сохранения Kitty для Master: ", e);
        }
    }

    private void updateKitty(Connection connection, Kitty kitty) throws EntitySaveException {
        String sql = "UPDATE kitties SET name = ?, birthday = ?, breed = ?, color = ?  WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillQueryKittyFields(kitty, statement);
            statement.setLong(5, kitty.getId());
            statement.executeUpdate();
            log.info("{} обновлен: {}", kitty.getClass().getSimpleName(), kitty);
        } catch (Exception e) {
            log.warn("Ошибка обновления Kitty для Master");
            throw new EntitySaveException("Ошибка обновления Kitty для Master: ", e);
        }
    }

    private void saveRelation(Connection connection, Master master) throws SQLException {
        String sql = "INSERT INTO master_kitty (kitty_id, master_id) VALUES (?, ?)";

        for (Kitty kitty : master.getKitties()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, kitty.getId());
                statement.setLong(2, master.getId());

                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("Связь Kitty с Master установлена в таблице master_kitty");
    }

    private void saveRelation(Connection connection, long idKitty, long idMaster) throws SQLException {
        String sql = "INSERT INTO master_kitty (kitty_id, master_id) VALUES (?, ?)";

        @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, idKitty);
        statement.setLong(2, idMaster);

        statement.executeUpdate();

        log.info("Связь Kitty с Master установлена в таблице masterB_master");
    }
}
