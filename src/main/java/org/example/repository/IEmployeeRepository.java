package org.example.repository;

import org.example.entity.Employee;
import org.example.entity.Task;

import java.util.List;

public interface IEmployeeRepository extends EntityRepository<Employee, Task> {
    void save(Employee employee);

    void delete(long id);

    void deleteAll();

    void update(Employee employee);

    Employee findById(long id);

    List<Employee> findAll();

    List<Task> getRelatedEntityByParentId(long id);
}
