const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');
const { spawn } = require('child_process'); // Import spawn from child_process
const http = require('http');

let mainWindow;
let jarProcess; // Variable to hold the reference to the JAR process
let port = 4000;
const checkServer = (retries = 30) => {
  return new Promise((resolve, reject) => {
    let attempts = 0;
    
    const checkConnection = () => {
      http.get(`http://localhost:${port}`, (res) => {
        if (res.statusCode === 200) {
          resolve();
        } else {
          tryAgain();
        }
      }).on('error', (err) => {
        tryAgain();
      });
    };

    const tryAgain = () => {
      attempts++;
      if (attempts >= retries) {
        reject(new Error('Server failed to start'));
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
      //preload: path.join(__dirname, 'preload.js'), // Optional
      nodeIntegration: true,     
      contextIsolation: false,   
      enableRemoteModule: true  
    },
  });

  const startPath = app.isPackaged 
    ? path.join(app.getAppPath(), 'build', 'index.html')
    : path.join(__dirname, 'index.html');

  mainWindow.webContents.openDevTools();

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
};

const startJarProcess = () => {
  // Path to your JAR file
  const jarPath = path.join(process.resourcesPath, 'resources', 'artifactid-0.0.1-SNAPSHOT.jar');
  //console.log("look for jar at: ", path.join(process.resourcesPath, 'resources', 'artifactid-0.0.1-SNAPSHOT.jar'))
  if (!fs.existsSync(jarPath)) {
    console.error('JAR file not found!');
    return;
  }

  // Spawn the JAR process
  jarProcess = spawn('java', ['-jar', jarPath,`--server.port=${port}`, `app.file.storage-dir=./User/Models` /*,arg1,arg2*/ ]);

  // Optional: Log output from the JAR process
  jarProcess.stdout.on('data', (data) => {
    console.log(`JAR stdout: ${data}`);
  });

  jarProcess.stderr.on('data', (data) => {
    console.error(`JAR stderr: ${data}`);
  });

  jarProcess.on('close', (code) => {
    console.log(`JAR process exited with code ${code}`);
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
    app.quit();
  }
});

// Ensure the JAR process is killed when the Electron app quits
app.on('will-quit', () => {
  if (jarProcess) {
    // Force kill on Windows - win32 will match both windows 32 bit and 64 bit
    if (process.platform === 'win32') {
      spawn('taskkill', ['/pid', jarProcess.pid, '/f', '/t']);
    } else {
      jarProcess.kill('SIGTERM'); // More explicit signal
    }
  }
});