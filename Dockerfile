# Image de base : Java 21 (JDK) sur Ubuntu 22.04, comme dans le TP
FROM eclipse-temurin:21-jdk-jammy

# Dossier de travail dans le conteneur
WORKDIR /app

# Copier le jar construit par Maven (mvnw clean package)
COPY target/linkguard-0.0.1-SNAPSHOT.jar app.jar

# Port écouté par l'application
EXPOSE 8080

# Commande lancée au démarrage du conteneur
ENTRYPOINT ["java", "-jar", "app.jar"]
