FROM amazoncorretto:21-alpine
WORKDIR /app

# 1. Copiamos solo los archivos que definen las dependencias
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 2. "INSTALAMOS" las dependencias (este paso las descarga todas)
RUN chmod +x gradlew
RUN ./gradlew build -x test

# 3. Copiamos el código fuente (src) y arrancamos
COPY src src
EXPOSE 10000
CMD ["./gradlew", "bootRun"]