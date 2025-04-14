package service;

import org.example.configs.SessionManager;
import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.enums.ColorKitty;
import org.example.enums.SessionName;
import org.example.exception.EntityDeleteException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.EntitySaveException;
import org.example.repository.jdbc.JdbcMasterRepository;
import org.example.service.MasterService;
import org.example.utils.CreateDropTablesUtil;
import org.example.utils.EntityCreatorUtil;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JDBC: Тестирование сервиса Master")
class JdbcMasterServiceTest {

    List<Master> masters;
    List<Kitty> kitties;

    static MasterService service;
    private static Connection connection;


    @BeforeEach
    void setUp() {
        masters = EntityCreatorUtil.getMasters(6);
        kitties = EntityCreatorUtil.getKitties(6);
    }

    @BeforeAll
    static void setUpAll() {
        JdbcMasterRepository masterRepository = new JdbcMasterRepository();
        service = new MasterService(masterRepository);
        connection = (Connection) SessionManager.createSession(SessionName.JDBC.getSessionName());
        CreateDropTablesUtil.createAllTables(connection);
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (connection != null) {
            CreateDropTablesUtil.dropAllTables(connection);
            connection.close();
        }
    }

    @Test
    @DisplayName("Проверка сохранения")
    void save() {
        Kitty kitty = kitties.getFirst();

        service.save(masters.get(0));
        service.save(masters.get(1));
        assertNotEquals(0, masters.getFirst().getId());
        assertNotEquals(0, masters.get(1).getId());

        masters.get(2).setKittyWithLinks(kitty);
        service.save(masters.get(2));
        assertEquals(kitty, service.getById(masters.get(2).getId()).getKitties().getFirst());

        masters.get(3).setId(-1L);
        assertThrows(EntitySaveException.class, () -> service.save(masters.get(3)));

        masters.get(3).setId(1L);
        assertThrows(EntitySaveException.class, () -> service.save(masters.get(3)));

        assertThrows(EntitySaveException.class, () -> service.save(masters.getFirst()));
    }

    @Test
    @DisplayName("Проверка обновления")
    void update() {
        Master master = Master.builder()
                .name("Петя")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        master.setKittyWithLinks(kitties.getFirst());
        service.save(master);

        Master masterForUpdate = service.getById(master.getId());
        masterForUpdate.setName("Updated");
        masterForUpdate.setBirthday(LocalDate.of(1993, 10, 10));

        masterForUpdate.getKitties().getFirst().setName("Белка");
        masterForUpdate.getKitties().getFirst().setBirthday(LocalDate.now());
        masterForUpdate.getKitties().getFirst().setColor(ColorKitty.RED_HAIRED);
        masterForUpdate.getKitties().getFirst().setBreed("Домашняя");

        service.update(masterForUpdate);

        Master updatedMaster = service.getById(masterForUpdate.getId());
        assertEquals(masterForUpdate.getName(), updatedMaster.getName());
        assertEquals(masterForUpdate.getBirthday(), updatedMaster.getBirthday());

        assertNotEquals(master.getKitties().getFirst(), updatedMaster.getKitties().getFirst());

        masterForUpdate.getKitties().getFirst().setId(0);
        service.update(masterForUpdate);
        assertNotEquals(masterForUpdate.getKitties().getFirst().getId(), updatedMaster.getKitties().getFirst().getId());
        assertEquals(masterForUpdate.getKitties().getFirst().getName(), updatedMaster.getKitties().getFirst().getName());
        assertEquals(masterForUpdate.getKitties().getFirst().getBirthday(), updatedMaster.getKitties().getFirst().getBirthday());
        assertEquals(masterForUpdate.getKitties().getFirst().getColor(), updatedMaster.getKitties().getFirst().getColor());
        assertEquals(masterForUpdate.getKitties().getFirst().getBreed(), updatedMaster.getKitties().getFirst().getBreed());
    }

    @Test
    @DisplayName("Проверка получения по id")
    void getById() {
        service.save(masters.get(0));
        service.save(masters.get(1));
        assertNotEquals(0, masters.getFirst().getId());
        assertNotEquals(0, masters.get(1).getId());

        assertEquals(masters.getFirst(), service.getById(masters.getFirst().getId()));
        assertEquals(masters.get(1), service.getById(masters.get(1).getId()));
        assertThrows(EntityNotFoundException.class, () -> service.getById(-1L));
    }

    @Test
    @DisplayName("Проверка получения всех записей")
    void getAll() {
        int allEntity = service.getAll().size();
        masters.forEach(task -> service.save(task));
        assertEquals(allEntity + masters.size(), service.getAll().size());
    }

    @Test
    @DisplayName("Проверка удаления по id")
    void deleteById() {
        service.save(masters.getFirst());
        service.deleteById(masters.getFirst().getId());
        assertThrows(EntityNotFoundException.class, () -> service.getById(masters.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления по объекту")
    void deleteByEntity() {
        service.save(masters.getFirst());
        service.deleteByEntity(masters.getFirst());
        assertThrows(EntityNotFoundException.class, () -> service.getById(masters.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(1000L));
    }

    @Test
    @DisplayName("Проверка удаления всех записей")
    void deleteAll() {
        for (int i = 0; i < masters.size(); i++) {
            masters.get(i).setKittyWithLinks(kitties.get(i));
            service.save(masters.get(i));
        }
        service.deleteAll();
        assertEquals(0, service.getAll().size());
    }

    @Test
    @DisplayName("Проверка получения первых 5 записей вложенной сущности")
    public void childEntity() {
        masters.getFirst().setKittyWithLinks(kitties);
        service.save(masters.getFirst());

        assertEquals(5, service.getRelatedEntityByParentId(masters.getFirst().getId()).size());
    }
}