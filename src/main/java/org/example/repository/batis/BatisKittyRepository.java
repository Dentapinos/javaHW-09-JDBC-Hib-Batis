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
import org.example.repository.IKittyRepository;

import java.util.List;

@Slf4j
public class BatisKittyRepository implements IKittyRepository {

    public void save(Kitty entity) throws EntitySaveException {
        if (entity == null) {
            log.error("Ошибка сохранения Kitty null");
            throw new EntitySaveException("Ошибка сохранения Kitty null");
        }
        if (entity.getId() != 0) {
            log.warn("id должно быть 0, установлен id={}", entity.getId());
            throw new EntitySaveException("Id должен быть 0, установлен id=" + entity.getId());
        }

        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            KittyMapper mapperKitty = session.getMapper(KittyMapper.class);
            MasterMapper mapperMaster = session.getMapper(MasterMapper.class);

            try {
                mapperKitty.save(entity);
                log.info("Kitty сохранен: {}", entity);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "KittyMapper", e);
            }

            if (entity.getMasters() != null) {
                for (Master master : entity.getMasters()) {
                    try {
                        Master loadMaster = mapperMaster.getById(master.getId());
                        if (loadMaster != null) {
                            mapperMaster.update(master);
                            log.info("Master для Kitty обновлен: {}", master);
                        } else {
                            mapperMaster.save(master);
                            log.info("Master для Kitty сохранен: {}", master);
                        }
                        mapperKitty.saveMasterKittyRelation(master.getId(), entity.getId());
                        session.commit();
                        log.info("Связь Master с Kitty установлена: {}", master);
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "KittyMapper", e);
                    }
                }
            }
            session.commit();
            log.info("Kitty сохранен: {}", entity);
        } catch (Exception e) {
            log.error("Ошибка сохранения {}:", entity.getClass().getSimpleName(), e);
            throw new EntitySaveException("Ошибка сохранения " + entity.getClass().getSimpleName(), e);
        }
    }

    public Kitty findById(long id) throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            KittyMapper mapperKitty = session.getMapper(KittyMapper.class);
            Kitty loadedEntity = null;
            try {
                loadedEntity = mapperKitty.getById(id);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "KittyMapper", e);
            }
            if (loadedEntity == null) {
                log.warn("Kitty с таким id не найдено: {}", id);
                throw new EntityNotFoundException("Kitty c id=" + id + " не найден");
            }
            log.info("Kitty найден: {}", loadedEntity);
            return loadedEntity;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Kitty по id={}", id, e);
            throw new RepositoryException("Ошибка получения Kitty по id=" + id, e);
        }
    }

    public List<Kitty> findAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            KittyMapper mapperKitty = session.getMapper(KittyMapper.class);
            List<Kitty> kitty = mapperKitty.getAll();
            log.info("Получены все Kitties: {}", kitty);
            return kitty;
        } catch (Exception e) {
            log.error("Ошибка получения всех Kitties:", e);
            throw new RepositoryException("Ошибка получения всех Kitties:", e);
        }
    }

    public void delete(long id) throws EntityNotFoundException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            KittyMapper mapperKitty = session.getMapper(KittyMapper.class);

            Kitty kitty = mapperKitty.getById(id);
            if (kitty != null) {
                try {
                    mapperKitty.deleteRelationByKittyId(kitty.getId());
                    log.info("Попытка удаления связей Kitty id={}", kitty.getId());
                    mapperKitty.deleteById(id);
                    session.commit();
                    log.info("Связи Kitty id={} удалены: {}", kitty.getId(), kitty);
                    log.info("Kitty удалена: {}", kitty);
                } catch (Exception e) {
                    rollBackWitchMapperException(session, "KittyMapper", e);
                }
            } else {
                log.warn("Kitty с id={} не найдена", id);
                throw new EntityNotFoundException("Kitty с id=" + id + " не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка удаления Kitty по id={} : {}", id, e.getMessage());
            throw new EntityDeleteException("Ошибка удаления Kitty по id", e);
        }
    }

    public void update(Kitty kitty) throws EntityUpdateException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            KittyMapper mapperKitty = session.getMapper(KittyMapper.class);
            MasterMapper mapperMaster = session.getMapper(MasterMapper.class);

            try {
                mapperKitty.update(kitty);
                log.info("Kitty обновлена: {}", kitty);
            } catch (Exception e) {
                rollBackWitchMapperException(session, "KittyMapper", e);
            }

            Kitty loadedModel = mapperKitty.getById(kitty.getId());
            if (loadedModel != null) {
                if (kitty.getMasters() != null) {
                    try {
                        for (Master master : kitty.getMasters()) {
                            Master loadedMaster = mapperMaster.getById(master.getId());
                            if (loadedMaster != null) {
                                mapperMaster.update(master);
                                log.info("Master для Kitty обновлен: {}", master);

                            } else {
                                mapperMaster.save(master);
                                log.info("Master для Kitty сохранен: {}", master);
                            }
                            mapperKitty.saveMasterKittyRelation(master.getId(), kitty.getId());
                            session.commit();
                            log.info("Связи Master с Kitty сохранены");
                        }
                    } catch (Exception e) {
                        rollBackWitchMapperException(session, "MasterMapper", e);
                    }
                }
            } else {
                log.warn("Kitty с таким id не найдено: {}", kitty.getId());
                throw new EntityNotFoundException("Kitty c id=" + kitty.getId() + " не найден");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновления Kitty {}:", kitty, e);
            throw new EntitySaveException("Ошибка обновления Kitty", e);
        }
    }

    public void deleteAll() throws RepositoryException {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            try {
                KittyMapper mapperKitty = session.getMapper(KittyMapper.class);
                mapperKitty.deleteAllRelation();
                log.info("Все связи удалены");
                mapperKitty.deleteAll();
                session.commit();
                log.info("Все Kitties удалены");
            } catch (Exception e) {
                rollBackWitchMapperException(session, "KittyMapper", e);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления всех Kitties:", e);
            throw new RepositoryException("Ошибка удаления всех Kitties:", e);
        }
    }

    public List<Master> getRelatedEntityByParentId(long id) {
        try (SqlSession session = (SqlSession) SessionManager.createSession(SessionName.MY_BATIS.getSessionName())) {
            KittyMapper mapperKitty = session.getMapper(KittyMapper.class);

            try {
                List<Master> masters = mapperKitty.getMastersByKittyId(id);
                if (masters != null) {
                    log.info("Master для Kitty найден");
                    return masters;
                }
            } catch (Exception e) {
                rollBackWitchMapperException(session, "MasterMapper", e);
            }
            log.warn("Kitty с таким id не найдено: {}", id);
            throw new EntityNotFoundException("Kitty c id=" + id + " не найден");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения Brand для Kitty: {}", e.getMessage());
            throw new RepositoryException("Ошибка получения Brand для Kitty", e);
        }
    }

    private void rollBackWitchMapperException(SqlSession session, String mapperName, Exception e) throws MapperException {
        session.rollback();
        log.error("Ошибка в {}: {}", mapperName, e.getMessage());
        throw new MapperException("Ошибка в " + mapperName + " :", e);
    }

}