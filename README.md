This is useful maven plugin which provides build-time validation of localized messages in your java project.

The idea is to use some markers in java code (@Annotations) to provide information about defined localized messages. Then at build time maven plugin will use this information to analyze and check that both code and properties files are in sync.

## Simple example ##
(here is [FullExample](http://github.com/viktor-podzigun/i18n-maven-plugin/blob/master/i18n-demo/src/main/java/com/googlecode/i18n/demo/FullExample.java)):
```java
// Messages.java
@MessageProvider
public enum Messages {

    HELLO_WORLD,
    
    @MessageFormatted
    HELLO_WORLD_MSG,
    
    @StringFormatted
    HELLO_WORLD_STR,
    
}
```

```properties
# Messages.properties
HELLO_WORLD=Hello World!
HELLO_WORLD_MSG=Hello World! My name is {0}
HELLO_WORLD_STR=Hello World! My name is %s
```

```properties
# Messages_ru.properties
HELLO_WORLD=Привет Мир!
HELLO_WORLD_MSG=Привет Мир! Меня зовут {0}
HELLO_WORLD_STR=Привет Мир! Меня зовут % s
NOT_USED=Тест
```

### Example output ###
```
[INFO]
[INFO] --- i18n-maven-plugin:1.0.0-SNAPSHOT:i18n (default) @ i18n-demo ---
[INFO] Checking com.googlecode.i18n.demo.Messages
[INFO]   Checking Messages.properties
[INFO]   Checking Messages_ru.properties
[ERROR]     Invalid format [HELLO_WORLD_STR]
        Conversion = s, Flags =
[WARNING]   found not used keys:
[WARNING]     [NOT_USED]
[INFO]
[INFO] Check results:
[INFO]   1 error(s), 1 warning(s)
[INFO] -------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] -------------------------------------------------------------------
```

### Example configuration ###
For full list of configuration parameters, please, refer to [I18nPluginMojo](http://github.com/viktor-podzigun/i18n-maven-plugin/blob/master/i18n-maven-plugin/src/main/java/com/googlecode/i18n/plugin/I18nPluginMojo.java)
```xml
<!-- pom.xml -->
    <build>
        <plugins>
            <plugin>
                <groupId>com.googlecode.i18n-maven-plugin</groupId>
                <artifactId>i18n-maven-plugin</artifactId>
                <version>${i18n-maven-plugin.version}</version>
                <executions>
                    <!-- You can define either class/source mapped localization check -->
                    <execution>
                        <id>classes-check</id>
                        <goals><goal>i18n</goal></goals>
                    </execution>
                    
                    <!-- And/Or plain old properties check -->
                    <execution>
                        <id>plain-check</id>
                        <goals><goal>i18n</goal></goals>
                        <configuration>
                            <locales>en,ru</locales>
                            <baseLocale>en</baseLocale>
                            
                            <!-- If your properties files looks like "en.properties"
                                 then this path should ends with "/" -->
                            <plainFilePath>localization/</plainFilePath>
                        </configuration>
                    </execution>
                </executions>
                <configuration>
                    <locales>ru</locales>
                </configuration>
            </plugin>
        </plugins>
    </build>
    
    <dependencies>
        <dependency>
            <groupId>com.googlecode.i18n-maven-plugin</groupId>
            <artifactId>i18n-annotations</artifactId>
            <version>${i18n-maven-plugin.version}</version>
        </dependency>
    </dependencies>
```

## Maven ##
Plugin modules are available on [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22com.googlecode.i18n-maven-plugin%22), through [Sonatype OSS hosting](http://oss.sonatype.org/).

## Groups ##
[Discussion](http://groups.google.com/group/i18n-maven-plugin-discuss)
[Commits](http://groups.google.com/group/i18n-maven-plugin-commits)

## Code license ##
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Change log ##

### 1.1.0 (future release) ###
Added plain properties check mode

### 1.0.0 ###
First release
