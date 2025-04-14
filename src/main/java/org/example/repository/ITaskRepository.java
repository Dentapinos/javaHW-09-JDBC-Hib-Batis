package org.example.repository;

import org.example.entity.Employee;
import org.example.entity.Task;

import java.util.List;

public interface ITaskRepository extends EntityRepository<Task, Employee> {
    void save(Task task);

    void delete(long id);

    void deleteAll();

    void update(Task task);

    Task findById(long id);

    List<Task> findAll();

    List<Employee> getRelatedEntityByParentId(long id);
}
