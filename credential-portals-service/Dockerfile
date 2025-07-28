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



# # Multi-stage build para otimizar o Dockerfile
# # Stage 1: Build
# FROM eclipse-temurin:17-jdk-alpine AS builder

# # Configurar variáveis de ambiente para encoding
# ENV LANG=C.UTF-8
# ENV LC_ALL=C.UTF-8
# ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

# # Definir diretório de trabalho
# WORKDIR /app

# # Copiar arquivos Maven
# COPY mvnw .
# COPY mvnw.cmd .
# COPY .mvn .mvn
# COPY pom.xml .

# # Dar permissão de execução ao mvnw
# RUN chmod +x mvnw

# # Verificar versão do Java e Maven
# RUN java -version && ./mvnw --version

# # Baixar dependências
# RUN ./mvnw dependency:go-offline -q

# # Copiar código fonte
# COPY src src

# # Verificar estrutura do projeto
# RUN ls -la src/main/java/br/com/consultdg/credential_portals_service/api/model/

# # Construir aplicação com encoding explícito
# RUN ./mvnw clean package -DskipTests -e -Dmaven.compiler.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8

# # Verificar se o JAR foi criado
# RUN ls -la target/

# # Stage 2: Runtime
# FROM eclipse-temurin:17-jre-alpine

# # Configurar variáveis de ambiente para encoding
# ENV LANG=C.UTF-8
# ENV LC_ALL=C.UTF-8
# ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

# # Definir diretório de trabalho
# WORKDIR /app

# # Copiar o JAR da stage anterior (automaticamente pega o JAR correto)
# COPY --from=builder /app/target/*.jar app.jar

# # Expor porta da aplicação
# EXPOSE 8084

# # Comando para executar a aplicação
# CMD ["java", "-jar", "app.jar"]