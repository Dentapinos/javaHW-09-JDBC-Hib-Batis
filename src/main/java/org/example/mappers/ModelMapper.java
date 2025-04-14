package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface ModelMapper {
    ModelCar getById(long id) throws SQLException;

    void save(ModelCar model) throws SQLException;

    void update(ModelCar model) throws SQLException;

    BrandCar getBrandByModelId(long id);

    List<ModelCar> getAll() throws SQLException;

    void deleteById(long id) throws SQLException;

    void deleteAll() throws SQLException;
}
