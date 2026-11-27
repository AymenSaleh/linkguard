# Image de base : JRE Java 21 sur Alpine
# (un JRE suffit pour exécuter le jar : pas de compilateur, pas de binutils, beaucoup moins de paquets)
FROM eclipse-temurin:21-jre-alpine

# Appliquer les correctifs de sécurité disponibles et créer un utilisateur sans privilèges
RUN apk upgrade --no-cache \
    && addgroup -S linkguard \
    && adduser -S -G linkguard linkguard

# Dossier de travail dans le conteneur
WORKDIR /app

# Copier le jar construit par Maven (mvnw clean package)
COPY target/linkguard-0.0.1-SNAPSHOT.jar app.jar

# Le conteneur ne tourne plus en root
USER linkguard

# Port écouté par l'application
EXPOSE 8080

# Commande lancée au démarrage du conteneur
ENTRYPOINT ["java", "-jar", "app.jar"]
