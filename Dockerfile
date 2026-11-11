# ============================================================================
# ColheAi - API (GS 2026/2 - Domain Driven Design Using Java)
# Since Young - 1TDSPV-2026
# Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
# ----------------------------------------------------------------------------
# Imagem Docker da API, usada pelo Render (que nao tem runtime Java nativo).
#
# Multi-stage (dois estagios):
#   1. build: imagem com JDK 21 + Maven, so para compilar e gerar o .jar;
#   2. execucao: imagem so com a JRE 21, recebendo apenas o .jar pronto.
# A imagem final fica menor e nao leva Maven, codigo-fonte nem credenciais.
# Usuario e senha do banco NAO estao aqui: chegam em tempo de execucao pelas
# variaveis de ambiente DB_URL, DB_USERNAME e DB_PASSWORD (painel do Render).
#
# Para testar na maquina local (com Docker instalado), na pasta do projeto:
#   docker build -t colheai-api .
#   docker run -p 8080:8080 -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... colheai-api
# ============================================================================

# ---------- Estagio 1: compilacao ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Primeiro so o pom.xml: o Docker guarda as dependencias em cache e, se so o
# codigo mudar, o proximo build nao baixa tudo de novo.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Estagio 2: execucao ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/target/colheai-api-1.0.0.jar app.jar

# A API nao roda como root: se fosse invadida, nao teria permissao de administrador no container.
USER 1000:1000

# O Render informa a porta pela variavel PORT; o application.properties usa
# server.port=${PORT:8080}, entao localmente continua 8080.
EXPOSE 8080

# -Duser.timezone: o servidor do Render roda em UTC; sem isso, uma proposta enviada
#   depois das 21h (horario de Brasilia) ficaria com a data do dia seguinte.
# -XX:MaxRAMPercentage=75: o plano gratuito tem 512 MB; a JVM pode usar ate 75% deles.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
