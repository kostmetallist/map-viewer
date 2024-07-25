set driver_class=%1
javac -d bin\ src\*.java
java -cp bin\ %driver_class%