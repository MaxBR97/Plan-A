const fs = require('fs');
const path = require('path');

// Path to config.json and temp file
const configPath = path.join(__dirname, '..', 'public', 'config.json');
const tempPath = path.join(__dirname, '..', 'temp_desktop_setting.json');

try {
  // Check if temp file exists
  if (!fs.existsSync(tempPath)) {
    console.log('ℹ️  No temporary desktop setting found, skipping restoration');
    return;
  }

  // Read the original value from temp file
  const tempContent = fs.readFileSync(tempPath, 'utf8');
  const { originalIsDesktop } = JSON.parse(tempContent);
  
  // Read the current config
  const configContent = fs.readFileSync(configPath, 'utf8');
  const config = JSON.parse(configContent);
  
  // Restore the original IS_DESKTOP value
  config.IS_DESKTOP = false;
  
  // Write the updated config back to file
  fs.writeFileSync(configPath, JSON.stringify(config, null, 4));
  
  // Clean up the temp file
  fs.unlinkSync(tempPath);
  
  console.log('✅ Restored config.json: IS_DESKTOP set back to', originalIsDesktop);
  console.log('🧹 Cleaned up temporary files');
} catch (error) {
  console.error('❌ Error restoring config.json:', error.message);
  // Don't exit with error code to avoid breaking the build process
} 