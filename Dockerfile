FROM openjdk:8-alpine

COPY target/uberjar/the-beer-tasting-app.jar /the-beer-tasting-app/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/the-beer-tasting-app/app.jar"]
