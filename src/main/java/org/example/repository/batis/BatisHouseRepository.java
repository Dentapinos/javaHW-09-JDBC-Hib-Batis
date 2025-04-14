package org.example.repository.batis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.example.configs.SessionManager;
import org.example.entity.House;
import org.example.entity.Street;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.mappers.HouseMapper;
import org.example.mappers.StreetMapper;
import org.example.repository.IHouseRepository;

import java.util.List;

@Slf4j
public class BatisHouseRepository implements IHouseRepository {

    public void save(House entity) throws EntitySaveException {
        if (entity == null) {
            log.error("Ошибка сохранения House null");
            throw new EntitySaveException("Ошибка сохранения House null");
        }
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }

        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            HouseMapper mapperHouse = session.getMapper(HouseMapper.class);
            StreetMapper mapperStreet = session.getMapper(StreetMapper.class);

            if (entity.getStreet() != null) {
                try {
                    mapperStreet.save(entity.getStreet());
                    log.info("Street сохранен: {}", entity.getStreet());
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "StreetMapper", e);
                }
            }
            try {
                mapperHouse.save(entity);
                session.commit();
            } catch (Exception e) {
                rollBackWitchMapperException(session, "HouseMapper", e);
            }
            log.info("House сохранен: {}", entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public House findById(long id) throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            HouseMapper mapperHouse = session.getMapper(HouseMapper.class);
            House loadedEntity = null;
            try {
                loadedEntity = mapperHouse.getById(id);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "HouseMapper", e);
            }
            if (loadedEntity == null) {
                log.warn("House с таким id не найдено: {}", id);
                throw new EntityNotFoundException("House c id=" + id + " не найден");
            }
            log.info("House найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения House по id={}", id, e);
            throw new RepositoryException("Ошибка получения House по id=" + id, e);
        }
    }

    public List<House> findAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            HouseMapper mapperHouse = session.getMapper(HouseMapper.class);
            List<House> house = mapperHouse.getAll();
            log.info("Получены все Houses: {}", house);
            return house;
        } catch (Exception e) {
            log.error("Ошибка получения всех Houses:", e);
            throw new RepositoryException("Ошибка получения всех Houses:", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            HouseMapper mapperHouse = session.getMapper(HouseMapper.class);

            House house = mapperHouse.getById(id);
            if (house != null) {
                try {
                    mapperHouse.deleteById(id);
                    session.commit();
                    log.info("House удалена: {}", house);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "HouseMapper", e);
                }
            } else {
                log.warn("House с id={} не найдена", id);
                throw new EntityNotFoundException("House с id=" + id + " не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка удаления House по id={} : {}", id, e.getMessage());
            throw new EntityDeleteException("Ошибка удаления House по id", e);
        }
    }

    public void update(House house) throws EntityUpdateException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            HouseMapper mapperHouse = session.getMapper(HouseMapper.class);
            StreetMapper mapperStreet = session.getMapper(StreetMapper.class);

            House loadedModel = mapperHouse.getById(house.getId());
            if (loadedModel != null) {
                try {
                    Street street = mapperStreet.getById(house.getStreet().getId());
                    if (street != null) {
                        mapperStreet.update(house.getStreet());
                        log.info("Street для House обновлен: {}", house.getStreet());
                    } else {
                        mapperStreet.save(house.getStreet());
                        log.info("Brand для House сохранен: {}", house.getStreet());
                    }
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "StreetMapper", e);
                }

                try {
                    mapperHouse.update(house);
                    session.commit();
                    log.info("House обновлена: {}", house);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "HouseMapper", e);
                }

            } else {
                log.warn("House с таким id не найдено: {}", house.getId());
                throw new EntityNotFoundException("House c id=" + house.getId() + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновления House {}:", house, e);
            throw new EntitySaveException("Ошибка обновления House", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            try {
                HouseMapper mapperHouse = session.getMapper(HouseMapper.class);
                mapperHouse.deleteAll();
                session.commit();
                log.info("Все Houses удалены");
            } catch (Exception e) {
                rollBackWitchMapperException(session, "HouseMapper", e);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления всех Houses:", e);
            throw new RepositoryException("Ошибка удаления всех Houses:", e);
        }
    }

    public List<Street> getRelatedEntityByParentId(long id) {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            HouseMapper mapperHouse = session.getMapper(HouseMapper.class);

            try {
                List<Street> street = mapperHouse.getStreetByHouseId(id);
                if (street != null) {
                    log.info("Street для House найден");
                    return street;
                }
            } catch (Exception e) {
                rollBackWitchMapperException(session, "StreetMapper", e);
            }
            log.warn("House с таким id не найдено: {}", id);
            throw new EntityNotFoundException("House c id=" + id + " не найден");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Brand для House: {}", e.getMessage());
            throw new RepositoryException("Ошибка получения Brand для House", e);
        }
    }

    private void rollBackWitchMapperException(SqlSession session, String mapperName, Exception e) throws MapperException {
        session.rollback();
        log.error("Ошибка в {}: {}", mapperName, e.getMessage());
        throw new MapperException("Ошибка в " + mapperName + " :", e);
    }

}