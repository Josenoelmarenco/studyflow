# StudyFlow — container image
#
# StudyFlow is a JavaFX *desktop* application, so this image is meant for a
# reproducible build-and-test environment and for running the app against a
# reachable MariaDB. Showing the window requires an X server on the host (see
# the run notes at the bottom); the build and the test suite need neither a
# display nor a database, because the tests run against in-memory H2.
#
# ── Stage 1: build and test ──────────────────────────────────────────────
# A single, pinned base image gives every developer and the CI agent the exact
# same Maven and JDK 21.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the build descriptor first and pre-fetch dependencies. Docker caches this
# layer, so day-to-day source changes don't re-download the whole dependency set.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Now the sources and the database scripts (the tests load db/schema.sql).
COPY src ./src
COPY db ./db

# Compile, run the full JUnit suite and enforce the JaCoCo coverage gate.
# If coverage drops below the threshold in pom.xml, the image fails to build —
# exactly the guarantee a pipeline should give.
RUN mvn -B clean verify

# ── Stage 2: runtime ─────────────────────────────────────────────────────
# Keep Maven available so the app launches through the JavaFX plugin, which
# wires the JavaFX modules onto the module path for us.
FROM maven:3.9-eclipse-temurin-21 AS runtime

WORKDIR /app

# Bring over the project and the dependencies already resolved in the build
# stage, so the runtime image does not hit the network again.
COPY --from=build /root/.m2 /root/.m2
COPY --from=build /app /app

# Database connection is supplied at run time (never baked into the image).
# These defaults assume a MariaDB reachable as the host "db".
ENV STUDYFLOW_DB_URL=jdbc:mariadb://db:3306/studyflow \
    STUDYFLOW_DB_USER=studyflow \
    STUDYFLOW_DB_PASSWORD=change-me

# Launch the JavaFX application.
CMD ["mvn", "-q", "javafx:run"]

# ── Run notes ────────────────────────────────────────────────────────────
# Build the image:
#     docker build -t studyflow .
#
# Run against a MariaDB container, forwarding the display (Linux host):
#     docker run --rm \
#       -e DISPLAY=$DISPLAY \
#       -v /tmp/.X11-unix:/tmp/.X11-unix \
#       -e STUDYFLOW_DB_URL=jdbc:mariadb://host.docker.internal:3306/studyflow \
#       -e STUDYFLOW_DB_USER=studyflow \
#       -e STUDYFLOW_DB_PASSWORD=your-password \
#       studyflow
#
# On macOS the display needs XQuartz ("Allow connections from network clients")
# and DISPLAY=host.docker.internal:0.
