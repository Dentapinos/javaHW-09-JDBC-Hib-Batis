package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.Employee;
import org.example.entity.Task;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface TaskMapper {
    void save(Task task) throws SQLException;

    Task getById(long id) throws SQLException;

    void update(Task task) throws SQLException;

    void deleteAll() throws SQLException;

    List<Task> getAll() throws SQLException;

    void deleteById(long id) throws SQLException;

    Employee getEmployeeByTaskId(long employeeId) throws SQLException;


}
