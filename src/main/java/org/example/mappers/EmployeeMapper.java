package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.Employee;
import org.example.entity.Task;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface EmployeeMapper {
    Employee getById(long id) throws SQLException;

    void save(Employee employee) throws SQLException;

    void update(Employee employee) throws SQLException;

    Task getTaskByEmployeeId(long id);

    List<Employee> getAll() throws SQLException;

    void deleteById(long id) throws SQLException;

    void deleteAll() throws SQLException;

    void deleteAllRelation();
}
