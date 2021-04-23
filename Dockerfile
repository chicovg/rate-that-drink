FROM openjdk:8-alpine

COPY target/uberjar/rate-that-drink.jar /rate-that-drink/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/rate-that-drink/app.jar"]
