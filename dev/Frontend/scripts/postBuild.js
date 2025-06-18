const fs = require("fs");
const path = require("path");

// === Paths ===
const frontendPath = path.resolve(__dirname, "..");
const buildPath = path.join(frontendPath, "build");
const electronPath = path.join(frontendPath, "electron.js");

const backendStaticPath = path.resolve(frontendPath, "../Backend/src/main/resources/static");
const backendResourcesPath = path.resolve(frontendPath, "../Backend/src/main/resources");
const configPath = path.join(backendStaticPath, "config.json");
const applicationPropertiesPath = path.join(backendResourcesPath, "application.properties");

// === 1. Copy electron.js ===
if (fs.existsSync(electronPath)) {
  fs.copyFileSync(electronPath, path.join(buildPath, "electron.js"));
  console.log("✓ Copied electron.js to build/");
} else {
  console.warn("⚠️ electron.js not found — skipping copy.");
}

// === 2. Clear backend static folder ===
if (fs.existsSync(backendStaticPath)) {
  fs.rmSync(backendStaticPath, { recursive: true, force: true });
}
fs.mkdirSync(backendStaticPath, { recursive: true });
console.log("✓ Cleaned static/ directory");

// === 3. Copy build/* to static/ ===
function copyRecursive(src, dest) {
  const items = fs.readdirSync(src, { withFileTypes: true });
  for (const item of items) {
    const srcPath = path.join(src, item.name);
    const destPath = path.join(dest, item.name);
    if (item.isDirectory()) {
      fs.mkdirSync(destPath, { recursive: true });
      copyRecursive(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}
copyRecursive(buildPath, backendStaticPath);
console.log("✓ Copied build/ contents to static/");

// === 4. Read port from application.properties ===
let port = null;
const appPropsContent = fs.readFileSync(applicationPropertiesPath, "utf-8");
for (const line of appPropsContent.split("\n")) {
  const match = line.match(/^server\.port\s*=\s*(\d+)/);
  if (match) {
    port = match[1];
    break;
  }
}
if (!port) {
  throw new Error("❌ Could not find 'server.port' in application.properties");
}
console.log(`✓ Found server.port = ${port}`);

// === 5. Update config.json ===
const configContent = fs.readFileSync(configPath, "utf-8");
const config = JSON.parse(configContent);
config.API_PORT = port;
fs.writeFileSync(configPath, JSON.stringify(config, null, 2));
console.log("✓ Updated config.json with API_PORT =", port);