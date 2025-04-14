package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Employee;
import org.example.entity.Task;
import org.example.exception.*;
import org.example.repository.EntityRepository;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class EmployeeService {

    private EntityRepository<Employee, Task> repository;

    public Employee save(Employee employee) {
        log.info("Сохранение {}", employee.getClass().getSimpleName());
        try {
            repository.save(employee);
            log.info("{} сохранен: {}", employee.getClass().getSimpleName(), employee);
            return employee;
        } catch (EntitySaveException e) {
            log.error("Ошибка сохранения {}:", employee.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public void deleteById(long id) {
        log.info("Удаление Employee по id={}", id);
        try {
            checkingValueIsLessThanZero(id);
            repository.delete(id);
            log.info("Employee удалена по id={}", id);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Employee по id={}:", id, e);
            throw e;
        }
    }

    public void deleteByEntity(Employee tmployee) {
        log.info("Удаление Employee по по объекту {}", tmployee);
        try {
            checkingValueIsLessThanZero(tmployee.getId());
            repository.delete(tmployee.getId());
            log.info("Employee удалена по объекту {}", tmployee);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Employee по объекту {}:", tmployee, e);
            throw e;
        }
    }

    private void checkingValueIsLessThanZero(long id) throws EntityDeleteException {
        if (id < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
    }

    public void deleteAll() {
        log.info("Удаление всех Employees");
        try {
            repository.deleteAll();
            log.info("Все Employees удалены");
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления всех Employees:", e);
            throw e;
        }
    }

    public Employee update(Employee employee) {
        log.info("Обновление Employee: {}", employee);
        try {
            Employee loadedTask = repository.findById(employee.getId());
            if (loadedTask != null) {
                repository.update(employee);
                log.info("Employee обновлен: {}", loadedTask);
                return loadedTask;
            } else {
                log.warn("Employee с таким id={} не найден", employee.getId());
                throw new EntityNotFoundException("Employee с таким id=" + employee.getId() + " не найден");
            }
        } catch (EntityUpdateException e) {
            log.error("Ошибка обновления Employee: {}", employee, e);
            throw e;
        }
    }

    public Employee getById(long id) {
        log.info("Получение Employee по id={}", id);
        try {
            Employee employee = repository.findById(id);
            log.info("Employee получена: {}", employee);
            return employee;
        } catch (RepositoryException e) {
            log.error("Employee c id={} не найдена:", id, e);
            throw e;
        }
    }

    public List<Employee> getAll() {
        log.info("Получение всех Employees");
        try {
            List<Employee> tmployees = repository.findAll();
            log.info("Все Employees получены: {}", tmployees);
            return tmployees;
        } catch (RepositoryException e) {
            log.error("Ошибка получения всех Employees", e);
            throw e;
        }
    }

    public List<Task> getRelatedEntityByParentId(long id) {
        log.info("Получение связанных объектов Task по Employee id={}", id);
        try {
            List<Task> employeeList = repository.getRelatedEntityByParentId(id);
            log.info("Связанные объекты Task для Employee получены: {}", employeeList.size());
            return employeeList;
        } catch (EntityNotFoundException e) {
            log.error("Ошибка получения Task для Employee: ", e);
            throw e;
        }
    }
}
