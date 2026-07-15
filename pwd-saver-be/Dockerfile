# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY pom.xml checkstyle.xml ./
COPY src ./src

# pom.xml pins the build to a JDK 25 *toolchain* (not just the ambient JDK),
# so Maven needs a toolchains.xml declaring where JDK 25 lives in this image.
# Written inside the cache-mounted RUN itself, since a cache mount hides
# anything written to that path by an earlier layer.
RUN --mount=type=cache,target=/root/.m2 \
    apk add --no-cache maven && \
    printf '<?xml version="1.0" encoding="UTF-8"?>\n<toolchains>\n  <toolchain>\n    <type>jdk</type>\n    <provides>\n      <version>25</version>\n    </provides>\n    <configuration>\n      <jdkHome>%s</jdkHome>\n    </configuration>\n  </toolchain>\n</toolchains>\n' "$JAVA_HOME" > /root/.m2/toolchains.xml && \
    mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S pwdsaver && adduser -S pwdsaver -G pwdsaver
USER pwdsaver

COPY --from=build /workspace/target/pwdsaver-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
