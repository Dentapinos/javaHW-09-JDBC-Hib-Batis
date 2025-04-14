package org.example;

import org.example.entity.*;
import org.example.enums.Operation;
import org.example.enums.SessionName;
import org.example.service.ServiceFactory;
import org.example.utils.EntityCreatorUtil;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;

public class TimeMeasurement {

    private static DefaultCategoryDataset dataset;
    private static int quantityEntity = 100;
    private List<Task> tasks = EntityCreatorUtil.getTasks(quantityEntity);
    private List<Employee> employees = EntityCreatorUtil.getEmployees(quantityEntity);
    private List<Master> masters = EntityCreatorUtil.getMasters(quantityEntity);
    private List<Kitty> kitties = EntityCreatorUtil.getKitties(quantityEntity);
    private List<Street> streets = EntityCreatorUtil.getStreets(quantityEntity);
    private List<House> houses = EntityCreatorUtil.getHouses(quantityEntity);
    private List<ModelCar> modelCars = EntityCreatorUtil.getModelCars(quantityEntity);
    private List<BrandCar> brandCars = EntityCreatorUtil.getBrandsCar(quantityEntity);

    public TimeMeasurement(DefaultCategoryDataset dataset, int quantityEntity) {
        TimeMeasurement.dataset = dataset;
        TimeMeasurement.quantityEntity = quantityEntity;
    }

    public void run() {

        for (SessionName sessionName : SessionName.values()) {
            for (Operation operation : Operation.values()) {
                measurementTimeMaster(masters, sessionName, operation);
                measurementTimeKitty(kitties, sessionName, operation);
                measurementTimeStreet(streets, sessionName, operation);
                measurementTimeHouse(houses, sessionName, operation);
                measurementTimeModelCar(modelCars, sessionName, operation);
                measurementTimeBrandCar(brandCars, sessionName, operation);
                measurementTimeEmployee(employees, sessionName, operation);
                measurementTimeTask(tasks, sessionName, operation);
            }
            rebuildEntity(quantityEntity);
        }
    }

    private void rebuildEntity(int quantityEntity) {
        tasks = EntityCreatorUtil.getTasks(quantityEntity);
        employees = EntityCreatorUtil.getEmployees(quantityEntity);
        masters = EntityCreatorUtil.getMasters(quantityEntity);
        kitties = EntityCreatorUtil.getKitties(quantityEntity);
        streets = EntityCreatorUtil.getStreets(quantityEntity);
        houses = EntityCreatorUtil.getHouses(quantityEntity);
        modelCars = EntityCreatorUtil.getModelCars(quantityEntity);
        brandCars = EntityCreatorUtil.getBrandsCar(quantityEntity);
    }

    public static void measurementTimeMaster(List<Master> masterList, SessionName sessionName, Operation operation) {
        long startTime = System.currentTimeMillis();

        for (Master entity : masterList) {
            if (operation.equals(Operation.SAVE)) ServiceFactory.getMasterService(sessionName).save(entity);
            else if (operation.equals(Operation.GET_ALL)) {
                ServiceFactory.getMasterService(sessionName).getAll();
                break;
            }
        }

        fillDataset("Master", sessionName, startTime, operation);
    }

    public static void measurementTimeKitty(List<Kitty> kittyList, SessionName sessionName, Operation operation) {
        long duration;
        long startTime = System.currentTimeMillis();

        for (Kitty entity : kittyList) {
            if (operation.equals(Operation.SAVE)) ServiceFactory.getKittyService(sessionName).save(entity);
            else if (operation.equals(Operation.GET_ALL)) {
                ServiceFactory.getKittyService(sessionName).getAll();
                break;
            }
        }

        fillDataset("Kitty", sessionName, startTime, operation);
    }

    public static void measurementTimeStreet(List<Street> streetList, SessionName sessionName, Operation operation) {
        long startTime = System.currentTimeMillis();

        for (Street entity : streetList) {
            if (operation.equals(Operation.SAVE)) ServiceFactory.getStreetService(sessionName).save(entity);
            else if (operation.equals(Operation.GET_ALL)) {
                ServiceFactory.getStreetService(sessionName).getAll();
                break;
            }
        }

        fillDataset("Street", sessionName, startTime, operation);
    }

    public static void measurementTimeHouse(List<House> houseList, SessionName sessionName, Operation operation) {
        long startTime = System.currentTimeMillis();

        for (House entity : houseList) {
            if (operation.equals(Operation.SAVE)) ServiceFactory.getHouseService(sessionName).save(entity);
            else if (operation.equals(Operation.GET_ALL)) {
                ServiceFactory.getHouseService(sessionName).getAll();
                break;
            }
        }

        fillDataset("House", sessionName, startTime, operation);
    }

    public static void measurementTimeBrandCar(List<BrandCar> brandCarList, SessionName sessionName, Operation operation) {
        long startTime = System.currentTimeMillis();

        for (BrandCar entity : brandCarList) {
            if (operation.equals(Operation.SAVE)) ServiceFactory.getBrandCarService(sessionName).save(entity);
            else if (operation.equals(Operation.GET_ALL)) {
                ServiceFactory.getBrandCarService(sessionName).getAll();
                break;
            }
        }

        fillDataset("BrandCar", sessionName, startTime, operation);
    }

    public static void measurementTimeModelCar(List<ModelCar> modelCarList, SessionName sessionName, Operation operation) {
        long startTime = System.currentTimeMillis();

        for (ModelCar entity : modelCarList) {
            if (operation.equals(Operation.SAVE)) ServiceFactory.getModelCarService(sessionName).save(entity);
            else if (operation.equals(Operation.GET_ALL)) {
                ServiceFactory.getModelCarService(sessionName).getAll();
                break;
            }
        }

        fillDataset("ModelCar", sessionName, startTime, operation);
    }

    public static void measurementTimeEmployee(List<Employee> employeeList, SessionName sessionName, Operation operation) {
        long startTime = System.currentTimeMillis();

        for (Employee entity : employeeList) {
            if (operation.equals(Operation.SAVE)) ServiceFactory.getEmployeeService(sessionName).save(entity);
            else if (operation.equals(Operation.GET_ALL)) {
                ServiceFactory.getEmployeeService(sessionName).getAll();
                break;
            }
        }

        fillDataset("Employee", sessionName, startTime, operation);
    }

    public static void measurementTimeTask(List<Task> taskList, SessionName sessionName, Operation operation) {
        long startTime = System.currentTimeMillis();

        for (Task entity : taskList) {
            if (operation.equals(Operation.SAVE)) ServiceFactory.getTaskService(sessionName).save(entity);
            else if (operation.equals(Operation.GET_ALL)) {
                ServiceFactory.getTaskService(sessionName).getAll();
                break;
            }
        }
        fillDataset("Task", sessionName, startTime, operation);
    }

    private static void fillDataset(String className, SessionName sessionName, long startTime, Operation operation) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String type = operation.name();
        System.out.println(sessionName.name() + ": - Время сохранения " + className + ": " + duration + " миллисекунд");
        dataset.addValue(duration, sessionName.name() + "-" + type, className);
    }
}
