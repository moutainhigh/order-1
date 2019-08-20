FROM hub.c.163.com/library/java:8-alpine

MAINTAINER zoomzlin 1109391515@qq.com

ADD server/target/*.jar app.jar

EXPOSE 8081

COPY  ./agent/ /agent/
COPY  ./start.sh  start.sh

CMD ["sh","start.sh"]