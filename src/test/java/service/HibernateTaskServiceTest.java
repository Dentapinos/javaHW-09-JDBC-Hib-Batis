package service;

import org.example.entity.Employee;
import org.example.entity.Task;
import org.example.enums.TypeTask;
import org.example.exception.EntityDeleteException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.EntitySaveException;
import org.example.repository.hibernate.HibernateTaskRepository;
import org.example.service.TaskService;
import org.example.utils.EntityCreatorUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HIBERNATE: Тестирование сервиса Task")
class HibernateTaskServiceTest {

    List<Task> tasks;
    List<Employee> employees;

    static TaskService service;

    @BeforeEach
    void setUp() {
        tasks = EntityCreatorUtil.getTasks(5);
        employees = EntityCreatorUtil.getEmployees(5);
    }

    @BeforeAll
    static void setUpAll() {
        HibernateTaskRepository taskRepository = new HibernateTaskRepository();
        service = new TaskService(taskRepository);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void save() {
        service.save(tasks.get(0));
        service.save(tasks.get(1));
        assertNotEquals(0, tasks.get(0).getId());
        assertNotEquals(0, tasks.get(1).getId());

        tasks.get(2).setEmployee(employees.getFirst());
        service.save(tasks.get(2));
        assertEquals(employees.getFirst(), service.getById(tasks.get(2).getId()).getEmployee());

        tasks.get(3).setId(-1L);
        assertThrows(EntitySaveException.class, () -> service.save(tasks.get(3)));

        tasks.get(3).setId(1L);
        assertThrows(EntitySaveException.class, () -> service.save(tasks.get(3)));

        assertThrows(EntitySaveException.class, () -> service.save(tasks.getFirst()));
    }

    @Test
    @DisplayName("Проверка обновления")
    void update() {
        Task task = Task.builder()
                .name("Имя задачи")
                .description("Описание задачи")
                .deadline(LocalDate.of(2000, 1, 1))
                .type(TypeTask.ANALYTICS)
                .employee(employees.getFirst())
                .build();
        service.save(task);

        Task taskFromDB = service.getById(task.getId());
        taskFromDB.setName("Updated");
        taskFromDB.setType(TypeTask.BUG);
        taskFromDB.setDescription("Updated Description");
        taskFromDB.setDeadline(EntityCreatorUtil.generateRandomDateDeadline());
        employees.get(1).setId(taskFromDB.getEmployee().getId());
        taskFromDB.setEmployee(employees.get(1));
        service.update(taskFromDB);

        Task newTaskFromDB = service.getById(taskFromDB.getId());
        assertEquals(taskFromDB.getName(), newTaskFromDB.getName());
        assertEquals(taskFromDB.getType(), newTaskFromDB.getType());
        assertEquals(taskFromDB.getDescription(), newTaskFromDB.getDescription());
        assertEquals(taskFromDB.getDeadline(), newTaskFromDB.getDeadline());
        assertEquals(taskFromDB.getEmployee(), newTaskFromDB.getEmployee());

        assertNotEquals(task.getName(), newTaskFromDB.getName());
        assertNotEquals(task.getType(), newTaskFromDB.getType());
        assertNotEquals(task.getDescription(), newTaskFromDB.getDescription());
        assertNotEquals(task.getDeadline(), newTaskFromDB.getDeadline());
        assertNotEquals(task.getEmployee(), newTaskFromDB.getEmployee());

        taskFromDB.getEmployee().setId(0);
        service.update(taskFromDB);
        assertNotEquals(taskFromDB.getEmployee().getId(), newTaskFromDB.getEmployee().getId());
        assertEquals(taskFromDB.getEmployee().getName(), newTaskFromDB.getEmployee().getName());
        assertEquals(taskFromDB.getEmployee().getBirthDate(), newTaskFromDB.getEmployee().getBirthDate());
    }

    @Test
    @DisplayName("Проверка получения по id")
    void getById() {
        service.save(tasks.get(0));
        service.save(tasks.get(1));
        assertNotEquals(0, tasks.get(0).getId());
        assertNotEquals(0, tasks.get(1).getId());

        assertEquals(tasks.get(0), service.getById(tasks.get(0).getId()));
        assertEquals(tasks.get(1), service.getById(tasks.get(1).getId()));
        assertThrows(EntityNotFoundException.class, () -> service.getById(-1L));
    }

    @Test
    @DisplayName("Проверка получения всех записей")
    void getAll() {
        int allEntity = service.getAll().size();
        tasks.forEach(task -> service.save(task));
        assertEquals(allEntity + tasks.size(), service.getAll().size());
    }

    @Test
    @DisplayName("Проверка удаления по id")
    void deleteById() {
        service.save(tasks.getFirst());
        service.deleteById(tasks.getFirst().getId());
        assertThrows(EntityNotFoundException.class, () -> service.getById(tasks.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления по объекту")
    void deleteByEntity() {
        service.save(tasks.getFirst());
        service.deleteByEntity(tasks.getFirst());
        assertThrows(EntityNotFoundException.class, () -> service.getById(tasks.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления всех записей")
    void deleteAll() {
        tasks.forEach(task -> service.save(task));
        service.deleteAll();
        assertEquals(0, service.getAll().size());
    }

    @Test
    @DisplayName("Проверка получения первых 5 записей вложенной сущности")
    public void childEntity() {
        tasks.getFirst().setEmployee(employees.getFirst());
        service.save(tasks.getFirst());

        assertEquals(1, service.getRelatedEntityByParentId(tasks.getFirst().getId()).size());
    }
}