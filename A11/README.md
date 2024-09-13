# Compile && Run
## Linux or MacOs
```bash
    find . -name "*.java" > sources.txt && javac -d out @sources.txt
    java -cp out Main
```

## Windows
```bash
    dir /s /B *.java > sources.txt && javac -d out @sources.txt
    java -cp out Main
```
