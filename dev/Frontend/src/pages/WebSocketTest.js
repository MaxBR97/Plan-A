import React, { useState, useRef, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import './WebSocketTest.css';
import SolutionResultsPage from "./SolutionResultsPage.js";
import { useZPL } from "../context/ZPLContext";

const WebSocketTest = () => {
  const {
    image,
    model,
    solutionResponse,
    updateImage,
    updateImageField,
    updateModel,
    updateSolutionResponse,
    initialImageState
  } = useZPL();

  const [connected, setConnected] = useState(false);
  const [serverUrl, setServerUrl] = useState('ws://localhost:4000/scip-websocket'); // ws:// for native WebSocket
  const [messages, setMessages] = useState([]);
  const [topic, setTopic] = useState('/topic/progress');
  const [destination, setDestination] = useState('/app/message');
  const [messageContent, setMessageContent] = useState('{}');
  const [solveCommandJSON, setSolveCommandJSON] = useState('{"parameter": "value"}');
  const [globalSelectedTuples, setGlobalSelectedTuples] = useState({});

  const stompClient = useRef(null);
  const sessionId = "test-session";
  const baseUrl = "http://localhost:3000";

  const messageLogRef = useRef(null);

  useEffect(() => {
    if (messageLogRef.current) {
      messageLogRef.current.scrollTop = messageLogRef.current.scrollHeight;
    }
  }, [messages]);

  const logMessage = (message) => {
    const timestamp = new Date().toLocaleTimeString();
    setMessages(prev => [...prev, `[${timestamp}] ${message}`]);
  };

  const connect = () => {
    logMessage("Connecting to " + serverUrl);
    try {
      stompClient.current = new Client({
        brokerURL: serverUrl, // Native WebSocket only
        connectHeaders: {
          'X-Requested-With': 'XMLHttpRequest'
        },
        debug: (str) => logMessage("STOMP: " + str),
        onConnect: (frame) => {
          setConnected(true);
          logMessage("Connected: " + frame);
        },
        onStompError: (error) => {
          logMessage("STOMP Error: " + JSON.stringify(error));
          setConnected(false);
        },
        onWebSocketClose: () => {
          setConnected(false);
          logMessage("WebSocket closed");
        },
        reconnectDelay: 120000
      });

      stompClient.current.activate();
    } catch (err) {
      logMessage("Connection error: " + err.message);
    }
  };

  const disconnect = () => {
    if (stompClient.current && stompClient.current.connected) {
      stompClient.current.deactivate();
      stompClient.current = null;
      setConnected(false);
      logMessage("Disconnected");
    }
  };

  const subscribe = () => {
    if (!stompClient.current || !connected) {
      logMessage("Not connected!");
      return;
    }

    stompClient.current.subscribe(topic, (message) => {
      logMessage("Received: " + message.body);
    });

    logMessage("Subscribed to: " + topic);
  };

  const sendMessage = () => {
    if (!stompClient.current || !connected) {
      logMessage("Not connected!");
      return;
    }

    try {
      const jsonContent = JSON.parse(messageContent);
      stompClient.current.publish({
        destination: destination,
        body: JSON.stringify(jsonContent)
      });
      logMessage("Sent to " + destination + ": " + messageContent);
    } catch (e) {
      logMessage("Error parsing JSON: " + e.message);
    }
  };

  const makeHttpRequest = (endpoint, method, body = null,flag) => {
    const options = {
      method: method,
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    };

    if (body) {
      options.body = body;
    }

    fetch(baseUrl + endpoint, options)
      .then(response => {
        logMessage(`Response status: ${response.status}`);
        
        // if(flag){
          
        //   const responseText = response.text();

        //   if (!response.ok) {
        //       console.error("Server returned an error:", responseText);
        //       throw new Error(`HTTP Error! Status: ${response.status} - ${responseText}`);
        //   }

        //   const data = JSON.parse(responseText);
        //   console.log("Solve response: ", data);

        //   updateSolutionResponse(data);
        // }
        return response.text();
      })
      .then(text => {logMessage(`${method} ${endpoint}: ${text}`); console.log(JSON.parse(text)); if(flag) updateSolutionResponse(JSON.parse(text))})
      .catch(error => logMessage(`Error: ${error.message}`));
  };

  const startSolve = () => makeHttpRequest('/solve/start', 'POST', solveCommandJSON,true);
  const pauseSolve = () => makeHttpRequest(`/solve/pause`, 'POST');
  const continueSolve = () => makeHttpRequest(`/solve/continue`, 'POST',solveCommandJSON,true);
  const pollSolve = () => makeHttpRequest(`/solve/poll`, 'POST');
  const pollSolution = () => makeHttpRequest(`/solve/pollSolution`, 'POST', null , true);
  const finishSolve = () => makeHttpRequest(`/solve/finish`, 'POST');
  const clearMessages = () => setMessages([]);
  
  useEffect(() => {
    return () => {
      if (stompClient.current) {
        stompClient.current.deactivate();
      }
    };
  }, []);
  
  return (
    <div className="websocket-tester">
      <h1>SCIP WebSocket Test Client</h1>
      
      {/* Connection Card */}
      <div className="card">
        <h2>Connection</h2>
        <input 
          type="text" 
          className="input-field"
          value={serverUrl} 
          onChange={(e) => setServerUrl(e.target.value)}
          placeholder="WebSocket URL" 
        />
        <div className="button-container">
          <button 
            onClick={connect} 
            disabled={connected}
            className="button button-blue"
          >
            Connect
          </button>
          <button 
            onClick={disconnect} 
            disabled={!connected}
            className="button button-red"
          >
            Disconnect
          </button>
          <span className={connected ? "status status-connected" : "status status-disconnected"}>
            {connected ? "Connected" : "Disconnected"}
          </span>
        </div>
      </div>
      
      {/* Subscribe Card */}
      <div className="card">
        <h2>Subscribe to Topic</h2>
        <input 
          type="text" 
          className="input-field"
          value={topic} 
          onChange={(e) => setTopic(e.target.value)}
          placeholder="Topic to subscribe to" 
        />
        <button 
          onClick={subscribe}
          disabled={!connected}
          className="button button-blue"
        >
          Subscribe
        </button>
      </div>
      
      {/* Send Message Card */}
      <div className="card">
        <h2>Send Message</h2>
        <input 
          type="text" 
          className="input-field"
          value={destination} 
          onChange={(e) => setDestination(e.target.value)}
          placeholder="Destination" 
        />
        <textarea 
          value={messageContent} 
          onChange={(e) => setMessageContent(e.target.value)}
          placeholder="Message content (JSON)" 
          className="textarea-field"
          rows="3"
        />
        <button 
          onClick={sendMessage}
          disabled={!connected}
          className="button button-blue"
        >
          Send Message
        </button>
      </div>
      
      {/* HTTP Endpoints Card */}
      <div className="card">
        <h2>HTTP Endpoints</h2>
        <div className="action-group">
          <input 
            type="text" 
            className="input-field"
            value={solveCommandJSON} 
            onChange={(e) => setSolveCommandJSON(e.target.value)}
            placeholder="Solve command JSON" 
          />
        </div>
        <div className="button-grid">
          <button onClick={startSolve} className="button button-green">
            Start Solve
          </button>
          <button onClick={pauseSolve} className="button button-yellow">
            Pause Solve
          </button>
          <button onClick={continueSolve} className="button button-blue">
            Continue Solve
          </button>
          <button onClick={pollSolve} className="button button-yellow">
            Poll Solve
          </button>
          <button onClick={pollSolution} className="button button-yellow">
            Poll Solution
          </button>
          <button onClick={finishSolve} className="button button-red">
            Finish Solve
          </button>
          <button onClick={clearMessages} className="button button-red">
            clear messages
          </button>
        </div>
      </div>
      
      {/* Message Log Card */}
      <div className="card">
        <h2>Message Log</h2>
        <div className="message-log" ref={messageLogRef}>
          {messages.map((msg, index) => (
            <div key={index}>{msg}</div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default WebSocketTest;