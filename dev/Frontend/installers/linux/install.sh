#!/bin/bash

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to get system package manager
get_package_manager() {
    if command_exists apt; then
        echo "apt"
    elif command_exists dnf; then
        echo "dnf"
    elif command_exists yum; then
        echo "yum"
    elif command_exists pacman; then
        echo "pacman"
    else
        echo "unknown"
    fi
}

# Function to install Java
install_java() {
    PKG_MANAGER=$(get_package_manager)
    case $PKG_MANAGER in
        "apt")
            sudo apt update
            sudo apt install -y default-jre
            ;;
        "dnf"|"yum")
            sudo $PKG_MANAGER install -y java-1.8.0-openjdk
            ;;
        "pacman")
            sudo pacman -Sy jre8-openjdk --noconfirm
            ;;
        *)
            echo "Unsupported package manager. Please install Java manually."
            exit 1
            ;;
    esac
}

# Function to check Java version
check_java_version() {
    if command_exists java; then
        version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
        if [ $(echo "$version >= 1.8" | bc -l) -eq 1 ]; then
            return 0
        fi
    fi
    return 1
}

# Main installation script
echo "Checking dependencies..."

# Check and install Java
if ! check_java_version; then
    echo "Java 8 or higher is required. Installing..."
    install_java
    
    if ! check_java_version; then
        echo "Java installation failed. Please install Java 8 or higher manually."
        exit 1
    fi
fi

# Add any additional dependency checks here
# For example, checking for Python, Node.js, etc.

echo "All dependencies installed successfully!"
exit 0