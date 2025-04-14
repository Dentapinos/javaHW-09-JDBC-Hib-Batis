package org.example.service;

import org.example.entity.*;
import org.example.enums.SessionName;
import org.example.repository.EntityRepository;
import org.example.repository.hibernate.*;
import org.example.repository.jdbc.*;

public class ServiceFactory {

    public static EmployeeService getEmployeeService(SessionName sessionName) {
        EntityRepository<Employee, Task> repository;
        if (sessionName.name().equals(SessionName.HIBERNATE.name())) {
            repository = new HibernateEmployeeRepository();
        } else if (sessionName.name().equals(SessionName.JDBC.name())) {
            repository = new JdbcEmployeeRepository();
        } else if (sessionName.name().equals(SessionName.MY_BATIS.name())) {
            repository = new HibernateEmployeeRepository();
        } else {
            repository = null;
        }
        return new EmployeeService(repository);
    }

    static public TaskService getTaskService(SessionName sessionName) {
        EntityRepository<Task, Employee> repository;
        if (sessionName.name().equals(SessionName.HIBERNATE.name())) {
            repository = new HibernateTaskRepository();
        } else if (sessionName.name().equals(SessionName.JDBC.name())) {
            repository = new JdbcTaskRepository();
        } else if (sessionName.name().equals(SessionName.MY_BATIS.name())) {
            repository = new HibernateTaskRepository();
        } else {
            repository = null;
        }
        return new TaskService(repository);
    }

    static public MasterService getMasterService(SessionName sessionName) {
        EntityRepository<Master, Kitty> repository;
        if (sessionName.name().equals(SessionName.HIBERNATE.name())) {
            repository = new HibernateMasterRepository();
        } else if (sessionName.name().equals(SessionName.JDBC.name())) {
            repository = new JdbcMasterRepository();
        } else if (sessionName.name().equals(SessionName.MY_BATIS.name())) {
            repository = new HibernateMasterRepository();
        } else {
            repository = null;
        }
        return new MasterService(repository);
    }

    static public KittyService getKittyService(SessionName sessionName) {
        EntityRepository<Kitty, Master> repository;
        if (sessionName.name().equals(SessionName.HIBERNATE.name())) {
            repository = new HibernateKittyRepository();
        } else if (sessionName.name().equals(SessionName.JDBC.name())) {
            repository = new JdbcKittyRepository();
        } else if (sessionName.name().equals(SessionName.MY_BATIS.name())) {
            repository = new HibernateKittyRepository();
        } else {
            repository = null;
        }
        return new KittyService(repository);
    }

    static public BrandCarService getBrandCarService(SessionName sessionName) {
        EntityRepository<BrandCar, ModelCar> repository;
        if (sessionName.name().equals(SessionName.HIBERNATE.name())) {
            repository = new HibernateBrandRepository();
        } else if (sessionName.name().equals(SessionName.JDBC.name())) {
            repository = new JdbcBrandRepository();
        } else if (sessionName.name().equals(SessionName.MY_BATIS.name())) {
            repository = new HibernateBrandRepository();
        } else {
            repository = null;
        }
        return new BrandCarService(repository);
    }

    static public ModelCarService getModelCarService(SessionName sessionName) {
        EntityRepository<ModelCar, BrandCar> repository;
        if (sessionName.name().equals(SessionName.HIBERNATE.name())) {
            repository = new HibernateModelCarRepository();
        } else if (sessionName.name().equals(SessionName.JDBC.name())) {
            repository = new JdbcModelRepository();
        } else if (sessionName.name().equals(SessionName.MY_BATIS.name())) {
            repository = new HibernateModelCarRepository();
        } else {
            repository = null;
        }
        return new ModelCarService(repository);
    }

    static public StreetService getStreetService(SessionName sessionName) {
        EntityRepository<Street, House> repository;
        if (sessionName.name().equals(SessionName.HIBERNATE.name())) {
            repository = new HibernateStreetRepository();
        } else if (sessionName.name().equals(SessionName.JDBC.name())) {
            repository = new JdbcStreetRepository();
        } else if (sessionName.name().equals(SessionName.MY_BATIS.name())) {
            repository = new HibernateStreetRepository();
        } else {
            repository = null;
        }
        return new StreetService(repository);
    }

    static public HouseService getHouseService(SessionName sessionName) {
        EntityRepository<House, Street> repository;
        if (sessionName.name().equals(SessionName.HIBERNATE.name())) {
            repository = new HibernateHouseRepository();
        } else if (sessionName.name().equals(SessionName.JDBC.name())) {
            repository = new JdbcHouseRepository();
        } else if (sessionName.name().equals(SessionName.MY_BATIS.name())) {
            repository = new HibernateHouseRepository();
        } else {
            repository = null;
        }
        return new HouseService(repository);
    }
}
