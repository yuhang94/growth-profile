# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-21 AS builder

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

WORKDIR /build

# 配置 GitHub Packages Maven 仓库认证
RUN mkdir -p /root/.m2 && \
    echo '<settings><servers>\
      <server><id>github-bom</id><username>'"${GITHUB_ACTOR}"'</username><password>'"${GITHUB_TOKEN}"'</password></server>\
      <server><id>github-common</id><username>'"${GITHUB_ACTOR}"'</username><password>'"${GITHUB_TOKEN}"'</password></server>\
    </servers></settings>' > /root/.m2/settings.xml

# 复制 POM 先下载依赖（利用 Docker 缓存）
COPY pom.xml .
COPY growth-profile-api/pom.xml growth-profile-api/pom.xml
COPY growth-profile-service/pom.xml growth-profile-service/pom.xml
RUN mvn dependency:go-offline -q || true

# 复制源码并构建
COPY . .
RUN mvn clean package -DskipTests -q

# 清理敏感信息
RUN rm -f /root/.m2/settings.xml

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/growth-profile-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
