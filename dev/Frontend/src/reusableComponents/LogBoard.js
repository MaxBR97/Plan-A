import { useRef, useEffect } from 'react';
import './LogBoard.css';

const LogBoard = ({ text = '', height = '200px', className = '' }) => {
  const logAreaRef = useRef(null);

  // Auto-scroll to bottom when text updates
  useEffect(() => {
    if (logAreaRef.current) {
      logAreaRef.current.scrollTop = logAreaRef.current.scrollHeight;
    }
  }, [text]);

  return (
    <div className="flex flex-col w-full h-full" style={{ flex: 1, minHeight: 0 }}>
      <div 
        ref={logAreaRef}
        className={`log-container text-sm bg-gray-900 text-green-400 p-4 rounded ${className}`}
        style={{ height: '100%', minHeight: 0, overflow: 'auto' }}
      >
        <pre style={{ margin: 0, whiteSpace: 'pre-wrap', wordWrap: 'break-word' }}>
          {text || 'No logs yet.'}
        </pre>
      </div>
    </div>
  );
};

export default LogBoard;