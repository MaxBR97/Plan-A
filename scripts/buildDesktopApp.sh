#! /bin/bash
echo "Build for linux or windows? [l|w]"
read OS
export OS
if [ "$OS" != "w" ] && [ "$OS" != "l" ]; then
    echo "wrong input: choose 'l' or 'w'!"
    exit 1;
fi

SCRIPT_DIR=$( dirname "${BASH_SOURCE[0]}" )
#build server
$SCRIPT_DIR/buildBackendJar.sh
#move stick jar with electron builder
mv -f $SCRIPT_DIR/../dev/Backend/target/artifactid-0.0.1-SNAPSHOT.jar $SCRIPT_DIR/../dev/Frontend/resources
chmod 777 $SCRIPT_DIR/../dev/Frontend/resources/artifactid-0.0.1-SNAPSHOT.jar
if [ "$OS" == "l" ]; then
    cp $SCRIPT_DIR/../SCIPOptSuite-9.2.0-Linux-ubuntu24.deb $SCRIPT_DIR/../dev/Frontend/installers/linux/scipoptsuite.deb &&
    (cd $SCRIPT_DIR/../dev/Frontend && npm run electron-build -- -$OS)
    rm -rf "$SCRIPT_DIR/../Distribution"
    mv $SCRIPT_DIR/../dev/Frontend/Distribution $SCRIPT_DIR/../
    rm $SCRIPT_DIR/../dev/Frontend/installers/linux/scipoptsuite.deb
elif [ "$OS" == "w" ]; then
    #$SCRIPT_DIR/installWine.sh &&
    #export WINEDEBUG=-all &&
    #export DISPLAY=:0 &&
    #wget -O  $SCRIPT_DIR/../dev/Frontend/installers/windows/SCIPOptSuite-installer.exe https://www.scipopt.org/download/release/SCIPOptSuite-9.2.1-win64.exe &&
    #wget -O $SCRIPT_DIR/../dev/Frontend/installers/windows/vc_redist.x64.exe https://aka.ms/vs/17/release/vc_redist.x64.exe &&
    #wget -O $SCRIPT_DIR/../dev/Frontend/installers/windows/jdk-windows.exe https://download.oracle.com/java/23/latest/jdk-23_windows-x64_bin.exe &&
    (cd $SCRIPT_DIR/../dev/Frontend && npm run electron-build -- -$OS --x64 --config.win.sign=false)
    rm -rf "$SCRIPT_DIR/../Distribution"
    mv $SCRIPT_DIR/../dev/Frontend/Distribution $SCRIPT_DIR/../
    #rm $SCRIPT_DIR/../dev/Frontend/installers/windows/SCIPOptSuite-installer.exe
    #rm $SCRIPT_DIR/../dev/Frontend/installers/windows/vc_redist.x64.exe
    #rm $SCRIPT_DIR/../dev/Frontend/installers/windows/jdk-windows.exe
else
    echo "wrong input: choose 'l' or 'w'!"
    exit 1
fi
