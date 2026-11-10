# ---------- Stage 1: build the jar ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy only the build files first, so dependency downloads are cached
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline

# Now copy the source and build (tests already run in CI)
COPY src src
RUN ./mvnw -B package -DskipTests

# Split the jar into layers: libraries first, our code last
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# ---------- Stage 2: the small runtime image ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# A normal user, so the app doesn't run as root
RUN useradd --system --uid 1001 app

COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

USER app
EXPOSE 8080

# Use up to 75% of the container's memory for the Java heap
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]