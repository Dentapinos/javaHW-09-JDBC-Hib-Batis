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
import org.example.repository.IBrandRepository;

import java.util.List;

@Slf4j
public class BatisBrandRepository implements IBrandRepository {

    public void save(BrandCar entity) throws EntitySaveException {
        if (entity == null) {
            log.error("Ошибка сохранения BrandCar null");
            throw new EntitySaveException("Ошибка сохранения BrandCar null");
        }
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }

        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            ModelMapper modelMapper = session.getMapper(ModelMapper.class);
            BrandMapper brandMapper = session.getMapper(BrandMapper.class);

            try {
                brandMapper.save(entity);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "BrandCar", e);
            }

            if (entity.getModels() != null) {

                for (ModelCar modelCar : entity.getModels()) {
                    try {
                        ModelCar model = modelMapper.getById(modelCar.getId());
                        if (model != null) {
                            modelMapper.update(modelCar);
                            log.info("ModelCar для BrandCar обновлен: {}", model);
                        } else {
                            modelMapper.save(modelCar);
                            log.info("ModelCar для BrandCar сохранен: {}", entity.getModels());
                        }
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "ModelCarMapper", e);
                    }
                }
            }

            session.commit();
            log.info("ModelCar сохранен: {}", entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public BrandCar findById(long id) throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            BrandMapper brandMapper = session.getMapper(BrandMapper.class);
            BrandCar loadedEntity = null;
            try {
                loadedEntity = brandMapper.getById(id);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "BrandMapper", e);
            }
            if (loadedEntity == null) {
                log.warn("BrandCar с таким id не найдено: {}", id);
                throw new EntityNotFoundException("BrandCar c id=" + id + " не найден");
            }
            log.info("BrandCar найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения BrandCar по id={}", id, e);
            throw new RepositoryException("Ошибка получения BrandCar по id=" + id, e);
        }
    }

    public List<BrandCar> findAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            BrandMapper brandMapper = session.getMapper(BrandMapper.class);
            List<BrandCar> brandsCar = brandMapper.getAll();
            log.info("Получены все BrandsCar: {}", brandsCar);
            return brandsCar;
        } catch (Exception e) {
            log.error("Ошибка получения всех BrandsCar:", e);
            throw new RepositoryException("Ошибка получения всех BrandsCar:", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            BrandMapper brandMapper = session.getMapper(BrandMapper.class);
            ModelMapper modelMapper = session.getMapper(ModelMapper.class);

            BrandCar brand = brandMapper.getById(id);
            if (brand != null) {
                if (brand.getModels() != null) {
                    try {
                        for (ModelCar modelCar : brand.getModels()) {
                            modelMapper.deleteById(modelCar.getId());
                        }
                        log.info("Все ModelsCar связанные с BrandCar id={} удалены", id);
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "BrandMapper", e);
                    }
                }

                try {
                    brandMapper.deleteById(id);
                    session.commit();
                    log.info("BrandCar удалена: {}", brand);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "BrandMapper", e);
                }
            } else {
                log.warn("BrandCar с id={} не найдена", id);
                throw new EntityNotFoundException("BrandCar с id=" + id + " не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка удаления BrandCar по id={} : {}", id, e.getMessage());
            throw new EntityDeleteException("Ошибка удаления BrandCar по id", e);
        }
    }

    public void update(BrandCar brand) throws EntityUpdateException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            ModelMapper modelMapper = session.getMapper(ModelMapper.class);
            BrandMapper brandMapper = session.getMapper(BrandMapper.class);

            BrandCar loadedBrand = brandMapper.getById(brand.getId());
            if (loadedBrand != null) {
                try {
                    brandMapper.update(brand);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "BrandMapper", e);
                }

                try {
                    for (ModelCar modelCar : brand.getModels()) {
                        ModelCar model = modelMapper.getById(modelCar.getId());
                        modelCar.setBrand(brand);
                        if (model != null) {
                            modelMapper.update(modelCar);
                            log.info("ModelCar для BrandCar обновлен: {}", modelCar);
                        } else {
                            modelMapper.save(modelCar);
                            log.info("ModelCar для BrandCar сохранен: {}", modelCar);
                        }
                    }
                    session.commit();
                    log.info("BrandCar обновлена: {}", brand);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "ModelMapper", e);
                }

            } else {
                log.warn("BrandCar с таким id не найдено: {}", brand.getId());
                throw new EntityNotFoundException("BrandCar c id=" + brand.getId() + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновления BrandCar {}:", brand, e);
            throw new EntitySaveException("Ошибка обновления BrandCar", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            BrandMapper brandMapper = session.getMapper(BrandMapper.class);
            ModelMapper modelMapper = session.getMapper(ModelMapper.class);
            try {
                modelMapper.deleteAll();
                brandMapper.deleteAll();
                session.commit();
                log.info("Все BrandsCar удалены вместе со всеми ModelCar");
            } catch (Exception e) {
                rollBackWitchMapperException(session, "BrandMapper", e);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления всех BrandsCar:", e);
            throw new RepositoryException("Ошибка удаления всех BrandsCar:", e);
        }
    }

    public List<ModelCar> getRelatedEntityByParentId(long id) {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            BrandMapper mapper = session.getMapper(BrandMapper.class);

            try {
                List<ModelCar> models = mapper.getModelsByBrandId(id);
                if (!models.isEmpty()) {
                    log.info("ModelCar для BrandCar найдены");
                    return models;
                }
            } catch (Exception e) {
                rollBackWitchMapperException(session, "ModelMapper", e);
            }
            log.warn("BrandCar с таким id не найдено: {}", id);
            throw new EntityNotFoundException("BrandCar c id=" + id + " не найден");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения ModelCar для BrandCar: {}", e.getMessage());
            throw new RepositoryException("Ошибка получения ModelCar для BrandCar", e);
        }
    }

    private void rollBackWitchMapperException(SqlSession session, String mapperName, Exception e) throws MapperException {
        session.rollback();
        log.error("Ошибка в {}: {}", mapperName, e.getMessage());
        throw new MapperException("Ошибка в " + mapperName + " :", e);
    }

}