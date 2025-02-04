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
    mvn exec:java
```

### Run .jar
```bash
    java -jar target/coding-theory-1.0-SNAPSHOT.jar
```
