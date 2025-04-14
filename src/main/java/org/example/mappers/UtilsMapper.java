package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UtilsMapper {
    void resetTableAutoIncrement(String tableName);

    void clearTableByName(String tableName);
}
