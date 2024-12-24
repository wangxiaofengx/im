# 使用 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 将构建好的 JAR 文件复制到容器中的工作目录
COPY target/video-conference.jar /app/video-conference.jar

# 公开容器的端口（Spring Boot 默认是 8080）
EXPOSE 9900

# 启动 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "/app/video-conference.jar"]
