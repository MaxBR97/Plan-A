#!/bin/bash

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
DEBS_PATH="/usr/local/share"

# Create DEBIAN folder and control file
mkdir -p $SCRIPT_DIR/../Distribution/Plan-A/DEBIAN &&

echo -e "Package: Plan-A
Version: 1.0
Architecture: all
Maintainer: Your Name <you@example.com>
Description: Installs two .deb packages" > $SCRIPT_DIR/../Distribution/Plan-A/DEBIAN/control &&

# Create postinst script
echo -e "#!/bin/sh\n
dpkg -i $DEBS_PATH/plan-a-front_0.1.0_amd64.deb\n
dpkg -i $DEBS_PATH/scipoptsuite.deb\n
\n" > $SCRIPT_DIR/../Distribution/Plan-A/DEBIAN/postinst &&

chmod +x $SCRIPT_DIR/../Distribution/Plan-A/DEBIAN/postinst &&

# Create the necessary folder to store .deb files
mkdir -p $SCRIPT_DIR/../Distribution/Plan-A/usr/local/share &&

# Copy the .deb files to the shared folder
cp $SCRIPT_DIR/../Distribution/plan-a-front_0.1.0_amd64.deb $SCRIPT_DIR/../Distribution/linux-unpacked/resources/installers/linux/scipoptsuite.deb $SCRIPT_DIR/../Distribution/Plan-A/usr/local/share/ &&

# Build the .deb package
dpkg-deb --build $SCRIPT_DIR/../Distribution/Plan-A

