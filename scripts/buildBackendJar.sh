#! /bin/bash
# This script will create a jar file which will be located under dev/Backend/target/
SCRIPT_DIR=$( dirname "${BASH_SOURCE[0]}" )

cd $SCRIPT_DIR/../dev/Backend
mvn clean generate-sources package -DskipTests
