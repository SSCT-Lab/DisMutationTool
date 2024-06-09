# Use the official OpenJDK 8 image as the base image
FROM openjdk:8-jdk

# Install required packages: unzip, and maven
RUN apt-get update && apt-get install -y \
    maven \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Create a directory for the RocketMQ source
RUN mkdir -p /usr/local/src/rocketmq

COPY rocketmq-all-5.2.0-source-release.zip /usr/local/src/rocketmq/

# Change to the RocketMQ directory
WORKDIR /usr/local/src/rocketmq

# Unzip the RocketMQ source
RUN unzip rocketmq-all-5.2.0-source-release.zip && rm rocketmq-all-5.2.0-source-release.zip

# Print Java, Maven, and RocketMQ directory to confirm installation
RUN java -version && mvn -version && ls -la

# Set the working directory to the RocketMQ source code directory
WORKDIR /usr/local/src/rocketmq/rocketmq-all-5.2.0-source-release

RUN mvn clean install -Dmaven.test.skip=true

# Default command to keep the container running
CMD ["bash"]
