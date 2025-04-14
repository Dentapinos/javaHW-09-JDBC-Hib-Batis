package org.example.service;

import org.example.entity.House;
import org.example.entity.Street;
import org.example.exception.EntityDeleteException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.EntitySaveException;
import org.example.repository.hibernate.HibernateStreetRepository;
import org.example.utils.EntityCreatorUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HIBERNATE: Тестирование сервиса Street")
class HibernateStreetServiceTest {

    List<Street> streets;
    List<House> houses;

    static StreetService service;

    @BeforeEach
    void setUp() {
        streets = EntityCreatorUtil.getStreets(6);
        houses = EntityCreatorUtil.getHouses(6);
    }

    @BeforeAll
    static void setUpAll() {
        HibernateStreetRepository streetRepository = new HibernateStreetRepository();
        service = new StreetService(streetRepository);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void save() {
        House house = houses.getFirst();

        service.save(streets.get(0));
        service.save(streets.get(1));
        assertNotEquals(0, streets.getFirst().getId());
        assertNotEquals(0, streets.get(1).getId());

        streets.get(2).setHouseWithLinks(house);
        service.save(streets.get(2));
        assertEquals(house, service.getById(streets.get(2).getId()).getHouses().getFirst());

        streets.get(3).setId(-1L);
        assertThrows(EntitySaveException.class, () -> service.save(streets.get(3)));

        streets.get(3).setId(1L);
        assertThrows(EntitySaveException.class, () -> service.save(streets.get(3)));

        assertThrows(EntitySaveException.class, () -> service.save(streets.getFirst()));
    }

    @Test
    @DisplayName("Проверка обновления")
    void update() {
        Street street = Street.builder()
                .name("Северная")
                .postcode(111111)
                .build();

        House house = EntityCreatorUtil.getHouse();
//        Так как Street не держатель связи, то не обходимо установить
//        House -> Street, а Street -> House , для этого у Street есть
//        метод connectOneHouse() который установит двустороннюю связь
        street.setHouseWithLinks(houses.getFirst());
        service.save(street);

        Street streetFromDB = service.getById(street.getId());
        streetFromDB.setName("Updated");
        streetFromDB.setPostcode(999999);
        streetFromDB.getHouses().getFirst().setNumberStoreys(9);

        service.update(streetFromDB);

        Street newStreetFromDB = service.getById(streetFromDB.getId());
        assertEquals(streetFromDB.getName(), newStreetFromDB.getName());
        assertEquals(streetFromDB.getPostcode(), newStreetFromDB.getPostcode());
        assertEquals(streetFromDB.getHouses().getFirst(), newStreetFromDB.getHouses().getFirst());

        assertNotEquals(street.getName(), newStreetFromDB.getName());
        assertNotEquals(street.getPostcode(), newStreetFromDB.getPostcode());
        assertNotEquals(street.getHouses().getFirst(), newStreetFromDB.getHouses().getFirst());

        streetFromDB.getHouses().getFirst().setId(0);
        service.update(streetFromDB);
        assertNotEquals(streetFromDB.getHouses().getFirst().getId(), newStreetFromDB.getHouses().getFirst().getId());
        assertEquals(streetFromDB.getHouses().getFirst().getName(), newStreetFromDB.getHouses().getFirst().getName());
        assertEquals(streetFromDB.getHouses().getFirst().getDateBuilding(), newStreetFromDB.getHouses().getFirst().getDateBuilding());
        assertEquals(streetFromDB.getHouses().getFirst().getType(), newStreetFromDB.getHouses().getFirst().getType());
        assertEquals(streetFromDB.getHouses().getFirst().getNumberStoreys(), newStreetFromDB.getHouses().getFirst().getNumberStoreys());
    }

    @Test
    @DisplayName("Проверка получения по id")
    void getById() {
        service.save(streets.get(0));
        service.save(streets.get(1));
        assertNotEquals(0, streets.getFirst().getId());
        assertNotEquals(0, streets.get(1).getId());

        assertEquals(streets.getFirst(), service.getById(streets.getFirst().getId()));
        assertEquals(streets.get(1), service.getById(streets.get(1).getId()));
        assertThrows(EntityNotFoundException.class, () -> service.getById(-1L));
    }

    @Test
    @DisplayName("Проверка получения всех записей")
    void getAll() {
        int allEntity = service.getAll().size();
        streets.forEach(task -> service.save(task));
        assertEquals(allEntity + streets.size(), service.getAll().size());
    }

    @Test
    @DisplayName("Проверка удаления по id")
    void deleteById() {
        service.save(streets.getFirst());
        service.deleteById(streets.getFirst().getId());
        assertThrows(EntityNotFoundException.class, () -> service.getById(streets.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления по объекту")
    void deleteByEntity() {
        service.save(streets.getFirst());
        service.deleteByEntity(streets.getFirst());
        assertThrows(EntityNotFoundException.class, () -> service.getById(streets.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления всех записей")
    void deleteAll() {

        for (int i = 0; i < streets.size(); i++) {
            streets.get(i).setHouseWithLinks(houses.get(i));
            service.save(streets.get(i));
        }

        service.deleteAll();
        assertEquals(0, service.getAll().size());
    }

    @Test
    @DisplayName("Проверка получения первых 5 записей вложенной сущности")
    public void childEntity() {
        streets.getFirst().setHousesWithLinks(houses);
        service.save(streets.getFirst());

        assertEquals(5, service.getRelatedEntityByParentId(streets.getFirst().getId()).size());
    }
}