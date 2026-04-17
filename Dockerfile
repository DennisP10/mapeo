# 1. Usamos la imagen oficial de Amazon Corretto 21
FROM amazoncorretto:21-alpine

# 2. Directorio de trabajo dentro del servidor de Render
WORKDIR /app

# 3. Copiamos todo el contenido de tu carpeta 'mapeo'
COPY . .

# 4. Damos permisos de ejecución al wrapper de Gradle
RUN chmod +x gradlew

# 5. Ejecutamos la instalación (el comando que pediste)
RUN ./gradlew installDist -x test

# 6. Render usa el puerto 10000 por defecto
EXPOSE 10000

# 7. Comando de arranque usando bootRun
CMD ["./gradlew", "bootRun"]