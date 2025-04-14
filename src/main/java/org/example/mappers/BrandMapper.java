package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface BrandMapper {
    BrandCar getById(long id) throws SQLException;

    void save(BrandCar brand) throws SQLException;

    void update(BrandCar brand) throws SQLException;

    List<ModelCar> getModelsByBrandId(long id);

    List<BrandCar> getAll() throws SQLException;

    void deleteById(long id) throws SQLException;

    void deleteAll() throws SQLException;
}
