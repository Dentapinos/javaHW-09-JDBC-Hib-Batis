package org.example.utils;

import org.example.entity.*;
import org.example.enums.ColorKitty;
import org.example.enums.TypeBody;
import org.example.enums.TypeBuilding;
import org.example.enums.TypeTask;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityCreatorUtil {

    private static final List<String> names = new ArrayList<String>(List.of(
            "Денис",
            "Виктор",
            "Андрей",
            "Ксения",
            "Анна",
            "Виктория"
    ));

    private static final List<String> nicknames = new ArrayList<String>(List.of(
            "Макси",
            "Смоки",
            "Рокки",
            "Тигра",
            "Орео",
            "Феликс",
            "Локи",
            "Оскар",
            "Лаки"
    ));

    private static final List<String> breedOfCats = new ArrayList<>(List.of(
            "Сибирская кошка",
            "Русская голубая кошка",
            "Курильский бобтейл",
            "Карельский бобтейл",
            "Уральский рекс"
    ));

    private static final List<String> date = new ArrayList<>(List.of(
            "1991-12-04",
            "1987-10-03",
            "2001-08-12",
            "2013-03-23",
            "2000-06-27",
            "1970-05-08"
    ));

    private static final List<String> housesName = new ArrayList<>(List.of(
            "Золотой Ключ", "Серебряный Лебедь", "Красная Звезда", "Синий Океан", "Зеленый Сад", "Пурпурный Век", "Белый Лебедь", "Черный Орёл", "Желтый Солнце", "Голубой Небесный"
    ));

    private static final List<String> streetsName = new ArrayList<>(List.of(
            "Ленинградский проспект", "Арбат", "Невский проспект", "Кутузовский проспект", "Проспект Мира", "Тверская улица", "Большая Ордынка", "Новоарбатская улица", "Пушкинская улица", "Кузнецкий Мост"
    ));

    private static final List<String> brandsCarName = new ArrayList<>(List.of(
            "Лада", "УАЗ", "ГАЗ", "КАМАЗ", "МАЗ", "ТАГАЗ", "ИЖ", "Москвич", "АЗЛК", "ВАЗ"
    ));

    private static final List<String> modelsCarName = new ArrayList<>(List.of(
            "Лада ВАЗ 2101", "Лада ВАЗ 2107", "Лада ВАЗ 2110", "УАЗ Патриот", "ГАЗель", "ГАЗель Next", "Москвич-412", "Москвич-423", "Москвич-2141", "Москвич-2140"
    ));

    private static final List<String> taskNames = new ArrayList<>(List.of(
            "Сортировка Массива", "Поиск Элемента", "Обработка Строк", "Анализ Данных", "Оптимизация Кода", "Работа с Базами Данных", "Интеграция API", "Тестирование Программы", "Разработка Интерфейса", "Обработка Ошибок"
    ));

    private static final Random rand = new Random();

    public static Master getMaster() {
        Master master = new Master();
        master.setName(names.get(rand.nextInt(names.size())));
        master.setBirthday(generateRandomDate());
        return master;
    }

    public static ArrayList<Master> getMasters(int quantity) {
        ArrayList<Master> masters = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Master master = getMaster();
            master.setName(master.getName() + i);
            masters.add(master);
        }
        return masters;
    }

    public static Kitty getKitty() {
        Kitty kitty = new Kitty();
        kitty.setName(nicknames.get(rand.nextInt(nicknames.size())));
        kitty.setBirthday(LocalDate.parse(date.get(rand.nextInt(date.size()))));
        kitty.setBreed(breedOfCats.get(rand.nextInt(breedOfCats.size())));
        kitty.setColor(ColorKitty.valueOf(ColorKitty.values()[rand.nextInt(ColorKitty.values().length)].name()));
        return kitty;
    }

    public static ArrayList<Kitty> getKitties(int quantity) {
        ArrayList<Kitty> kitties = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Kitty kitty = getKitty();
            kitty.setName(kitty.getName() + i);
            kitties.add(kitty);
        }
        return kitties;
    }

    public static House getHouse() {
        return House.builder()
                .name(housesName.get(rand.nextInt(housesName.size())))
                .numberStoreys(rand.nextInt(500) / 5 + 1)
                .dateBuilding(generateRandomDate())
                .type(TypeBuilding.valueOf(TypeBuilding.values()[rand.nextInt(TypeBuilding.values().length)].name()))
                .build();
    }

    public static ArrayList<House> getHouses(int quantity) {
        ArrayList<House> houses = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            House house = getHouse();
            house.setName(house.getName() + i);
            houses.add(house);
        }
        return houses;
    }

    public static Street getStreet() {
        return Street.builder()
                .name(streetsName.get(rand.nextInt(streetsName.size())))
                .postcode(rand.nextInt(100) + 100)
                .build();
    }

    public static ArrayList<Street> getStreets(int quantity) {
        int uniqPostCode = 100;
        ArrayList<Street> streets = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Street street = getStreet();
            street.setPostcode(uniqPostCode++);
            street.setName(street.getName() + i);
            streets.add(street);
        }
        return streets;
    }

    public static BrandCar getBrandCar() {
        return BrandCar.builder()
                .name(brandsCarName.get(rand.nextInt(brandsCarName.size())))
                .dateFoundation(generateRandomDate())
                .build();
    }

    public static ArrayList<BrandCar> getBrandsCar(int quantity) {
        ArrayList<BrandCar> brandCars = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            brandCars.add(getBrandCar());
        }
        return brandCars;
    }

    public static ModelCar getModelCar() {
        return ModelCar.builder()
                .name(modelsCarName.get(rand.nextInt(modelsCarName.size())))
                .body(TypeBody.valueOf(TypeBody.values()[rand.nextInt(TypeBody.values().length)].name()))
                .width(rand.nextInt(1500) + 1000)
                .length(rand.nextInt(2000) + 3000)
                .build();
    }

    public static ArrayList<ModelCar> getModelCars(int quantity) {
        ArrayList<ModelCar> modelCars = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            modelCars.add(getModelCar());
        }
        return modelCars;
    }

    public static Employee getEmployee() {
        return Employee.builder()
                .name(names.get(rand.nextInt(names.size())))
                .birthDate(generateRandomDate())
                .build();
    }

    public static ArrayList<Employee> getEmployees(int quantity) {
        ArrayList<Employee> employees = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            employees.add(getEmployee());
        }
        return employees;
    }

    public static Task getTask() {
        return Task.builder()
                .name(taskNames.get(rand.nextInt(taskNames.size())))
                .description(taskNames.get(rand.nextInt(taskNames.size())))
                .type(TypeTask.valueOf(TypeTask.values()[rand.nextInt(TypeTask.values().length)].name()))
                .deadline(generateRandomDateDeadline())
                .build();
    }

    public static ArrayList<Task> getTasks(int quantity) {
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            tasks.add(getTask());
        }
        return tasks;
    }

    public static LocalDate generateRandomDate() {
        // Определяем начальную дату (1 января 1950 года)
        LocalDate startDate = LocalDate.of(1950, 1, 1);

        // Получаем текущую дату
        LocalDate today = LocalDate.now();

        // Вычисляем количество дней между начальной датой и сегодняшним днем
        long daysBetween = ChronoUnit.DAYS.between(startDate, today);

        // Создаем объект Random для генерации случайных чисел
        Random random = new Random();

        // Генерируем случайное количество дней от 0 до daysBetween
        int randomDays = random.nextInt((int) daysBetween + 1);

        // Добавляем случайное количество дней к начальной дате

        return startDate.plusDays(randomDays);
    }

    public static LocalDate generateRandomDateDeadline() {
        // Получаем текущую дату
        LocalDate today = LocalDate.now();

        // Определяем дату через год
        LocalDate nextYear = today.plusYears(1);

        // Вычисляем количество дней между сегодняшним днем и днем через год
        long daysBetween = ChronoUnit.DAYS.between(today, nextYear);

        // Создаем объект Random для генерации случайных чисел
        Random random = new Random();

        // Генерируем случайное количество дней от 0 до daysBetween
        int randomDays = random.nextInt((int) daysBetween + 1);

        // Добавляем случайное количество дней к текущей дате

        return today.plusDays(randomDays);
    }

}
