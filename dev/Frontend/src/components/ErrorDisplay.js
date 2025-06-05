import React from 'react';
import './ErrorDisplay.css';

const cleanErrorMessage = (message) => {
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

  // Remove "Error: " prefix if it exists
  cleanedMessage = cleanedMessage.replace(/^Error:\s*/i, '');
  
  // Split by either \r\n or \n and filter out empty lines
  const lines = cleanedMessage.split(/\r?\n/).filter(line => line.trim());
  return lines;
};

const parseErrorMessage = (error) => {
  try {
    const messageLines = cleanErrorMessage(error);
    
    // Check if it's a module conflict error
    if (messageLines.length > 0 && messageLines[0].includes('Module conflicts detected')) {
      return {
        title: 'Module Conflict Detected',
        details: messageLines.slice(1),
        type: 'conflict'
      };
    }

    // Default error format
    return {
      title: 'Configuration Error',
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

const ErrorDisplay = ({ error, className = '' }) => {
  if (!error) return null;

  const parsedError = parseErrorMessage(error);

  return (
    <div className={`error-container ${className}`}>
      <div className="error-title">
        <i className="error-icon">⚠️</i>
        {parsedError.title}
      </div>
      <div className="error-details">
        {parsedError.details.map((detail, index) => (
          <div key={index} className="error-detail-line">
            {detail.startsWith('-') ? (
              <strong>{detail}</strong>
            ) : detail.startsWith('*') ? (
              <span className="error-detail-bullet">{detail}</span>
            ) : (
              detail
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default ErrorDisplay; 