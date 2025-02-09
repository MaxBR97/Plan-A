#!/bin/bash
# installers/linux/preinst

set -e  # Exit on error
set -x  # Print commands being executed

echo "Running preInstall!"

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

# Install SCIP
if [ -f /opt/plan-a/installers/scipoptsuite.deb ] && [ "$PKG_MANAGER" = "apt" ]; then
    echo "Installing SCIP from .deb package"
    dpkg -i /opt/plan-a/installers/scipoptsuite.deb
elif [ -f /opt/plan-a/installers/SCIPOptSuite.sh ]; then
    echo "Installing SCIP from .sh installer"
    chmod +x /opt/plan-a/installers/SCIPOptSuite.sh
    mkdir -p /opt/scipoptsuite
    /opt/plan-a/installers/SCIPOptSuite.sh --prefix=/opt/scipoptsuite --skip-license
    ln -sf /opt/scipoptsuite/bin/scip /usr/local/bin/scip
else
    echo "ERROR: No SCIP installer found in /opt/plan-a/installers/"
    ls -la /opt/plan-a/installers/
    exit 1
fi

# Add SCIP to PATH if not already present
SCIP_PATH="/usr/local/bin"
if ! grep -q "SCIP_PATH" /etc/environment; then
    echo "PATH=$PATH:$SCIP_PATH" >> /etc/environment
    source /etc/environment
fi