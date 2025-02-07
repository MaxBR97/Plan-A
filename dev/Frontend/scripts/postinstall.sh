#!/bin/bash
# Check if Java is installed
if ! command -v java &> /dev/null; then
  echo "Java not found. Installing..."
  sudo apt-get update
  sudo apt-get install -y openjdk-11-jre # Install OpenJDK
else
  echo "Java is already installed."
fi