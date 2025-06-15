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

  // Clear logs function
  const clearLogs = useCallback(() => {
    setLogs('');
  }, []);

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
      window.clearLogs = clearLogs;
    }
    
    return () => {
      if (window) {
        delete window.appendLog;
        delete window.clearLogs;
      }
    };
  }, [appendLog, clearLogs]);

  // Handle manual scroll to determine if auto-scroll should be enabled
  const handleScroll = () => {
    if (!logAreaRef.current) return;
    
    const { scrollTop, scrollHeight, clientHeight } = logAreaRef.current;
    const isScrolledToBottom = scrollHeight - scrollTop - clientHeight < 5;
    setAutoScroll(isScrolledToBottom);
  };

  // Test function to add sample logs
  const addSampleLogs = () => {
    const timestamp = new Date().toISOString();
    appendLog(`[${timestamp}] Sample log entry #${Math.floor(Math.random() * 1000)}`);
  };

  return (
    <div className="flex flex-col w-full h-full">
      
      <div 
        ref={logAreaRef}
        className={`log-container text-sm bg-gray-900 text-green-400 p-4 rounded ${className}`}
        style={{ height }}
        onScroll={handleScroll}
      >
        {logs || 'No logs yet.'}
      </div>
      
    </div>
  );
}