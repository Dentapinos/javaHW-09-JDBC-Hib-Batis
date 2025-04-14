package service;

import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.enums.ColorKitty;
import org.example.exception.EntityDeleteException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.EntitySaveException;
import org.example.repository.batis.BatisKittyRepository;
import org.example.service.KittyService;
import org.example.utils.EntityCreatorUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MyBATIS: Тестирование сервиса Kitty")
class BatisKittyServiceTest {

    List<Kitty> kitties;
    List<Master> masters;

    static KittyService service;

    @BeforeEach
    void setUp() {
        kitties = EntityCreatorUtil.getKitties(6);
        masters = EntityCreatorUtil.getMasters(6);
    }

    @BeforeAll
    static void setUpAll() {
        BatisKittyRepository kittyRepository = new BatisKittyRepository();
        service = new KittyService(kittyRepository);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void save() {
        Master master = masters.getFirst();

        service.save(kitties.get(0));
        service.save(kitties.get(1));
        assertNotEquals(0, kitties.getFirst().getId());
        assertNotEquals(0, kitties.get(1).getId());

        kitties.get(2).setMasterWithLink(master);
        service.save(kitties.get(2));
        assertEquals(master, service.getById(kitties.get(2).getId()).getMasters().getFirst());

        kitties.get(3).setId(-1L);
        assertThrows(EntitySaveException.class, () -> service.save(kitties.get(3)));

        kitties.get(3).setId(1L);
        assertThrows(EntitySaveException.class, () -> service.save(kitties.get(3)));

        assertThrows(EntitySaveException.class, () -> service.save(kitties.getFirst()));
    }

    @Test
    @DisplayName("Проверка обновления")
    void update() {
        Kitty kitty = Kitty.builder()
                .name("Мурка")
                .breed("Царская")
                .birthday(LocalDate.of(2018, 8, 9))
                .color(ColorKitty.BROWN)
                .build();

        kitty.setMasterWithLink(masters.getFirst());
        service.save(kitty);

        Kitty kittyForUpdate = service.getById(kitty.getId());
        kittyForUpdate.setName("Updated");
        kittyForUpdate.setBreed("Домашняя");
        kittyForUpdate.setBirthday(LocalDate.of(2020, 3, 19));
        kittyForUpdate.setColor(ColorKitty.BLACK);
        kittyForUpdate.getMasters().getFirst().setName("Николай");
        kittyForUpdate.getMasters().getFirst().setBirthday(LocalDate.now());

        service.update(kittyForUpdate);

        Kitty updatedKitty = service.getById(kittyForUpdate.getId());
        assertEquals(kittyForUpdate.getName(), updatedKitty.getName());
        assertEquals(kittyForUpdate.getBreed(), updatedKitty.getBreed());
        assertEquals(kittyForUpdate.getColor(), updatedKitty.getColor());
        assertEquals(kittyForUpdate.getBirthday(), updatedKitty.getBirthday());

        assertNotEquals(kitty.getMasters().getFirst(), updatedKitty.getMasters().getFirst());

        kittyForUpdate.getMasters().getFirst().setId(0);
        service.update(kittyForUpdate);
        assertNotEquals(kittyForUpdate.getMasters().getFirst().getId(), updatedKitty.getMasters().getFirst().getId());
        assertEquals(kittyForUpdate.getMasters().getFirst().getName(), updatedKitty.getMasters().getFirst().getName());
        assertEquals(kittyForUpdate.getMasters().getFirst().getBirthday(), updatedKitty.getMasters().getFirst().getBirthday());
    }

    @Test
    @DisplayName("Проверка получения по id")
    void getById() {
        service.save(kitties.get(0));
        service.save(kitties.get(1));
        assertNotEquals(0, kitties.getFirst().getId());
        assertNotEquals(0, kitties.get(1).getId());

        assertEquals(kitties.getFirst(), service.getById(kitties.getFirst().getId()));
        assertEquals(kitties.get(1), service.getById(kitties.get(1).getId()));
        assertThrows(EntityNotFoundException.class, () -> service.getById(-1L));
    }

    @Test
    @DisplayName("JDBC: Проверка получения всех записей")
    void getAll() {
        int allEntity = service.getAll().size();
        kitties.forEach(task -> service.save(task));
        assertEquals(allEntity + kitties.size(), service.getAll().size());
    }

    @Test
    @DisplayName("Проверка удаления по id")
    void deleteById() {
        service.save(kitties.getFirst());
        service.deleteById(kitties.getFirst().getId());
        assertThrows(EntityNotFoundException.class, () -> service.getById(kitties.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления по объекту")
    void deleteByEntity() {
        service.save(kitties.getFirst());
        service.deleteByEntity(kitties.getFirst());
        assertThrows(EntityNotFoundException.class, () -> service.getById(kitties.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления всех записей")
    void deleteAll() {

        for (int i = 0; i < kitties.size(); i++) {
            kitties.get(i).setMasterWithLink(masters.get(i));
            service.save(kitties.get(i));
        }

        service.deleteAll();
        assertEquals(0, service.getAll().size());
    }

    @Test
    @DisplayName("Проверка получения первых 5 записей вложенной сущности")
    public void childEntity() {
        kitties.getFirst().setMasterWithLink(masters);
        service.save(kitties.getFirst());

        assertEquals(5, service.getRelatedEntityByParentId(kitties.getFirst().getId()).size());
    }
}