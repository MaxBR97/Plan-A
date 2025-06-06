import React from 'react';
import './ErrorDisplay.css';

const cleanErrorMessage = (message) => {
  console.log("error msg", message);
  // If message is a stringified JSON, parse it
  let cleanedMessage = message;
  try {
    if (typeof message === 'string' && (message.startsWith('{') || message.startsWith('["'))) {
      const parsed = JSON.parse(message);
      cleanedMessage = parsed.msg || parsed.message || message;
    }
  } catch (e) {
    cleanedMessage = message;
  }
  
  // Split by either \r\n or \n and filter out empty lines
  const lines = cleanedMessage.split(/\r?\n/).filter(line => line.trim());
  return lines;
};

const parseErrorMessage = (error) => {
  try {
    // If error is a string, try to parse it as JSON
    let errorObj = error;
    if (typeof error === 'string') {
      try {
        errorObj = JSON.parse(error);
      } catch (e) {
        // If parsing fails, use the original string
        errorObj = { msg: error };
      }
    }

    const messageLines = cleanErrorMessage(errorObj.msg || errorObj.message || error);
    
    // Check if it's a module conflict error
    if (messageLines.length > 0 && messageLines[0].includes('Module conflicts detected')) {
      return {
        title: 'Module Conflict Detected',
        details: messageLines.slice(1),
        type: 'conflict'
      };
    }

    // Use the exception type if available
    const title = errorObj.exception ? `Error (${errorObj.exception})` : 'Error';

    // Default error format
    return {
      title: title,
      details: messageLines.length > 0 ? messageLines : [error.toString()],
      type: 'general'
    };
  } catch (e) {
    // Fallback for unparseable errors
    return {
      title: 'Error',
      details: [error.toString()],
      type: 'general'
    };
  }
};

const ErrorDisplay = ({ error, className = '', onClose }) => {
  if (!error) return null;

  const parsedError = parseErrorMessage(error);

  return (
    <div className={`error-container ${className}`}>
      <div className="error-header">
        <div className="error-title">
          <i className="error-icon">⚠️</i>
          {parsedError.title}
        </div>
        <button className="error-close-button" onClick={onClose}>×</button>
      </div>
      <div className="error-details">
        {parsedError.details.map((detail, index) => (
          <div key={index} className="error-detail-line">
            {detail.startsWith('-') ? (
              <strong className="error-detail-text">{detail}</strong>
            ) : detail.startsWith('*') ? (
              <span className="error-detail-bullet error-detail-text">{detail}</span>
            ) : (
              <span className="error-detail-text">{detail}</span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default ErrorDisplay; 