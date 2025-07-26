# Multi-stage build para otimizar o Dockerfile
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos Maven
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Dar permissão de execução ao mvnw
RUN chmod +x mvnw

# Baixar dependências
RUN ./mvnw dependency:go-offline

# Copiar código fonte
COPY src src

# Construir aplicação
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Definir diretório de trabalho
WORKDIR /app

# Copiar o JAR da stage anterior (automaticamente pega o JAR correto)
COPY --from=builder /app/target/*.jar app.jar

# Expor porta da aplicação
EXPOSE 8084

# Comando para executar a aplicação
CMD ["java", "-jar", "app.jar"]