#! /bin/bash

# Set default values if environment variables are not set
PUBLIC_URL=${PUBLIC_URL:-"http://localhost:4000"}
PUBLIC_KEYCLOAK_URL=${PUBLIC_KEYCLOAK_URL:-"http://localhost:8080"}

# Parse PUBLIC_URL to extract host and port
# Remove protocol (http:// or https://) and split by :
URL_WITHOUT_PROTOCOL=${PUBLIC_URL#*://}
if [[ $URL_WITHOUT_PROTOCOL == *:* ]]; then
    # Port is specified in the URL
    HOST_PART=${URL_WITHOUT_PROTOCOL%:*}
    PORT_PART=${URL_WITHOUT_PROTOCOL#*:}
else
    # No port specified, use default ports based on protocol
    HOST_PART=$URL_WITHOUT_PROTOCOL
    if [[ $PUBLIC_URL == https://* ]]; then
        PORT_PART="443"
    else
        PORT_PART="80"
    fi
fi

# Add protocol back to host
if [[ $PUBLIC_URL == https://* ]]; then
    HOST="https://$HOST_PART"
else
    HOST="http://$HOST_PART"
fi

CONFIG_FILE="/Plan-A/dev/Backend/src/main/resources/static/config.json"

# Create new JSON content
jq --arg host "$HOST" \
   --arg port "$PORT_PART" \
   --arg keycloak_url "$PUBLIC_KEYCLOAK_URL" \
   '.REACT_APP_API_HOST = $host | 
    .REACT_APP_API_PORT = $port | 
    .IS_DESKTOP = false |
    .keycloak.url = $keycloak_url |
    .keycloak.realm = "plan-a" |
    .keycloak.clientId = "spring-gateway"' \
    "$CONFIG_FILE" > "$CONFIG_FILE.tmp" && mv "$CONFIG_FILE.tmp" "$CONFIG_FILE"

# the package step should have been done at build time,
# but it causes a problem where the package embbeds the config.json file into the jar 
# prior to this script, making it not aware of the new domains and ports.
cd /Plan-A/dev/Backend && mvn package -DskipTests && java -jar target/artifactid-0.0.1-SNAPSHOT.jar --app.file.storage-dir=../../../Plan-A/dev/Backend/data