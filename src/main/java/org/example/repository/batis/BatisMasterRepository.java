package org.example.repository.batis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.example.configs.SessionManager;
import org.example.entity.Kitty;
import org.example.entity.Master;
import org.example.enums.SessionName;
import org.example.exception.*;
import org.example.mappers.KittyMapper;
import org.example.mappers.MasterMapper;
import org.example.repository.IMasterRepository;

import java.util.List;

@Slf4j
public class BatisMasterRepository implements IMasterRepository {

    public void save(Master entity) throws EntitySaveException {
        if (entity == null) {
            log.error("Ошибка сохранения Master null");
            throw new EntitySaveException("Ошибка сохранения Master null");
        }
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }

        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            MasterMapper mapperMaster = session.getMapper(MasterMapper.class);
            KittyMapper kittyMapper = session.getMapper(KittyMapper.class);

            try {
                mapperMaster.save(entity);
                log.info("Master сохранен: {}", entity);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "MasterMapper", e);
            }

            if (entity.getKitties() != null) {
                for (Kitty kitty : entity.getKitties()) {
                    try {
                        Kitty loadMaster = kittyMapper.getById(kitty.getId());
                        if (loadMaster != null) {
                            kittyMapper.update(kitty);
                            log.info("Kitty для Master обновлен: {}", kitty);
                        } else {
                            kittyMapper.save(kitty);
                            log.info("Kitty для Master сохранен: {}", kitty);
                        }
                        mapperMaster.saveMasterKittyRelation(entity.getId(), kitty.getId());
                        session.commit();
                        log.info("Связь Kitty с Master установлена: {}", kitty);
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "MasterMapper", e);
                    }
                }
            }
            session.commit();
            log.info("Master сохранен: {}", entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public Master findById(long id) throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            MasterMapper mapperMaster = session.getMapper(MasterMapper.class);
            Master loadedEntity = null;
            try {
                loadedEntity = mapperMaster.getById(id);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "MasterMapper", e);
            }
            if (loadedEntity == null) {
                log.warn("Master с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Master c id=" + id + " не найден");
            }
            log.info("Master найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Master по id={}", id, e);
            throw new RepositoryException("Ошибка получения Master по id=" + id, e);
        }
    }

    public List<Master> findAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            MasterMapper mapperMaster = session.getMapper(MasterMapper.class);
            List<Master> master = mapperMaster.getAll();
            log.info("Получены все Masters: {}", master);
            return master;
        } catch (Exception e) {
            log.error("Ошибка получения всех Masters:", e);
            throw new RepositoryException("Ошибка получения всех Masters:", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            MasterMapper mapperMaster = session.getMapper(MasterMapper.class);

            Master master = mapperMaster.getById(id);
            if (master != null) {
                try {
                    mapperMaster.deleteRelationByMasterId(master.getId());
                    log.info("Попытка удаления связей Master id={}", master.getId());
                    mapperMaster.deleteById(id);
                    session.commit();
                    log.info("Связи Master id={} удалены: {}", master.getId(), master);
                    log.info("Master удалена: {}", master);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "MasterMapper", e);
                }
            } else {
                log.warn("Master с id={} не найдена", id);
                throw new EntityNotFoundException("Master с id=" + id + " не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Master по id={} : {}", id, e.getMessage());
            throw new EntityDeleteException("Ошибка удаления Master по id", e);
        }
    }

    public void update(Master master) throws EntityUpdateException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            MasterMapper mapperMaster = session.getMapper(MasterMapper.class);
            KittyMapper kittyMapper = session.getMapper(KittyMapper.class);

            try {
                mapperMaster.update(master);
                log.info("Master обновлена: {}", master);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "MasterMapper", e);
            }

            Master loadedModel = mapperMaster.getById(master.getId());
            if (loadedModel != null) {
                if (master.getKitties() != null) {
                    try {
                        for (Kitty kitty : master.getKitties()) {
                            Kitty loadedKitty = kittyMapper.getById(kitty.getId());
                            if (loadedKitty != null) {
                                kittyMapper.update(kitty);
                                log.info("Kitty для Master обновлен: {}", kitty);

                            } else {
                                kittyMapper.save(kitty);
                                log.info("Kitty для Master сохранен: {}", kitty);
                            }
                            mapperMaster.saveMasterKittyRelation(master.getId(), kitty.getId());
                            session.commit();
                            log.info("Связи Kitty с Master сохранены");
                        }
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "KittyMapper", e);
                    }
                }
            } else {
                log.warn("Master с таким id не найдено: {}", master.getId());
                throw new EntityNotFoundException("Master c id=" + master.getId() + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновления Master {}:", master, e);
            throw new EntitySaveException("Ошибка обновления Master", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            try {
                MasterMapper mapperMaster = session.getMapper(MasterMapper.class);
                mapperMaster.deleteAllRelation();
                log.info("Все связи удалены");
                mapperMaster.deleteAll();
                session.commit();
                log.info("Все Masters удалены");
            } catch (Exception e) {
                rollBackWitchMapperException(session, "MasterMapper", e);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления всех Masters:", e);
            throw new RepositoryException("Ошибка удаления всех Masters:", e);
        }
    }

    public List<Kitty> getRelatedEntityByParentId(long id) {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            MasterMapper mapperMaster = session.getMapper(MasterMapper.class);

            try {
                List<Kitty> kitties = mapperMaster.getKittiesByMasterId(id);
                if (kitties != null) {
                    log.info("Kitty для Master найден");
                    return kitties;
                }
            } catch (Exception e) {
                rollBackWitchMapperException(session, "KittyMapper", e);
            }
            log.warn("Master с таким id не найдено: {}", id);
            throw new EntityNotFoundException("Master c id=" + id + " не найден");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Brand для Master: {}", e.getMessage());
            throw new RepositoryException("Ошибка получения Brand для Master", e);
        }
    }

    private void rollBackWitchMapperException(SqlSession session, String mapperName, Exception e) throws MapperException {
        session.rollback();
        log.error("Ошибка в {}: {}", mapperName, e.getMessage());
        throw new MapperException("Ошибка в " + mapperName + " :", e);
    }

}