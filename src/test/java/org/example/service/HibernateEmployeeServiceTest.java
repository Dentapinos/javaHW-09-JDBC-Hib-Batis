package org.example.service;

import org.example.entity.Employee;
import org.example.entity.Task;
import org.example.exception.EntityDeleteException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.EntitySaveException;
import org.example.repository.hibernate.HibernateEmployeeRepository;
import org.example.utils.EntityCreatorUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HIBERNATE: Тестирование сервиса Employee")
class HibernateEmployeeServiceTest {

    List<Employee> employees;
    List<Task> tasks;

    static EmployeeService service;

    @BeforeEach
    void setUp() {
        employees = EntityCreatorUtil.getEmployees(6);
        tasks = EntityCreatorUtil.getTasks(6);
    }

    @BeforeAll
    static void setUpAll() {
        HibernateEmployeeRepository employeeRepository = new HibernateEmployeeRepository();
        service = new EmployeeService(employeeRepository);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void save() {
        service.save(employees.get(0));
        service.save(employees.get(1));
        assertNotEquals(0, employees.get(0).getId());
        assertNotEquals(0, employees.get(1).getId());

        System.out.println();

        employees.get(2).setTaskWithLink(tasks.getFirst());
        service.save(employees.get(2));
        assertEquals(tasks.getFirst(), service.getById(employees.get(2).getId()).getTask());

        employees.get(3).setId(-1L);
        assertThrows(EntitySaveException.class, () -> service.save(employees.get(3)));

        employees.get(3).setId(1L);
        assertThrows(EntitySaveException.class, () -> service.save(employees.get(3)));

        assertThrows(EntitySaveException.class, () -> service.save(employees.get(0)));
    }

    @Test
    @DisplayName("Проверка обновления")
    void update() {
        Employee employee = Employee.builder()
                .name("Вася")
                .birthDate(LocalDate.of(2010, 1, 1))
                .build();

        employee.setTaskWithLink(tasks.getFirst());
        service.save(employee);

        Employee employeeFromDB = service.getById(employee.getId());
        employeeFromDB.setName("Updated");
        employeeFromDB.setBirthDate(LocalDate.of(2011, 12, 21));

        tasks.get(1).setId(employeeFromDB.getTask().getId());

        employeeFromDB.setTaskWithLink(tasks.get(1));
        service.update(employeeFromDB);

        Employee newEmployeeFromDB = service.getById(employeeFromDB.getId());
        assertEquals(employeeFromDB.getName(), newEmployeeFromDB.getName());
        assertEquals(employeeFromDB.getBirthDate(), newEmployeeFromDB.getBirthDate());
        assertEquals(employeeFromDB.getTask(), newEmployeeFromDB.getTask());

        assertNotEquals(employee.getName(), newEmployeeFromDB.getName());
        assertNotEquals(employee.getBirthDate(), newEmployeeFromDB.getBirthDate());
        assertNotEquals(employee.getTask(), newEmployeeFromDB.getTask());

        employeeFromDB.getTask().setId(0);
        service.update(employeeFromDB);
        assertNotEquals(employeeFromDB.getTask().getId(), newEmployeeFromDB.getTask().getId());
        assertEquals(employeeFromDB.getTask().getName(), newEmployeeFromDB.getTask().getName());
        assertEquals(employeeFromDB.getTask().getDescription(), newEmployeeFromDB.getTask().getDescription());
        assertEquals(employeeFromDB.getTask().getType(), newEmployeeFromDB.getTask().getType());
        assertEquals(employeeFromDB.getTask().getDeadline(), newEmployeeFromDB.getTask().getDeadline());
    }

    @Test
    @DisplayName("Проверка получения по id")
    void getById() {
        service.save(employees.get(0));
        service.save(employees.get(1));
        assertNotEquals(0, employees.get(0).getId());
        assertNotEquals(0, employees.get(1).getId());

        assertEquals(employees.get(0), service.getById(employees.get(0).getId()));
        assertEquals(employees.get(1), service.getById(employees.get(1).getId()));
        assertThrows(EntityNotFoundException.class, () -> service.getById(-1L));
    }

    @Test
    @DisplayName("Проверка получения всех записей")
    void getAll() {
        int allEntity = service.getAll().size();
        employees.forEach(task -> service.save(task));
        assertEquals(allEntity + employees.size(), service.getAll().size());
    }

    @Test
    @DisplayName("Проверка удаления по id")
    void deleteById() {
        service.save(employees.getFirst());
        service.deleteById(employees.getFirst().getId());
        assertThrows(EntityNotFoundException.class, () -> service.getById(employees.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления по объекту")
    void deleteByEntity() {
        service.save(employees.getFirst());
        service.deleteByEntity(employees.getFirst());
        assertThrows(EntityNotFoundException.class, () -> service.getById(employees.getFirst().getId()));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(-1L));
        assertThrows(EntityDeleteException.class, () -> service.deleteById(100L));
    }

    @Test
    @DisplayName("Проверка удаления всех записей")
    void deleteAll() {

        for (int i = 0; i < employees.size(); i++) {
            employees.get(i).setTaskWithLink(tasks.get(i));
            service.save(employees.get(i));
        }

        service.deleteAll();
        assertEquals(0, service.getAll().size());
    }

    @Test
    @DisplayName("Проверка получения 1 записи вложенной сущности")
    public void childEntity() {
        employees.getFirst().setTaskWithLink(tasks.getFirst());
        service.save(employees.getFirst());

        assertEquals(1, service.getRelatedEntityByParentId(employees.getFirst().getId()).size());
    }
}