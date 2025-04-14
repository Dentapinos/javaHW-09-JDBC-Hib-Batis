package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.House;
import org.example.entity.Street;

import java.util.List;

@Mapper
public interface StreetMapper {
    Street getById(long id);

    void save(Street street);

    void update(Street street);

    List<Street> getAll();

    List<House> getHousesByStreetId(long id);

    void deleteById(long id);

    void deleteHousesByStreetId(long id);

    void deleteByEntity(Street street);

    void deleteAll();

}
