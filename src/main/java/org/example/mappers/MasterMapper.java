package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Kitty;
import org.example.entity.Master;

import java.util.List;

@Mapper
public interface MasterMapper {

    void save(Master entity);

    List<Master> getAll();

    void deleteById(long id);

    void deleteByEntity(Master entity);

    void deleteAll();

    void update(Master entity);

    Master getById(long id);

    void saveMasterKittyRelation(@Param("m_id") long masterId, @Param("k_id") long kittyId);

    void deleteRelationByMasterId(long masterId);

    List<Kitty> getKittiesByMasterId(long masterId);

    void deleteAllRelation();

}
