package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.House;
import org.example.entity.Street;

import java.util.List;

@Mapper
public interface HouseMapper {
    House getById(long id);

    void save(House house);

    void update(House house);

    List<House> getAll();

    List<Street> getStreetByHouseId(long id);

    void deleteById(@Param("id_house") long id);

    void deleteByEntity(House house);

    void deleteAll();
}
