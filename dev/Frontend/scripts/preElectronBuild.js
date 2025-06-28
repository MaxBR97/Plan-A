const fs = require('fs');
const path = require('path');

// Path to config.json
const configPath = path.join(__dirname, '..', 'public', 'config.json');
console.log(configPath);
try {
  // Read the current config
  const configContent = fs.readFileSync(configPath, 'utf8');
  const config = JSON.parse(configContent);
  
  // Save the original IS_DESKTOP value
  const originalIsDesktop = config.IS_DESKTOP;
  
  // Set IS_DESKTOP to true for Electron build
  config.IS_DESKTOP = true;
  
  // Write the updated config back to file
  fs.writeFileSync(configPath, JSON.stringify(config, null, 4));
  
  // Save the original value to a temporary file for restoration
  const tempPath = path.join(__dirname, '..', 'temp_desktop_setting.json');
  fs.writeFileSync(tempPath, JSON.stringify({ originalIsDesktop }));
  
  console.log('✅ Updated config.json: IS_DESKTOP set to true for Electron build');
  console.log('📝 Original value saved for restoration');
} catch (error) {
  console.error('❌ Error updating config.json:', error.message);
  process.exit(1);
} 