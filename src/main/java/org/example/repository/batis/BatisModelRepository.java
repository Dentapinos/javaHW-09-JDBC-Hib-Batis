package org.example.repository.batis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.example.configs.SessionManager;
import org.example.entity.BrandCar;
import org.example.entity.ModelCar;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.mappers.BrandMapper;
import org.example.mappers.ModelMapper;
import org.example.repository.IModelRepository;

import java.util.List;

@Slf4j
public class BatisModelRepository implements IModelRepository {

    public void save(ModelCar entity) throws EntitySaveException {
        if (entity == null) {
            log.error("Ошибка сохранения ModelCar null");
            throw new EntitySaveException("Ошибка сохранения ModelCar null");
        }
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }

        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            ModelMapper mapperModel = session.getMapper(ModelMapper.class);
            BrandMapper mapperBrand = session.getMapper(BrandMapper.class);

            if (entity.getBrand() != null) {
                try {
                    mapperBrand.save(entity.getBrand());
                    log.info("Employee сохранен: {}", entity.getBrand());
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "BrandMapper", e);
                }
            }
            try {
                mapperModel.save(entity);
                session.commit();
            } catch (Exception e) {
                rollBackWitchMapperException(session, "ModelMapper", e);
            }
            log.info("ModelCar сохранен: {}", entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public ModelCar findById(long id) throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            ModelMapper mapperModel = session.getMapper(ModelMapper.class);
            ModelCar loadedEntity = null;
            try {
                loadedEntity = mapperModel.getById(id);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "ModelMapper", e);
            }
            if (loadedEntity == null) {
                log.warn("ModelCar с таким id не найдено: {}", id);
                throw new EntityNotFoundException("ModelCar c id=" + id + " не найден");
            }
            log.info("ModelCar найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения ModelCar по id={}", id, e);
            throw new RepositoryException("Ошибка получения ModelCar по id=" + id, e);
        }
    }

    public List<ModelCar> findAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            ModelMapper mapperModel = session.getMapper(ModelMapper.class);
            List<ModelCar> model = mapperModel.getAll();
            log.info("Получены все Models: {}", model);
            return model;
        } catch (Exception e) {
            log.error("Ошибка получения всех Models:", e);
            throw new RepositoryException("Ошибка получения всех Models:", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            ModelMapper mapperModel = session.getMapper(ModelMapper.class);

            ModelCar model = mapperModel.getById(id);
            if (model != null) {
                try {
                    mapperModel.deleteById(id);
                    session.commit();
                    log.info("ModelCar удалена: {}", model);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "ModelMapper", e);
                }
            } else {
                log.warn("ModelCar с id={} не найдена", id);
                throw new EntityNotFoundException("ModelCar с id=" + id + " не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка удаления ModelCar по id={} : {}", id, e.getMessage());
            throw new EntityDeleteException("Ошибка удаления ModelCar по id", e);
        }
    }

    public void update(ModelCar model) throws EntityUpdateException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            ModelMapper mapperModel = session.getMapper(ModelMapper.class);
            BrandMapper mapperBrand = session.getMapper(BrandMapper.class);

            ModelCar loadedModel = mapperModel.getById(model.getId());
            if (loadedModel != null) {
                try {
                    BrandCar brand = mapperBrand.getById(model.getBrand().getId());
                    if (brand != null) {
                        mapperBrand.update(model.getBrand());
                        log.info("Brand для ModelCar обновлен: {}", model.getBrand());
                    } else {
                        mapperBrand.save(model.getBrand());
                        log.info("Brand для ModelCar сохранен: {}", model.getBrand());
                    }
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "BrandMapper", e);
                }

                try {
                    mapperModel.update(model);
                    session.commit();
                    log.info("ModelCar обновлена: {}", model);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "ModelMapper", e);
                }

            } else {
                log.warn("ModelCar с таким id не найдено: {}", model.getId());
                throw new EntityNotFoundException("ModelCar c id=" + model.getId() + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновления ModelCar {}:", model, e);
            throw new EntitySaveException("Ошибка обновления ModelCar", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            try {
                ModelMapper mapperModel = session.getMapper(ModelMapper.class);
                mapperModel.deleteAll();
                session.commit();
                log.info("Все Models удалены");
            } catch (Exception e) {
                rollBackWitchMapperException(session, "ModelMapper", e);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления всех Models:", e);
            throw new RepositoryException("Ошибка удаления всех Models:", e);
        }
    }

    public List<BrandCar> getRelatedEntityByParentId(long id) {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            ModelMapper mapperModel = session.getMapper(ModelMapper.class);

            try {
                BrandCar brand = mapperModel.getBrandByModelId(id);
                if (brand != null) {
                    log.info("Brand для ModelCar найден");
                    return List.of(brand);
                }
            } catch (Exception e) {
                rollBackWitchMapperException(session, "BrandMapper", e);
            }
            log.warn("ModelCar с таким id не найдено: {}", id);
            throw new EntityNotFoundException("ModelCar c id=" + id + " не найден");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Brand для ModelCar: {}", e.getMessage());
            throw new RepositoryException("Ошибка получения Brand для ModelCar", e);
        }
    }

    private void rollBackWitchMapperException(SqlSession session, String mapperName, Exception e) throws MapperException {
        session.rollback();
        log.error("Ошибка в {}: {}", mapperName, e.getMessage());
        throw new MapperException("Ошибка в " + mapperName + " :", e);
    }

}