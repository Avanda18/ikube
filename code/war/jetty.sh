#export JAVA_OPTS="-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m"
#export MAVEN_OPTS="-XX:PermSize=1024m -XX:MaxPermSize=2048m -Xms4096m -Xmx8192m" 
#-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
#set MAVEN_OPTS=-XX:PermSize=128m -XX:MaxPermSize=256m -Xms512m -Xmx1024m 
mvn jetty:run -DskipTests=true -DskipITs=true -o -Djava.rmi.server.hostname=192.168.1.40 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8500 -Dcom.sun.management.jmxremote.rmi.port=8500 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false