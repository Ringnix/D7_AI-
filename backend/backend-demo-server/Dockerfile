FROM harbor.cyberway.com.cn/common/openjdk:1702-sky8_7

#set env
ENV JAVA_OPTS "-Xmx300M -Xms300M"
#service name
COPY target/*.jar /app.jar

CMD java  -javaagent:/agent/skywalking-agent.jar -server -Duser.timezone=GMT+08 ${JAVA_OPTS} -jar /app.jar