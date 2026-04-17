# 1. Usamos Amazon Corretto 21 (OpenJDK robusto)
FROM amazoncorretto:21-alpine

# 2. Instalamos utilidades de Linux necesarias para scripts de Windows
RUN apk add --no-cache dos2unix bash

WORKDIR /app

# 3. Copiamos los archivos de estructura de Gradle Groovy
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 4. LIMPIEZA TÉCNICA (Vital para archivos creados en Windows)
# Convertimos todos los archivos .gradle y el ejecutable a formato Linux
RUN dos2unix gradlew build.gradle settings.gradle && chmod +x gradlew

# 5. DESCARGA DE DEPENDENCIAS
# Usamos --no-daemon para ahorrar RAM en Render y evitar el Exit Code 1
RUN ./gradlew build -x test --no-daemon --stacktrace || true

# 6. Copiamos el código fuente
COPY src src

# 7. Construcción final
RUN ./gradlew build -x test --no-daemon

EXPOSE 10000

# 8. Comando de arranque
CMD ["./gradlew", "bootRun", "--no-daemon"]