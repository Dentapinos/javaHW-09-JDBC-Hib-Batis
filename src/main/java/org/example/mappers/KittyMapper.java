package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Kitty;
import org.example.entity.Master;

import java.util.List;

@Mapper
public interface KittyMapper {

    void save(Kitty entity);

    void deleteById(long id);

    void deleteByEntity(Kitty entity);

    void deleteAll();

    void update(Kitty entity);

    Kitty getById(long id);

    List<Kitty> getAll();

    void saveMasterKittyRelation(@Param("m_id") long masterId, @Param("k_id") long kittyId);

    void deleteRelationByKittyId(long kittyId);

    List<Master> getMastersByKittyId(long kittyId);

    void deleteAllRelation();
}
