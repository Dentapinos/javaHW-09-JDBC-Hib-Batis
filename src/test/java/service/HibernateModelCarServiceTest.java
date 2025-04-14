package service;

import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.enums.TypeBody;
import org.example.exception.EntityDeleteException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.EntitySaveException;
import org.example.repository.hibernate.HibernateModelCarRepository;
import org.example.service.ModelCarService;
import org.example.utils.EntityCreatorUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HIBERNATE: Тестирование сервиса Task")
class HibernateModelCarServiceTest {

    List<ModelCar> models;
    List<BrandCar> brands;

    static ModelCarService service;

    @BeforeEach
    void setUp() {
        models = EntityCreatorUtil.getModelCars(5);
        brands = EntityCreatorUtil.getBrandsCar(5);
    }

    @BeforeAll
    static void setUpAll() {
        HibernateModelCarRepository modelRepository = new HibernateModelCarRepository();
        service = new ModelCarService(modelRepository);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void save() {
        service.save(models.get(0));
        service.save(models.get(1));
        assertNotEquals(0, models.get(0).getId());
        assertNotEquals(0, models.get(1).getId());

        models.get(2).setBrand(brands.getFirst());
        service.save(models.get(2));
        assertEquals(brands.getFirst(), service.getById(models.get(2).getId()).getBrand());

        models.get(3).setId(-1L);
        assertThrows(EntitySaveException.class, () -> service.save(models.get(3)));

        models.get(3).setId(1L);
        assertThrows(EntitySaveException.class, () -> service.save(models.get(3)));

        assertThrows(EntitySaveException.class, () -> service.save(models.getFirst()));
    }

    @Test
    @DisplayName("Проверка обновления")
    void update() {
        ModelCar model = ModelCar.builder()
                .name("lx30")
                .width(2500)
                .length(4000)
                .body(TypeBody.COUPE)
                .build();

        model.setBrand(brands.getFirst());
        service.save(model);

//        Загружаем из базы и изменяем данные, после обновляем
        ModelCar modelForUpdate = service.getById(model.getId());
        modelForUpdate.setName("Updated");
        modelForUpdate.setWidth(2510);
        modelForUpdate.setLength(4011);
        modelForUpdate.setBody(TypeBody.PICKUP);
//        обновление street
        modelForUpdate.getBrand().setName("Updated");
        modelForUpdate.getBrand().setDateFoundation(LocalDate.now());

        service.update(modelForUpdate);

//        достаем из базы обновленный model и проверяем что он равен тем обновлениям что мы внесли
        ModelCar updatedHouse = service.getById(modelForUpdate.getId());
        assertEquals(modelForUpdate.getName(), updatedHouse.getName());
        assertEquals(modelForUpdate.getWidth(), updatedHouse.getWidth());
        assertEquals(modelForUpdate.getLength(), updatedHouse.getLength());
        assertEquals(modelForUpdate.getBody(), updatedHouse.getBody());

        assertEquals(modelForUpdate.getBrand(), updatedHouse.getBrand());

//        проверяем что данные до обновления не такие как новые
        assertNotEquals(model.getName(), updatedHouse.getName());
        assertNotEquals(model.getWidth(), updatedHouse.getWidth());
        assertNotEquals(model.getLength(), updatedHouse.getLength());
        assertNotEquals(model.getBody(), updatedHouse.getBody());

        assertNotEquals(model.getBrand(), updatedHouse.getBrand());

//        проверяем данные brand из поля ModelCar
        modelForUpdate.getBrand().setId(0);
        service.update(modelForUpdate);
        assertNotEquals(modelForUpdate.getBrand().getId(), updatedHouse.getBrand().getId());
        assertEquals(modelForUpdate.getBrand().getName(), updatedHouse.getBrand().getName());
        assertEquals(modelForUpdate.getBrand().getDateFoundation(), updatedHouse.getBrand().getDateFoundation());
    }

    @Test
    @DisplayName("Проверка получения по id")
    void getById() {
        service.save(models.get(0));
        service.save(models.get(1));
        assertNotEquals(0, models.get(0).getId());
        assertNotEquals(0, models.get(1).getId());

        assertEquals(models.get(0), service.getById(models.get(0).getId()));
        assertEquals(models.get(1), service.getById(models.get(1).getId()));
        assertThrows(EntityNotFoundException.class, () -> service.getById(-1L));
    }

    @Test
    @DisplayName("Проверка получения всех записей")
    void getAll() {
        int allEntity = service.getAll().size();
        models.forEach(task -> service.save(task));
        assertEquals(allEntity + models.size(), service.getAll().size());
    }

    @Test
    @DisplayName("Проверка удаления по id")
    void deleteById() {
        service.save(models.getFirst());
        service.deleteById(models.getFirst().getId());
        assertThrows(EntityNotFoundException.class, () -> service.getById(models.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления по объекту")
    void deleteByEntity() {
        service.save(models.getFirst());
        service.deleteByEntity(models.getFirst());
        assertThrows(EntityNotFoundException.class, () -> service.getById(models.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления всех записей")
    void deleteAll() {
        models.forEach(task -> service.save(task));
        service.deleteAll();
        assertEquals(0, service.getAll().size());
    }

    @Test
    @DisplayName("Проверка получения первых 5 записей вложенной сущности")
    public void childEntity() {
        models.getFirst().setBrand(brands.getFirst());
        service.save(models.getFirst());

        assertEquals(1, service.getRelatedEntityByParentId(models.getFirst().getId()).size());
    }
}