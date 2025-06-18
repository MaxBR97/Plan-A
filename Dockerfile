    FROM ubuntu:24.04

    # Prevent interactive prompts during package installation
    ENV DEBIAN_FRONTEND=noninteractive

    # Add universe repository and update
    RUN apt-get update && \
        apt-get install -y software-properties-common && \
        add-apt-repository universe && \
        apt-get update && \
        apt-get upgrade -y && \
        rm -rf /var/lib/apt/lists/*

    # Install basic build tools and dependencies
    RUN apt-get update && apt-get upgrade -y && apt-get install -y \
        default-jre \
        default-jdk \
        build-essential \
        curl \
        wget \
        gnupg \
        bash \
        dos2unix && \
        rm -rf /var/lib/apt/lists/*

    # Install NodeJS from NodeSource repository
    RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
        apt-get update && \
        apt-get install -y nodejs && \
        rm -rf /var/lib/apt/lists/*

    # Install Maven from their repository
    RUN apt-get update && apt-get install -y maven && \
        rm -rf /var/lib/apt/lists/*

    # Install SCIP dependencies
    RUN apt-get update && apt-get install -y \
        libblas3 \
        libboost-program-options1.83.0 \
        libboost-serialization1.83.0 \
        libcliquer1 \
        libgfortran5 \
        liblapack3 \
        libmetis5 \
        libopenblas0 \
        libtbb12 && \
        rm -rf /var/lib/apt/lists/*

    ENV AWS_ACCESS_KEY_ID=""
    ENV AWS_SECRET_ACCESS_KEY=""
    ENV AWS_DEFAULT_REGION="us-east-1"
    ENV AWS_SESSION_TOKEN=""

    WORKDIR /Plan-A
    COPY . .

    RUN dpkg -i SCIPOptSuite-9.2.0-Linux-ubuntu24.deb
    RUN cd /Plan-A/dev/Frontend && npm install
    RUN cd /Plan-A/dev/Frontend && npm run cross-platform-build
    RUN cd /Plan-A/dev/Backend && mvn clean install -DskipTests
    RUN cd /Plan-A/dev/Backend && mvn clean compile -DskipTests


    EXPOSE 3000 4000

    CMD ["/bin/bash", "-c" , "cd dev/Backend && mvn spring-boot:run"]
