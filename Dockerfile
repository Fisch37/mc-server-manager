### backend
FROM maven:3.9.16-eclipse-temurin-25-alpine AS build_java

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

### frontend
FROM denoland/deno:debian AS build_deno

WORKDIR /build


# Compile the main app so that it doesn't need to be compiled each startup/entry.
COPY frontend/deno.lock frontend/package.json .
RUN deno ci

# These steps will be re-run upon each file change in your working directory:
COPY frontend .
RUN deno run build

FROM ubuntu AS runtime

RUN sed -i 's/^Components: main$/& multiverse/' /etc/apt/sources.list.d/ubuntu.sources
RUN dpkg --add-architecture i386 && apt-get update
RUN apt-get install -y --no-install-recommends nginx openjdk-25-jre-headless
RUN echo steam steam/question select "I AGREE" | debconf-set-selections \
  && echo steam steam/license note '' | debconf-set-selections \
  && apt-get install -y --no-install-recommends steamcmd

RUN rm -rf /var/lib/apt/lists/*

WORKDIR /app

# The port that your application listens to.

EXPOSE 80
EXPOSE 8080


VOLUME /var/mcsm/
RUN mkdir /var/mcsm

FROM runtime

COPY frontend/nginx.conf /etc/nginx/sites-enabled/default
COPY --from=build_deno /build/dist /usr/share/nginx/html
COPY --from=build_java /tmp/app.jar ./app.jar

# ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["bash", "-lc", "\
  su $(id -nu 1000) -c \"java -jar app.jar & JAVA_PID=$!\"; \
  nginx -g 'daemon off;' & NGINX_PID=$!; \
  trap 'kill -TERM $JAVA_PID $NGINX_PID; wait' TERM INT; \
  wait -n; \
  kill -TERM $JAVA_PID $NGINX_PID; \
  wait \
"]