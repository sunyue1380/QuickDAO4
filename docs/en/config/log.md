# Log

QuickDAO used slf4j as log facade framework. You can choose logback,log4j and so on as log implement.  

> SQL statement will be printed in ``DEBUG`` level.

## Import logback

```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.3</version>
</dependency>
```

## Config logback.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true">
    <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
        <Target>System.out</Target>
        <encoder>
            <pattern>%-5level %d{yyyy-MM-dd HH:mm:ss.SSS} %c{10}:%L %m %n</pattern>
        </encoder>
    </appender>
    
    <logger name="cn.schoolwow.quickdao" level="DEBUG" additivity="false">
        <appender-ref ref="stdout"/>
    </logger>

    <root level="DEBUG">
        <appender-ref ref="stdout" />
    </root>
</configuration>
```