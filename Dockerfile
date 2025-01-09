FROM ubuntu:24.04

RUN apt-get update && apt-get upgrade -y


RUN apt-get install -y \
    default-jre \
    default-jdk \
    build-essential \
    nodejs \
    npm \
    curl \
    maven \
    dbus

RUN apt-get install -y \
    libblas3 \
    libboost-program-options1.83.0 \
    libboost-serialization1.83.0 \
    libcliquer1 \
    libgfortran5 \
    liblapack3 \
    libmetis5 \
    libopenblas0 \
    libtbb12

WORKDIR /Plan-A
COPY . .
RUN dpkg -i SCIPOptSuite-9.2.0-Linux-ubuntu24.deb
RUN cd /Plan-A/dev/Frontend && npm install && cd /Plan-A
RUN cd /Plan-A/dev/Backend && mvn compile && mvn generate-sources && cd /Plan-A

EXPOSE 3000 4000
