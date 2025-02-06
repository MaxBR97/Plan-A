#! /bin/bash

SCRIPT_DIR=$( dirname "${BASH_SOURCE[0]}" )
#build server
$SCRIPT_DIR/buildBackendJar.sh
#move stick jar with electron builder
(mv $SCRIPT_DIR/../dev/Backend/target/artifactid-0.0.1-SNAPSHOT.jar $SCRIPT_DIR/../dev/Frontend/resources)

(cd $SCRIPT_DIR/../dev/Frontend && npm run electron-build)
(mv $SCRIPT_DIR/../dev/Frontend/Distribution $SCRIPT_DIR/../)
