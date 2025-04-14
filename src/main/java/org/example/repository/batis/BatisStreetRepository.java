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
import org.example.repository.IStreetRepository;

import java.util.List;

@Slf4j
public class BatisStreetRepository implements IStreetRepository {

    public void save(Street entity) throws EntitySaveException {
        if (entity == null) {
            log.error("Ошибка сохранения Street null");
            throw new EntitySaveException("Ошибка сохранения Street null");
        }
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }

        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            HouseMapper houseMapper = session.getMapper(HouseMapper.class);
            StreetMapper streetMapper = session.getMapper(StreetMapper.class);

            try {
                streetMapper.save(entity);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "StreetMapper", e);
            }

            if (entity.getHouses() != null) {
                for (House house : entity.getHouses()) {
                    try {
                        House loadHouse = houseMapper.getById(house.getId());
                        if (loadHouse != null) {
                            houseMapper.update(house);
                            log.info("House для Street обновлен: {}", house);
                        } else {
                            houseMapper.save(house);
                            log.info("House для Street сохранен: {}", house);
                        }
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "HouseMapper", e);
                    }
                }
            }

            session.commit();
            log.info("House сохранен: {}", entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public Street findById(long id) throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            StreetMapper streetMapper = session.getMapper(StreetMapper.class);
            Street loadedEntity = null;
            try {
                loadedEntity = streetMapper.getById(id);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "StreetMapper", e);
            }
            if (loadedEntity == null) {
                log.warn("Street с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Street c id=" + id + " не найден");
            }
            log.info("Street найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Street по id={}", id, e);
            throw new RepositoryException("Ошибка получения Street по id=" + id, e);
        }
    }

    public List<Street> findAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            StreetMapper streetMapper = session.getMapper(StreetMapper.class);
            List<Street> streets = streetMapper.getAll();
            log.info("Получены все Streets: {}", streets);
            return streets;
        } catch (Exception e) {
            log.error("Ошибка получения всех Streets:", e);
            throw new RepositoryException("Ошибка получения всех Streets:", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            StreetMapper streetMapper = session.getMapper(StreetMapper.class);
            HouseMapper houseMapper = session.getMapper(HouseMapper.class);

            Street street = streetMapper.getById(id);
            if (street != null) {
                if (street.getHouses() != null) {
                    try {
                        for (House house : street.getHouses()) {
                            houseMapper.deleteById(house.getId());
                        }
                        log.info("Все Houses связанные с Street id={} удалены", id);
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "HouseMapper", e);
                    }
                }

                try {
                    streetMapper.deleteById(id);
                    session.commit();
                    log.info("Street удалена: {}", street);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "StreetMapper", e);
                }
            } else {
                log.warn("Street с id={} не найдена", id);
                throw new EntityNotFoundException("Street с id=" + id + " не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Street по id={} : {}", id, e.getMessage());
            throw new EntityDeleteException("Ошибка удаления Street по id", e);
        }
    }

    public void update(Street street) throws EntityUpdateException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            HouseMapper houseMapper = session.getMapper(HouseMapper.class);
            StreetMapper streetMapper = session.getMapper(StreetMapper.class);

            Street loadedBrand = streetMapper.getById(street.getId());
            if (loadedBrand != null) {
                try {
                    streetMapper.update(street);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "StreetMapper", e);
                }

                try {
                    for (House house : street.getHouses()) {
                        House loadHouse = houseMapper.getById(house.getId());
                        house.setStreet(street);
                        if (loadHouse != null) {
                            houseMapper.update(house);
                            log.info("House для Street обновлен: {}", house);
                        } else {
                            houseMapper.save(house);
                            log.info("House для Street сохранен: {}", house);
                        }
                    }
                    session.commit();
                    log.info("Street обновлена: {}", street);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "HouseMapper", e);
                }

            } else {
                log.warn("Street с таким id не найдено: {}", street.getId());
                throw new EntityNotFoundException("Street c id=" + street.getId() + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновления Street {}:", street, e);
            throw new EntitySaveException("Ошибка обновления Street", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            StreetMapper streetMapper = session.getMapper(StreetMapper.class);
            HouseMapper houseMapper = session.getMapper(HouseMapper.class);
            try {
                houseMapper.deleteAll();
                streetMapper.deleteAll();
                session.commit();
                log.info("Все Streets удалены вместе со всеми Houses");
            } catch (Exception e) {
                rollBackWitchMapperException(session, "StreetMapper", e);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления всех BrandsCar:", e);
            throw new RepositoryException("Ошибка удаления всех BrandsCar:", e);
        }
    }

    public List<House> getRelatedEntityByParentId(long id) {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            StreetMapper mapper = session.getMapper(StreetMapper.class);

            try {
                List<House> houses = mapper.getHousesByStreetId(id);
                if (!houses.isEmpty()) {
                    log.info("Houses для Street найдены");
                    return houses;
                }
            } catch (Exception e) {
                rollBackWitchMapperException(session, "HouseMapper", e);
            }
            log.warn("Street с таким id не найдено: {}", id);
            throw new EntityNotFoundException("Street c id=" + id + " не найден");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения House для Street: {}", e.getMessage());
            throw new RepositoryException("Ошибка получения House для Street", e);
        }
    }

    private void rollBackWitchMapperException(SqlSession session, String mapperName, Exception e) throws MapperException {
        session.rollback();
        log.error("Ошибка в {}: {}", mapperName, e.getMessage());
        throw new MapperException("Ошибка в " + mapperName + " :", e);
    }

}