
# Hi, I'm Dentapinos! 👋

[![Typing SVG](https://readme-typing-svg.herokuapp.com?color=%2336BCF7&lines=Computer+science+student)](https://git.io/typing-svg)

<details>
<summary>Используемые фреймворки в данной работе</summary>

| Rank | Frameworks |
|-----:|------------|
|     1| JDBC       |
|     2| Hibernate  |
|     3| MyBatis    |

</details>



# Домашнее задание №9 - Создание базы данных Mysql, выполнение CRUD с помощью HIBERNATE, JDBC, MyBatis

Сокращенно. Создать таблицы в базе данных MySql. Реализовать указанные методы работы с базой данных:<br>
public T save(T entity);<br>
public void deleteById(long id);<br>
public void deleteByEntity(T entity);<br>
public void deleteAll();<br>
public T update(T entity);<br>
public T getById(long id);<br>
public List<T> getAll();<br>
для каждого из фреймворков: JDBC, Hibernate, MyBatis.<br>
Отправить в бд запрос на добавление 100 сущностей и сравнить время, за которое это будет выполнено
всеми тремя способами.
Затем отправить запрос на получение этих же самых 100 сущностей,
(можно использовать метод getAll()), и также сравнить время, за которое это будет сделано тремя различными способами.
[Подробное описание дз](https://github.com/Kichmarevitmo/Lesson-11.-Part-1.-Homework)

## Что я сделал
Для каждого из фреймворков и на каждую отдельную сущность я создал репозиторий.
Далее я создал репозиторий для основной логики. В каждом репозитории и сервисе я произвел логирование с помощью SLF4J.
Что бы протестировать корректность выполнения запросов к БД я написал тесты, и для быстрого выполнения этих тестов я
подключил базу данных H2.
Что бы выполнить замер времени я создал класс TimeMeasurement в котором по методу run(),
будет произведен запуск выполнения сохранения и получения 100 записей(количество можно указать в конструкторе).
Так же для наглядности полученных данных я создал класс вывода в отдельном экране данных в виде графика.

## Описание
<span style = "color:green">org/example/configs</span> Тут собраны менеджеры настроек подключения к базе данных<br>
[HibernateManager.java](src/main/java/org/example/configs/HibernateManager.java) - настройки подключения к бд для Hibernate<br>
[JDBCManager.java](src/main/java/org/example/configs/JDBCManager.java) - настройки подключения к бд для Hibernate<br>
[MyBatisConfig.java](src/main/java/org/example/configs/MyBatisConfig.java) - настройки подключения к бд для Hibernate<br>
[SessionManager.java](src/main/java/org/example/configs/SessionManager.java) - фабрика подключений<br>


<span style = "color:green">org/example/entity</span> Тут собраны сущности<br>
[BrandCar.java](src/main/java/org/example/entity/BrandCar.java) - сущность бренда машины, может иметь множество моделей<br>
[ModelCar.java](src/main/java/org/example/entity/ModelCar.java) - сущность модели машины, может иметь только один бренд, и не может существовать без бренда<br>
[Master.java](src/main/java/org/example/entity/Master.java) - сущность хозяина кошек, может иметь множество кошек<br>
[Kitty.java](src/main/java/org/example/entity/Kitty.java) - сущность кошки, может иметь множество хозяев<br>
[Street.java](src/main/java/org/example/entity/Street.java) - сущность улицы, может иметь множество домов<br>
[House.java](src/main/java/org/example/entity/House.java) - сущность дома, может иметь только одну улицу и не может существовать без улицы<br>
[Employee.java](src/main/java/org/example/entity/Employee.java) - сущность сотрудника, может выполнять только одну задачу<br>
[Task.java](src/main/java/org/example/entity/Task.java) - сущность задачи, может быть взята только одним сотрудником<br>

<span style = "color:green">org/example/exception</span> классы собственных исключений во время работы приложения<br>

<span style = "color:green">org/example/mappers</span> Интерфейсы работы с мапперами для MyBatis, выступает в качестве репозитория<br>

<span style = "color:green">org/example/repositories</span> Содержит в себе пакеты с репозиториями для каждого из фреймворков<br>

<span style = "color:green">org/example/service</span> В этом пакете описаны сервисы. Вся логика работы с базой и логирование находится тут <br>

<span style = "color:green">org/example/utils</span> Утилиты для работы<br>
[CreatorTablesUtil.java](src/main/java/org/example/utils/CreateDropTablesUtil.java) - класс для выполнения операций создания, удаления таблиц, обновление авто-инкремента<br>
[EntityCreatorUtil.java](src/main/java/org/example/utils/EntityCreatorUtil.java) - удобная утилита для быстрого создания большого количества сущностей<br>
[Graph.java](src/main/java/org/example/utils/Graph.java) - класс который принимает DataSet и строит график из полученных данных<br>

<span style = "color:green">org/example</span> Основные классы для работы<br>
[TimeMeasurement.java](src/main/java/org/example/TimeMeasurement.java) - класс для выполнения тестов сохранения и чтения 100(любое число) записей всех сущностей,
а также сбор данных в DataSet

<span style = "color:lime">hibernate.cfg.xml</span> файл настройки hibernate, для [HibernateManager.java](src/main/java/org/example/configs/HibernateManager.java)<br>
<span style = "color:lime">mybatis-config.xml</span> файл настройки myBatis, для [MyBatisConfig.java](src/main/java/org/example/configs/MyBatisConfig.java)<br>
<span style = "color:lime">logback.xml</span> файл настройки для логирования <br>

## Тесты
Для тестирования запросов в базу данных я использовал H2 базу данных. Также для тестов я 
создал дополнительный файлы <span style = "color:yellow">hibernate.cfg.xml</span> и
<span style = "color:yellow">mybatis-config.xml</span> а так же конфигурационные классы
<span style = "color:yellow">src/test/java/org/example/configs</span>.<br>
[src/test/java/example/service](src/test/java/example/service) - пакет тестов для всех сущностей используя разные технологии<br>

## Что узнал и чему научился
Углубился в понимание JDBC, познакомился и вник в работу Hibernate и MyBatis.<br>
JDBC это низкоуровневый язык использующий для выполнения запросов в базу язык SQL.<br>
Hibernate — это ORM (Object-Relational Mapping) фреймворк, который позволяет работать с базами данных через объекты Java.
Он позволяет автоматически создавать SQL запросы посредством аннотаций, что я и сделал.
Под капотом Hibernate имеет тот же самый JDBC. Для работы с объектами Hibernate имеет два уровня кеша,
Первый уровень хранит загружаемые и сохраняемые объекты для работы с ними в не базы данных, 
это уменьшает количество обращений в базу, а второй уровень хранит данные сессий.
Так же он имеет кеш запросов и кеш коллекций, что уменьшает количество запросов в базу.
Для особых запросов Hibernate использует свой язык запросов HQL или JPQL.<br>
MyBatis позволяет писать произвольные запросы и мапить результаты на объекты. Язык Sql.
Для мапинга использует xml файлы или аннотации, я использовал xml.

## Результаты выполнения ДЗ
![график](src/main/resources/img/graphTest.jpg)

| Framework/method   | Master | Kitty  | Street | House  | ModelCar | BrandCar | Employee | Task   |
|--------------------|--------|--------|--------|--------|----------|----------|----------|--------|
| HIBERNATE-SAVE     | 3598.0 | 383.0  | 361.0  | 350.0  | 320.0    | 318.0    | 302.0    | 335.0  |
| HIBERNATE-GET_ALL  | 556.0  | 360.0  | 341.0  | 23.0   | 19.0     | 310.0    | 209.0    | 17.0   |
| JDBC-SAVE          | 1612.0 | 1395.0 | 1358.0 | 1317.0 | 1300.0   | 1279.0   | 1252.0   | 1233.0 |
| JDBC-GET_ALL       | 15.0   | 16.0   | 18.0   | 32.0   | 39.0     | 15.0     | 22.0     | 28.0   |
| MY_BATIS-SAVE      | 270.0  | 289.0  | 242.0  | 266.0  | 257.0    | 261.0    | 259.0    | 270.0  |
| MY_BATIS-GET_ALL   | 456.0  | 453.0  | 429.0  | 14.0   | 11.0     | 472.0    | 249.0    | 13.0   |

<span style = "color:green">Выводы</span> Сохранение<br>
По скорости сохранения я вижу, судя по данным, что Hibernate чуть дольше сохраняет чем MyBatis.
А JDBС существенно отстает по скорости сохранения.

<span style = "color:green">Выводы</span> Получение<br>
Получение с помощью JDBC происходит максимально быстро. 
А Hibernate и MyBatis производят получение с той же скоростью, что и сохранение.
От JDBC они отстают за счет кеширования данных.
Так же на графике видно что House и ModelCar они получают быстрее, 
это связано с тем что House и Street имеют только по одной вложенной сущности, а не коллекцию.


## Authors

- [@Dentapinos](https://github.com/Dentapinos)


