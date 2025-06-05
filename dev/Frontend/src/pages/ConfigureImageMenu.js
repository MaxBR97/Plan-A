import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import ErrorDisplay from '../components/ErrorDisplay';
import './ConfigureImageMenu.css';

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

const ConfigureImageMenu = () => {
  const navigate = useNavigate();
  const { image, updateImage, initialImageState } = useZPL();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const menuItems = [
    {
      title: 'Configure Inputs & Outputs',
      description: 'Select variables to display and configure their input dependencies',
      path: '/configure-input-outputs'
    },
    {
      title: 'Configure Constraints',
      description: 'Create and manage constraint modules to define problem logic',
      path: '/configure-constraints'
    },
    {
      title: 'Configure Preferences',
      description: 'Set up preference modules to define optimization objectives',
      path: '/configure-preferences'
    },
    {
      title: 'Configure Solver Settings',
      description: 'Customize solver parameters and behavior',
      path: '/configure-solver-options'
    },
    {
      title: 'Configure Names & Tags',
      description: 'Set aliases and tags for better data organization',
      path: '/configure-sets-params'
    }
  ];

  const handleFinish = async () => {
    setIsLoading(true);
    setError(null);

    const patchRequestBody = {
      imageId: image.imageId,
      image
    };

    try {
      console.log("🔄 Patch request body:", patchRequestBody);

      const patchResponse = await fetch("/images", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(patchRequestBody)
      });

      if (!patchResponse.ok) {
        const errorData = await patchResponse.json();
        throw new Error(errorData.msg || errorData.message || JSON.stringify(errorData));
      }

      console.log("✅ Configuration saved successfully!");
      navigate('/solution-preview');
    } catch (error) {
      console.error("Error saving configuration:", error);
      setError(error.message || error.toString());
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuit = () => {
    // Reset image state to initial state
    updateImage(initialImageState);
    // Navigate back to home
    navigate('/');
  };

  return (
    <div className="configure-menu-container">
      <div className="configure-menu-header">
        <h1>Configure Image</h1>
        <p className="configure-menu-description">
          Welcome to the configuration menu. Here you can customize various aspects of your optimization problem.
          Follow the steps below to set up your configuration.
        </p>
        <ErrorDisplay error={error} />
      </div>

      <div className="configure-menu-grid">
        {menuItems.map((item, index) => (
          <div 
            key={index} 
            className="configure-menu-item"
            onClick={() => navigate(item.path)}
          >
            <h2>{item.title}</h2>
            <p>{item.description}</p>
            <div className="configure-menu-item-number">{index + 1}</div>
          </div>
        ))}
      </div>

      <div className="configure-menu-footer">
        <div className="button-group">
          <button 
            className="quit-button"
            onClick={handleQuit}
          >
            Quit Image Creation
          </button>
          <button 
            className="finish-button"
            onClick={handleFinish}
            disabled={isLoading}
          >
            {isLoading ? 'Saving...' : 'Finish Configuration'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfigureImageMenu; 