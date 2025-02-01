# Prerequisites
- Download JavaFX 21 from [here](https://gluonhq.com/products/javafx/)
- Extract the downloaded file
- Set the path of the extracted folder in the `pom.xml` file
```xml
    <javafx.path>path/to/javafx-sdk-21</javafx.path>
```

# Compile && Run
### Compile
```bash
    mvn clean compile package
```
### Run
```bash
    # With UI
    mvn exec:java -Dexec.args="ui"
```
```bash
    # Without UI
    mvn exec:java
```

### Using .jar file
```bash
    # With UI
    java --module-path javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml -jar target/coding-theory-1.0-SNAPSHOT.jar ui
```

```bash
    # Without UI
    java -jar target/coding-theory-1.0-SNAPSHOT.jar
```
