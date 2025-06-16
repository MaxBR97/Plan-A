import { useState, useCallback, useRef, useEffect, useImperativeHandle, forwardRef } from 'react';
import './LogBoard.css';

const LogBoard = forwardRef((props, ref) => {
  const [logs, setLogs] = useState('');
  const logAreaRef = useRef(null);
  const [autoScroll, setAutoScroll] = useState(true);

  useImperativeHandle(ref, () => ({
    clearLogs: () => setLogs(''),
  }));

  // Function to append text to the log
  const appendLog = useCallback((text) => {
    setLogs(prevLogs => {
      try {
        // Ensure new log entries start on a new line if there's already content
        const newText = prevLogs ? 
          (prevLogs.endsWith('\n') ? prevLogs + text : prevLogs + '\n' + text) : 
          text;
        
        // If the new text exceeds the buffer size, trim from the beginning
        if (newText.length > props.bufferSize) {
          // Find the first newline after the trimming point to keep log entries intact
          const startIndex = newText.length - props.bufferSize;
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
  }, [props.bufferSize]);

  // Auto-scroll to bottom when logs update
  useEffect(() => {
    if (autoScroll && logAreaRef.current) {
      logAreaRef.current.scrollTop = logAreaRef.current.scrollHeight;
    }
  }, [logs, autoScroll]);

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
        className={`log-container text-sm bg-gray-900 text-green-400 p-4 rounded ${props.className}`}
        style={{ height: props.height }}
        onScroll={handleScroll}
      >
        {logs || 'No logs yet.'}
      </div>
      
    </div>
  );
});

export default LogBoard;