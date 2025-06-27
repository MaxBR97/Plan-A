const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');
const { spawn } = require('child_process'); // Import spawn from child_process
const http = require('http');

let mainWindow;
let jarProcess; // Variable to hold the reference to the JAR process
let port = 4000;
const checkServer = (retries = 60) => {
  return new Promise((resolve, reject) => {
    let attempts = 0;
    
    const checkConnection = () => {
      console.log(`Attempting to connect to server (attempt ${attempts + 1}/${retries})...`);
      http.get(`http://localhost:${port}`, (res) => {
        console.log(`Server responded with status: ${res.statusCode}`);
        if (res.statusCode === 200) {
          console.log('Server connection successful!');
          resolve();
        } else {
          console.log(`Server responded with unexpected status: ${res.statusCode}`);
          tryAgain();
        }
      }).on('error', (err) => {
        console.log(`Connection attempt failed: ${err.message}`);
        tryAgain();
      });
    };

    const tryAgain = () => {
      attempts++;
      if (attempts >= retries) {
        console.error(`Failed to connect to server after ${retries} attempts`);
        reject(new Error(`Server failed to start after ${retries} attempts`));
        return;
      }
      // Wait 1 second before next attempt
      setTimeout(checkConnection, 1000);
    };

    checkConnection();
  });
};

const createWindow = async () => {
  mainWindow = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      nodeIntegration: true,     
      contextIsolation: false,   
      enableRemoteModule: true  
    },
  });

  // Open DevTools automatically
  mainWindow.webContents.openDevTools();

  const startPath = app.isPackaged 
    ? path.join(app.getAppPath(), 'build', 'index.html')
    : path.join(__dirname, 'index.html');

  // mainWindow.webContents.openDevTools();

  try {
    // Wait for server to be ready
    await checkServer();
    // Now load the URL
    mainWindow.loadURL(`http://localhost:${port}`);
  } catch (err) {
    console.error('Failed to connect to server:', err);
    // Handle the error - maybe show an error page
    mainWindow.loadFile(path.join(__dirname, 'error.html'));
  }

  // Optional: Remove default Electron menu
  mainWindow.removeMenu();

  // Handle window close
  mainWindow.on('closed', () => {
    mainWindow = null;
    if (jarProcess) {
      if (process.platform === 'win32') {
        spawn('taskkill', ['/pid', jarProcess.pid, '/f', '/t']);
      } else {
        jarProcess.kill('SIGTERM');
      }
      jarProcess = null;
    }
  });
};

const startJarProcess = () => {
  // Path to your JAR file
  const jarPath = path.join(process.resourcesPath, 'resources', 'artifactid-0.0.1-SNAPSHOT.jar');
  console.log("Looking for JAR at:", jarPath);
  
  if (!fs.existsSync(jarPath)) {
    console.error('JAR file not found at:', jarPath);
    console.error('Available files in resources directory:');
    try {
      const resourceDir = path.join(process.resourcesPath, 'resources');
      if (fs.existsSync(resourceDir)) {
        const files = fs.readdirSync(resourceDir);
        files.forEach(file => console.error('  -', file));
      } else {
        console.error('Resources directory does not exist:', resourceDir);
      }
    } catch (err) {
      console.error('Error reading resources directory:', err.message);
    }
    return;
  }

  console.log('JAR file found, starting server...');

  // Get the paths for storage and database
  console.log('process.resourcesPath:', process.resourcesPath);
  const basePath = path.join(process.resourcesPath, 'resources');
  const storagePath = path.join(basePath, 'data');
  const dbPath = path.join(basePath);
  
  console.log('storagePath:', storagePath);
  console.log('dbPath:', dbPath);

  // Ensure directories exist
  if (!fs.existsSync(storagePath)) {
    console.log('Creating storage directory:', storagePath);
    fs.mkdirSync(storagePath, { recursive: true });
  }

  // Spawn the JAR process with correct paths
  const javaArgs = [
    '-jar', 
    jarPath,
    `--server.port=${port}`,
    `app.file.storage-dir=${storagePath}`,
    `spring.datasource.url=jdbc:h2:file:${dbPath};MODE=MySQL`,
    `spring.profiles.active=H2,securityAndGateway,streamSolver`
  ];
  
  console.log('Starting JAR with args:', javaArgs);
  
  jarProcess = spawn('java', javaArgs);

  // Optional: Log output from the JAR process
  jarProcess.stdout.on('data', (data) => {
    console.log(`JAR stdout: ${data}`);
  });

  jarProcess.stderr.on('data', (data) => {
    console.error(`JAR stderr: ${data}`);
  });

  jarProcess.on('error', (error) => {
    console.error('Failed to start JAR process:', error);
  });

  jarProcess.on('close', (code) => {
    console.log(`JAR process exited with code ${code}`);
    jarProcess = null;
  });
};

app.whenReady().then(async () => {
  startJarProcess();
  await createWindow();

  app.on('activate', async () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      await createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    if (jarProcess) {
      if (process.platform === 'win32') {
        spawn('taskkill', ['/pid', jarProcess.pid, '/f', '/t']);
      } else {
        jarProcess.kill('SIGTERM');
      }
      jarProcess = null;
    }
    app.quit();
  }
});

// Ensure the JAR process is killed when the Electron app quits
app.on('will-quit', () => {
  if (jarProcess) {
    if (process.platform === 'win32') {
      spawn('taskkill', ['/pid', jarProcess.pid, '/f', '/t']);
    } else {
      jarProcess.kill('SIGTERM');
    }
    jarProcess = null;
  }
});