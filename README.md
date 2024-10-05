# Compile && Run
## Download JavaFX 21 from [here](https://gluonhq.com/products/javafx/)
## Extract the downloaded file
## Set the path of the extracted folder in the `pom.xml` file
```xml
    <javafx.path>path/to/javafx-sdk-21</javafx.path>
```
## With UI
```bash
    mvn clean compile 
    mvn exec:java -Dexec.args="ui"
```
## Without UI
```bash
    mvn clean compile 
    mvn exec:java
```

## Run .jar file
```bash
    java -jar ui.jar
```
