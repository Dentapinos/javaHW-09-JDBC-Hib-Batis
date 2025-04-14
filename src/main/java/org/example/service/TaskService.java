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
public class TaskService {

    private EntityRepository<Task, Employee> repository;

    public Task save(Task task) {
        log.info("Сохранение Task");
        try {
            repository.save(task);
            log.info("{} сохранен: {}", task.getClass().getSimpleName(), task);
            return task;
        } catch (EntitySaveException e) {
            log.error("Ошибка сохранения Task:", e);
            throw e;
        }
    }

    public Task getById(long id) {
        log.info("Получение Task по id={}", id);
        try {
            Task task = repository.findById(id);
            log.info("Task получена: {}", task);
            return task;
        } catch (RepositoryException e) {
            log.error("Task c id={} не найдена:", id, e);
            throw e;
        }
    }

    public List<Task> getAll() {
        log.info("Получение всех Tasks");
        try {
            List<Task> tasks = repository.findAll();
            log.info("Все Tasks получены: {}", tasks);
            return tasks;
        } catch (RepositoryException e) {
            log.error("Ошибка получения всех Tasks", e);
            throw e;
        }
    }

    public void deleteById(long id) {
        log.info("Удаление Task по id={}", id);
        try {
            checkingValueIsLessThanZero(id);
            repository.delete(id);
            log.info("Task удалена по id={}", id);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Task по id={}:", id, e);
            throw e;
        }
    }

    public void deleteByEntity(Task task) {
        log.info("Удаление Task по по объекту {}", task);
        try {
            checkingValueIsLessThanZero(task.getId());
            repository.delete(task.getId());
            log.info("Task удалена по объекту {}", task);
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления Task по объекту {}:", task, e);
            throw e;
        }
    }

    private void checkingValueIsLessThanZero(long id) throws EntityDeleteException {
        if (id < 0) {
            throw new EntityDeleteException("id не может быть меньше 0");
        }
    }

    public void deleteAll() {
        log.info("Удаление всех Tasks");
        try {
            repository.deleteAll();
            log.info("Все Tasks удалены");
        } catch (EntityDeleteException e) {
            log.error("Ошибка удаления всех Tasks:", e);
            throw e;
        }
    }

    public Task update(Task task) {
        log.info("Обновление Task: {}", task);
        try {
            Task loadedTask = repository.findById(task.getId());
            if (loadedTask != null) {
                repository.update(task);
                log.info("Task обновлен: {}", loadedTask);
                return loadedTask;
            } else {
                log.warn("Task с таким id={} не найден", task.getId());
                throw new EntityNotFoundException("Task с таким id=" + task.getId() + " не найден");
            }
        } catch (EntityUpdateException e) {
            log.error("Ошибка обновления Task: {}", task, e);
            throw e;
        }
    }

    public List<Employee> getRelatedEntityByParentId(long id) {
        log.info("Получение связанных объектов Employee по Task id={}", id);
        try {
            List<Employee> employeeList = repository.getRelatedEntityByParentId(id);
            log.info("Связанные объекты Employee для Task получены: {}", employeeList.size());
            return employeeList;
        } catch (EntityNotFoundException e) {
            log.error("Ошибка получения Employee для Employee: ", e);
            throw e;
        }
    }
}
