### backend
FROM maven:3.9.9-eclipse-temurin-21 AS build_java

WORKDIR /workspace/

COPY ./backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY ./backend .

# Build the selected Maven module together with any modules it depends on.
RUN mvn --batch-mode -am -DskipTests package \
    && JAR_NAME="$(mvn --batch-mode -q -DforceStdout help:evaluate -Dexpression=project.build.finalName).jar" \
    && JAR_PATH="./target/${JAR_NAME}" \
    && test -f "${JAR_PATH}" \
    && cp "${JAR_PATH}" /tmp/app.jar


FROM eclipse-temurin:21-jre AS runtime

### frontend
FROM denoland/deno:debian AS build_deno

WORKDIR /build


# Compile the main app so that it doesn't need to be compiled each startup/entry.
COPY frontend/deno.lock frontend/package.json .
RUN deno ci

# These steps will be re-run upon each file change in your working directory:
COPY frontend .
RUN deno run build

FROM runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends nginx \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# The port that your application listens to.

EXPOSE 80
EXPOSE 8080

COPY frontend/nginx.conf /etc/nginx/sites-enabled/default
COPY --from=build_deno /build/dist /usr/share/nginx/html
COPY --from=build_java /tmp/app.jar ./app.jar

# ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["bash", "-lc", "\
  java -jar app.jar & JAVA_PID=$!; \
  nginx -g 'daemon off;' & NGINX_PID=$!; \
  trap 'kill -TERM $JAVA_PID $NGINX_PID; wait' TERM INT; \
  wait -n; \
  kill -TERM $JAVA_PID $NGINX_PID; \
  wait \
"]