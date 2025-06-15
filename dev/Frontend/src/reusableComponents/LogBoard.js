import { useState, useCallback, useRef, useEffect } from 'react';
import './LogBoard.css';

export default function LogBoard({ bufferSize = 1000, className = '', height = '400px' }) {
  const [logs, setLogs] = useState('');
  const logAreaRef = useRef(null);
  const [autoScroll, setAutoScroll] = useState(true);

  // Function to append text to the log
  const appendLog = useCallback((text) => {
    setLogs(prevLogs => {
      try {
        // Ensure new log entries start on a new line if there's already content
        const newText = prevLogs ? 
          (prevLogs.endsWith('\n') ? prevLogs + text : prevLogs + '\n' + text) : 
          text;
        
        // If the new text exceeds the buffer size, trim from the beginning
        if (newText.length > bufferSize) {
          // Find the first newline after the trimming point to keep log entries intact
          const startIndex = newText.length - bufferSize;
          const firstNewLineAfterTrim = newText.indexOf('\n', startIndex);
          
          if (firstNewLineAfterTrim !== -1) {
            return newText.slice(firstNewLineAfterTrim + 1);
          }
          return newText.slice(startIndex);
        }
        return newText;
      } catch (error) {
        console.error("Error in appendLog:", error);
        return prevLogs + "\nError logging message";
      }
    });
  }, [bufferSize]);

  // Auto-scroll to bottom when logs update
  useEffect(() => {
    if (autoScroll && logAreaRef.current) {
      logAreaRef.current.scrollTop = logAreaRef.current.scrollHeight;
    }
  }, [logs, autoScroll]);

  // Expose the appendLog function to parent components
  useEffect(() => {
    if (window) {
      window.appendLog = appendLog;
    }
    
    return () => {
      if (window) {
        delete window.appendLog;
      }
    };
  }, [appendLog]);

  // Handle manual scroll to determine if auto-scroll should be enabled
  const handleScroll = () => {
    if (!logAreaRef.current) return;
    
    const { scrollTop, scrollHeight, clientHeight } = logAreaRef.current;
    const isScrolledToBottom = scrollHeight - scrollTop - clientHeight < 5;
    setAutoScroll(isScrolledToBottom);
  };

  // Clear logs function
  const clearLogs = () => {
    setLogs('');
  };

  // Test function to add sample logs
  const addSampleLogs = () => {
    const timestamp = new Date().toISOString();
    appendLog(`[${timestamp}] Sample log entry #${Math.floor(Math.random() * 1000)}`);
  };

  return (
    <div className="log-board-container">
      <div 
        ref={logAreaRef}
        className={`log-container ${className}`}
        onScroll={handleScroll}
      >
        {logs || 'No logs yet. Use window.appendLog("Your log message") to add logs.'}
      </div>
    </div>
  );
}