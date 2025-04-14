package org.example.service;

import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.enums.TypeBody;
import org.example.exception.EntityDeleteException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.EntitySaveException;
import org.example.repository.hibernate.HibernateBrandRepository;
import org.example.utils.EntityCreatorUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HIBERNATE: Тестирование сервиса BrandCar")
class HibernateBrandCarServiceTest {

    List<BrandCar> brands;
    List<ModelCar> models;

    static BrandCarService service;

    @BeforeEach
    void setUp() {
        brands = EntityCreatorUtil.getBrandsCar(5);
        models = EntityCreatorUtil.getModelCars(5);
    }

    @BeforeAll
    static void setUpAll() {
        HibernateBrandRepository brandRepository = new HibernateBrandRepository();
        service = new BrandCarService(brandRepository);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void save() {
        ModelCar modelCar = models.getFirst();

        service.save(brands.get(0));
        service.save(brands.get(1));
        assertNotEquals(0, brands.getFirst().getId());
        assertNotEquals(0, brands.get(1).getId());

        brands.get(2).addModel(modelCar);
        service.save(brands.get(2));
        assertEquals(modelCar, service.getById(brands.get(2).getId()).getModels().getFirst());

        brands.get(3).setId(-1L);
        assertThrows(EntitySaveException.class, () -> service.save(brands.get(3)));

        brands.get(3).setId(1L);
        assertThrows(EntitySaveException.class, () -> service.save(brands.get(3)));

        assertThrows(EntitySaveException.class, () -> service.save(brands.getFirst()));
    }

    @Test
    @DisplayName("Проверка обновления")
    void update() {
        BrandCar brandCar = BrandCar.builder()
                .name("Toyota")
                .dateFoundation(LocalDate.of(1995, 10, 25))
                .build();

        brandCar.addModel(models.getFirst());
        service.save(brandCar);

        BrandCar brandForUpdate = service.getById(brandCar.getId());
        brandForUpdate.setName("Updated");
        brandForUpdate.setDateFoundation(LocalDate.of(1998, 1, 5));
        brandForUpdate.getModels().getFirst().setName("Updated");
        brandForUpdate.getModels().getFirst().setBody(TypeBody.SEDAN);
        brandForUpdate.getModels().getFirst().setWidth(2001);
        brandForUpdate.getModels().getFirst().setLength(5001);

        service.update(brandForUpdate);

        BrandCar updatedModel = service.getById(brandForUpdate.getId());
        assertEquals(brandForUpdate.getName(), updatedModel.getName());
        assertEquals(brandForUpdate.getDateFoundation(), updatedModel.getDateFoundation());

        assertEquals(brandForUpdate.getModels().getFirst().getName(), updatedModel.getModels().getFirst().getName());
        assertEquals(brandForUpdate.getModels().getFirst().getBody(), updatedModel.getModels().getFirst().getBody());
        assertEquals(brandForUpdate.getModels().getFirst().getWidth(), updatedModel.getModels().getFirst().getWidth());
        assertEquals(brandForUpdate.getModels().getFirst().getLength(), updatedModel.getModels().getFirst().getLength());

        assertNotEquals(brandCar.getModels().getFirst(), updatedModel.getModels().getFirst());

        brandForUpdate.getModels().getFirst().setId(0);
        service.update(brandForUpdate);
        assertNotEquals(brandForUpdate.getModels().getFirst().getId(), updatedModel.getModels().getFirst().getId());
        assertEquals(brandForUpdate.getModels().getFirst().getName(), updatedModel.getModels().getFirst().getName());
        assertEquals(brandForUpdate.getModels().getFirst().getBody(), updatedModel.getModels().getFirst().getBody());
        assertEquals(brandForUpdate.getModels().getFirst().getWidth(), updatedModel.getModels().getFirst().getWidth());
        assertEquals(brandForUpdate.getModels().getFirst().getLength(), updatedModel.getModels().getFirst().getLength());
    }

    @Test
    @DisplayName("Проверка получения по id")
    void getById() {
        service.save(brands.get(0));
        service.save(brands.get(1));
        assertNotEquals(0, brands.getFirst().getId());
        assertNotEquals(0, brands.get(1).getId());

        assertEquals(brands.getFirst(), service.getById(brands.getFirst().getId()));
        assertEquals(brands.get(1), service.getById(brands.get(1).getId()));
        assertThrows(EntityNotFoundException.class, () -> service.getById(-1L));
    }

    @Test
    @DisplayName("Проверка получения всех записей")
    void getAll() {
        int allEntity = service.getAll().size();
        brands.forEach(task -> service.save(task));
        assertEquals(allEntity + brands.size(), service.getAll().size());
    }

    @Test
    @DisplayName("Проверка удаления по id")
    void deleteById() {
        service.save(brands.getFirst());
        service.deleteById(brands.getFirst().getId());
        assertThrows(EntityNotFoundException.class, () -> service.getById(brands.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления по объекту")
    void deleteByEntity() {
        service.save(brands.getFirst());
        service.deleteByEntity(brands.getFirst());
        assertThrows(EntityNotFoundException.class, () -> service.getById(brands.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(1000L));
    }

    @Test
    @DisplayName("Проверка удаления всех записей")
    void deleteAll() {

        for (int i = 0; i < brands.size(); i++) {
            brands.get(i).addModel(models.get(i));
            service.save(brands.get(i));
        }

        service.deleteAll();
        assertEquals(0, service.getAll().size());
    }

    @Test
    @DisplayName("Проверка получения первых 5 записей вложенной сущности")
    public void childEntity() {
        brands.getFirst().setModelsWithLinks(models);
        service.save(brands.getFirst());

        assertEquals(5, service.getRelatedEntityByParentId(brands.getFirst().getId()).size());
    }
}