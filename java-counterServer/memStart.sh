nohup java -server -Xms1g -Xmx1g -Dcom.sun.management.jmxremote -Xloggc:./gc.log -jar target/java-counterServer-jar-with-dependencies.jar -in true >start.out & 2>&1
