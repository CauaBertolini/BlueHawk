# ==========================================
# Estágio 1: Build da aplicação (Maven + JDK 21)
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copia apenas os descritores do Maven para cache de dependências
COPY pom.xml .

# Baixa as dependências do projeto (otimização de cache do Docker)
RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# Garante fallback de application.properties caso não tenha sido enviado (ex.: arquivo no .gitignore)
RUN if [ ! -f src/main/resources/application.properties ] && [ -f src/main/resources/application-example.properties ]; then \
      cp src/main/resources/application-example.properties src/main/resources/application.properties; \
    fi

# Compila e gera o arquivo .jar sem rodar os testes unitários no build da imagem
RUN mvn clean package -DskipTests -B

# ==========================================
# Estágio 2: Imagem final de execução (JRE 21)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Cria usuário não-root por segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia o artefato .jar gerado no estágio anterior
COPY --from=builder /build/target/*.jar app.jar

# Permissão de execução para o usuário não-root
RUN chown -R appuser:appgroup /app
USER appuser

# Porta padrão de execução
ENV PORT=8080
ENV SERVER_PORT=${PORT}

# Parâmetros JVM otimizados para container (cgroups e limite de memória dinâmica)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Azure App Service e Container Apps escutam por padrão na 8080
EXPOSE 8080

# Inicialização do container
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
