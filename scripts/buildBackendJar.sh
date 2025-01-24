#! /bin/bash
<<<<<<< HEAD
# This script will create a jar file which will be located under dev/Backend/target/
=======
# This script will create a jar file which will be located under dev/Backend/target
>>>>>>> a2157b37d0c07b53d3f0773ad1251dfb543e1e62
SCRIPT_DIR=$( dirname "${BASH_SOURCE[0]}" )

cd $SCRIPT_DIR/../dev/Backend
mvn clean package -DskipTests
