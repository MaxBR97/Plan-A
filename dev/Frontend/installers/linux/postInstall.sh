#!/bin/bash

SCRIPT_DIR=$( dirname "${BASH_SOURCE[0]}" )
echo "RUNNING postInstall FROM DIR: $SCRIPT_DIR"

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Detect package manager
if command_exists apt-get; then
    PKG_MANAGER="apt"
elif command_exists dnf; then
    PKG_MANAGER="dnf"
elif command_exists yum; then
    PKG_MANAGER="yum"
elif command_exists pacman; then
    PKG_MANAGER="pacman"
elif command_exists zypper; then
    PKG_MANAGER="zypper"
else
    echo "No supported package manager found"
    exit 1
fi

# Ensure plan-a is symlinked to /usr/bin
APP_NAME="plan-a"
INSTALL_PATH="/opt/Plan-A"
BIN_PATH="/usr/bin/$APP_NAME"

echo "Checking if $APP_NAME is properly installed in PATH..."

if [ -f "$INSTALL_PATH/$APP_NAME" ]; then
    echo "Found binary at $INSTALL_PATH/$APP_NAME"

    # Remove old symlink if it exists
    if [ -L "$BIN_PATH" ]; then
        sudo rm "$BIN_PATH"
    fi

    # Create new symlink
    sudo ln -s "$INSTALL_PATH/$APP_NAME" "$BIN_PATH"
    echo "Symlink created: $BIN_PATH -> $INSTALL_PATH/$APP_NAME"
else
    echo "Error: $INSTALL_PATH/$APP_NAME not found!"
fi

# Set permissions and ownership for myJar.jar
JAR_PATH="/opt/Plan-A/resources/resources/artifactid-0.0.1-SNAPSHOT.jar"

if [ -f "$JAR_PATH" ]; then
    echo "Setting ownership and permissions for $JAR_PATH..."
    
    # Change ownership to root
    sudo chown root:root "$JAR_PATH"

    # Set permissions to rwsrwxrwx (4777)
    sudo chmod 4777 "$JAR_PATH"

    echo "Ownership and permissions set successfully for $JAR_PATH"
else
    echo "Warning: $JAR_PATH not found. Skipping ownership and permission changes."
fi

# SCIP installation process (only if SCIP is NOT already installed)
if [ "$PKG_MANAGER" = "apt" ]; then
    if command_exists scip; then
        echo "SCIP is already installed. Skipping installation."
    else
        (
            # Redirect output so it is both visible and logged
            exec > >(tee -a /tmp/scip_install.log) 2>&1
            
            echo "SCIP not found in PATH. Proceeding with installation..."
            
            # Wait for package manager to finish
            while pgrep -x 'dpkg|apt|apt-get' > /dev/null; do 
                sleep 0.6 
            done
            
            echo "Proceeding with SCIP installation..."

            # Clear conflicting environment variables
            eval $(env | grep -E 'DPKG|DEB' | awk -F= '{print "unset "$1}')
            export DEBIAN_FRONTEND=noninteractive

            # Acquire dpkg lock safely before installation
            flock --exclusive --close /var/lib/dpkg/lock \
                sudo wget -O /tmp/scipInstaller.deb https://www.scipopt.org/download/release/SCIPOptSuite-9.2.1-Linux-ubuntu24.deb && 
                sudo apt install /tmp/scipInstaller.deb -y

            echo "SCIP installation completed."
        ) &

        echo "Process is Downloading and Installing SCIP, WAIT FOR A MINUTE AFTER INSTALLATION FOR SCIP TO BE READY"

        # Wait for background process to complete before script exits
        wait
    fi
fi
