package service;

import org.example.entity.House;
import org.example.entity.Street;
import org.example.enums.TypeBuilding;
import org.example.exception.EntityDeleteException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.EntitySaveException;
import org.example.repository.hibernate.HibernateHouseRepository;
import org.example.service.HouseService;
import org.example.utils.EntityCreatorUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HIBERNATE: Тестирование сервиса House")
class HibernateHouseServiceTest {

    List<House> houses;
    List<Street> streets;

    static HouseService service;

    @BeforeEach
    void setUp() {
        houses = EntityCreatorUtil.getHouses(5);
        streets = EntityCreatorUtil.getStreets(5);
    }

    @BeforeAll
    static void setUpAll() {
        HibernateHouseRepository houseRepository = new HibernateHouseRepository();
        service = new HouseService(houseRepository);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void save() {
        service.save(houses.get(0));
        service.save(houses.get(1));
        assertNotEquals(0, houses.get(0).getId());
        assertNotEquals(0, houses.get(1).getId());

        houses.get(2).setStreet(streets.getFirst());
        service.save(houses.get(2));
        assertEquals(streets.getFirst(), service.getById(houses.get(2).getId()).getStreet());

        houses.get(3).setId(-1L);
        assertThrows(EntitySaveException.class, () -> service.save(houses.get(3)));

        houses.get(3).setId(1L);
        assertThrows(EntitySaveException.class, () -> service.save(houses.get(3)));

        assertThrows(EntitySaveException.class, () -> service.save(houses.getFirst()));
    }

    @Test
    @DisplayName("Проверка обновления")
    void update() {
        House house = House.builder()
                .name("МСК")
                .type(TypeBuilding.LIVING_QUARTERS)
                .numberStoreys(25)
                .dateBuilding(LocalDate.of(2019, 1, 23))
                .build();

        house.setStreetWithLink(streets.getFirst());
        service.save(house);

//        Загружаем из базы и изменяем данные, после обновляем
        House houseForUpdate = service.getById(house.getId());
        houseForUpdate.setName("Updated");
        houseForUpdate.setType(TypeBuilding.GARAGE);
        houseForUpdate.setNumberStoreys(10);
        houseForUpdate.setDateBuilding(LocalDate.of(2012, 12, 13));
//        обновление street
        houseForUpdate.getStreet().setPostcode(888888);
        houseForUpdate.getStreet().setName("Updated");

        service.update(houseForUpdate);

//        достаем из базы обновленный house и проверяем что он равен тем обновлениям что мы внесли
        House updatedHouse = service.getById(houseForUpdate.getId());
        assertEquals(houseForUpdate.getName(), updatedHouse.getName());
        assertEquals(houseForUpdate.getType(), updatedHouse.getType());
        assertEquals(houseForUpdate.getNumberStoreys(), updatedHouse.getNumberStoreys());
        assertEquals(houseForUpdate.getDateBuilding(), updatedHouse.getDateBuilding());
        assertEquals(houseForUpdate.getStreet(), updatedHouse.getStreet());

//        проверяем что данные до обновления не такие, как новые
        assertNotEquals(house.getName(), updatedHouse.getName());
        assertNotEquals(house.getNumberStoreys(), updatedHouse.getNumberStoreys());
        assertNotEquals(house.getDateBuilding(), updatedHouse.getDateBuilding());
        assertNotEquals(house.getStreet(), updatedHouse.getStreet());

//        проверяем данные street из поля House
        houseForUpdate.getStreet().setId(0);
        service.update(houseForUpdate);
        assertNotEquals(houseForUpdate.getStreet().getId(), updatedHouse.getStreet().getId());
        assertEquals(houseForUpdate.getStreet().getName(), updatedHouse.getStreet().getName());
        assertEquals(houseForUpdate.getStreet().getPostcode(), updatedHouse.getStreet().getPostcode());

    }

    @Test
    @DisplayName("Проверка получения по id")
    void getById() {
        service.save(houses.get(0));
        service.save(houses.get(1));
        assertNotEquals(0, houses.get(0).getId());
        assertNotEquals(0, houses.get(1).getId());

        assertEquals(houses.get(0), service.getById(houses.get(0).getId()));
        assertEquals(houses.get(1), service.getById(houses.get(1).getId()));
        assertThrows(EntityNotFoundException.class, () -> service.getById(-1L));
    }

    @Test
    @DisplayName("Проверка получения всех записей")
    void getAll() {
        int allEntity = service.getAll().size();
        houses.forEach(task -> service.save(task));
        assertEquals(allEntity + houses.size(), service.getAll().size());
    }

    @Test
    @DisplayName("Проверка удаления по id")
    void deleteById() {
        service.save(houses.getFirst());
        service.deleteById(houses.getFirst().getId());
        assertThrows(EntityNotFoundException.class, () -> service.getById(houses.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления по объекту")
    void deleteByEntity() {
        service.save(houses.getFirst());
        service.deleteByEntity(houses.getFirst());
        assertThrows(EntityNotFoundException.class, () -> service.getById(houses.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления всех записей")
    void deleteAll() {
        houses.forEach(task -> service.save(task));
        service.deleteAll();
        assertEquals(0, service.getAll().size());
    }

    @Test
    @DisplayName("Проверка получения первых 5 записей вложенной сущности")
    public void childEntity() {
        houses.getFirst().setStreet(streets.getFirst());
        service.save(houses.getFirst());

        assertEquals(1, service.getRelatedEntityByParentId(houses.getFirst().getId()).size());
    }
}