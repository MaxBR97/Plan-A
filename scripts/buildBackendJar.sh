#! /bin/bash
SCRIPT_DIR=$( dirname "${BASH_SOURCE[0]}" )

cd $SCRIPT_DIR/../dev/Backend
mvn clean package -DskipTests
