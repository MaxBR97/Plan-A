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
    # Install SCIP dependencies
    RUN apt-get update && apt-get install -y maven \
        vim \
        jq \
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

    # ENV AWS_ACCESS_KEY_ID=""
    # ENV AWS_SECRET_ACCESS_KEY=""
    # ENV AWS_DEFAULT_REGION="us-east-1"
    # ENV AWS_SESSION_TOKEN=""
    # ENV PUBLIC_URL="https://localhost"
    # ENV PUBLIC_PORT="443"
    # ENV PUBLIC_KEYCLOAK_URL="https://localhost:8080"

    WORKDIR /Plan-A
    COPY . .

    RUN dpkg -i SCIPOptSuite-9.2.0-Linux-ubuntu24.deb && \
        cd /Plan-A/dev/Frontend && npm install && npm run build && \
        cd /Plan-A/dev/Backend && mvn clean install -DskipTests && mvn clean compile -DskipTests && \
        rm -rf /Plan-A/dev/Frontend && rm -rf /Plan-A/SCIPOptSuite-9.2.0-Linux-ubuntu24.deb

    # Convert script to Unix line endings and make it executable
    RUN dos2unix scripts/containerEntryScript.sh && \
    chmod +x scripts/containerEntryScript.sh

    EXPOSE 3000 4000

    CMD ["/bin/bash", "-c" , "scripts/containerEntryScript.sh"]
