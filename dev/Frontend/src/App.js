import React, { useState, useEffect } from 'react';
import logo from './logo.svg';
import './App.css';

function App() {
  const [serverResponse, setServerResponse] = useState('');
  const [apiUrl, setApiUrl] = useState('');

  // Load configuration dynamically
  useEffect(() => {
    const loadConfig = async () => {
      try {
        const response = await fetch('/config.json'); // Fetch the runtime config
        if (!response.ok) {
          throw new Error('Failed to load configuration');
        }
        const config = await response.json();
        const { API_HOST, API_PORT } = config;
        setApiUrl(`${API_HOST}:${API_PORT}/string`); // Construct the API URL
      } catch (error) {
        console.error('Error loading configuration:', error);
        setApiUrl(''); // Handle configuration load failure
      }
    };

    loadConfig();
  }, []);

  // Function to fetch string from server
  const fetchString = async () => {
    if (!apiUrl) {
      setServerResponse('API URL not configured.');
      return;
    }

    try {
      const response = await fetch(apiUrl);
      if (!response.ok) {
        throw new Error('Failed to fetch data from server');
      }
      const data = await response.text();
      setServerResponse(data);
    } catch (error) {
      console.error('Error fetching data:', error);
      setServerResponse('Error: Unable to fetch data from the server');
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <button onClick={fetchString} className="App-button">
          Fetch String from Server
        </button>
        {serverResponse && <p className="App-response">Server response: {serverResponse}</p>}
      </header>
    </div>
  );
}

export default App;
