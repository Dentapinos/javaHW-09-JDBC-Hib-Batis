## Добавить зависимости

   <!-- SLF4J API -->

    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.3</version>
    </dependency>

    <!-- Logback Classic -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.5.18</version>
    </dependency>

    <!-- Logback Core -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-core</artifactId>
        <version>1.5.18</version>
    </dependency>

<span style = "color:yellow">SLF4J API:</span>
Это основная библиотека SLF4J, которая предоставляет API для логирования.

<span style = "color:yellow">Logback Classic:</span>
Это реализация SLF4J, которая предоставляет функциональность логирования.

<span style = "color:yellow">Logback Core:</span>
Это ядро Logback, которое предоставляет основные функции логирования.

## Добавить файл конфигурации logback.xml

После добавления зависимостей, создайте файл logback.xml в директории src/main/resources вашего проекта. Вот пример
базовой конфигурации logback.xml:

    <configuration>
    
        <!-- Конфигурация Appender для вывода логов в консоль -->
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
    
        <!-- Конфигурация Appender для вывода логов в файл -->
        <appender name="FILE" class="ch.qos.logback.core.FileAppender">
            <file>logs/app.log</file>
            <append>true</append>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
    
        <!-- Корневой логгер -->
        <root level="debug">
            <appender-ref ref="CONSOLE" />
            <appender-ref ref="FILE" />
        </root>
    
    </configuration>

### Объяснение конфигурации

Уровни логирования: Поддерживаются различные уровни логирования
(<span style = "color:yellow">TRACE, DEBUG, INFO, WARN, ERROR</span>).

Конфигурация Appender для вывода логов в консоль:

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">:

Определяет appender с именем CONSOLE, который выводит логи в консоль.

    <encoder>: 

Определяет формат вывода логов.

    <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>:

Определяет шаблон для форматирования логов. В данном случае, шаблон включает дату, уровень логирования, имя логгера и
сообщение.
Конфигурация Appender для вывода логов в файл:

    <appender name="FILE" class="ch.qos.logback.core.FileAppender">:

Определяет appender с именем FILE, который выводит логи в файл.

    <file>logs/app.log</file>:

Указывает путь к файлу, в который будут записываться логи.

    <append>true</append>:

Указывает, что логи будут добавляться в конец файла (если false, файл будет перезаписываться).

    <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>:

Определяет шаблон для форматирования логов.

Корневой логгер:

    <root level="debug">:

Определяет корневой логгер с уровнем логирования debug.

    <appender-ref ref="CONSOLE" />:

Указывает, что логи должны быть отправлены в appender с именем CONSOLE.

    <appender-ref ref="FILE" />:

Указывает, что логи должны быть отправлены в appender с именем FILE.

### Отключение логов Hibernate

    <logger name="org.hibernate" level="OFF" />

Что бы отключить все логи Hibernate, установить уровень логирования на OFF:

### Отключение логов MyBatis

    <logger name="org.apache.ibatis" level="ERROR" />

### Пример конфигурации для полного отключения логов JDBC

    <logger name="java.sql" level="OFF" />
    <logger name="javax.sql" level="OFF" />
    <logger name="com.zaxxer.hikari" level="OFF" /> <!-- Пример для HikariCP -->
    <logger name="org.hibernate.SQL" level="OFF" /> <!-- Пример для Hibernate -->