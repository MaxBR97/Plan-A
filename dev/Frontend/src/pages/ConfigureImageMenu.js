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
  const { image, updateImage, updateImageField, fetchAndSetImage, initialImageState, deleteImage } = useZPL();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [editingName, setEditingName] = useState(false);
  const [editingDescription, setEditingDescription] = useState(false);
  const [tempName, setTempName] = useState(image.imageName);
  const [tempDescription, setTempDescription] = useState(image.imageDescription);
  console.log("image:",image);
  const menuItems = [
    {
      title: 'Configure Domain Inputs & Outputs',
      description: 'Select variables to display and configure their input dependencies',
      path: '/configure-input-outputs'
    },
    {
      title: 'Configure Constraints',
      description: 'Create and manage constraint modules to define problem logic',
      path: '/configure-constraints'
    },
    {
      title: 'Configure Optimization Goals',
      description: 'Set up optimization modules to define optimization objectives',
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
      await fetchAndSetImage(); // Fetch the latest image data
      navigate('/solution-preview');
    } catch (error) {
      console.error("Error saving configuration:", error);
      setError(error.message || error.toString());
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuit = () => {
    deleteImage()
    // Reset image state to initial state
    updateImage(initialImageState);
    // Navigate back to home
    navigate('/');
  };

  return (
    <div className="configure-menu-container">
      <div className="configure-menu-header">
        <h1>Configure Image</h1>
        <p className="configure-menu-description main-description">
          Here you can customize various aspects of your optimization problem.
        </p>
        
        <div className="image-details">
          <div className="image-details-section">
            <label>Image Name <span className="edit-hint">(double-click to edit)</span></label>
            <div 
              className="image-name" 
              onDoubleClick={() => setEditingName(true)}
            >
              {editingName ? (
                <input
                  type="text"
                  value={tempName}
                  onChange={(e) => setTempName(e.target.value)}
                  onBlur={() => {
                    updateImageField("imageName", tempName);
                    setEditingName(false);
                  }}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                      updateImageField("imageName", tempName);
                      setEditingName(false);
                    }
                  }}
                  autoFocus
                  placeholder="Enter image name..."
                />
              ) : (
                <div className="editable-field">
                  <h2>{image.imageName}</h2>
                  <span className="edit-icon">✎</span>
                </div>
              )}
            </div>
          </div>
          
          <div className="image-details-section">
            <label>Image Description <span className="edit-hint">(double-click to edit)</span></label>
            <div 
              className="image-description"
              onDoubleClick={() => setEditingDescription(true)}
            >
              {editingDescription ? (
                <textarea
                  value={tempDescription}
                  onChange={(e) => setTempDescription(e.target.value)}
                  onBlur={() => {
                    updateImageField("imageDescription", tempDescription);
                    setEditingDescription(false);
                  }}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      updateImageField("imageDescription", tempDescription);
                      setEditingDescription(false);
                    }
                  }}
                  autoFocus
                  placeholder="Enter image description..."
                />
              ) : (
                <div className="editable-field">
                  <p>{image.imageDescription || "No description provided"}</p>
                  <span className="edit-icon">✎</span>
                </div>
              )}
            </div>
          </div>
        </div>
        
        {error && <ErrorDisplay error={error} onClose={() => setError(null)} />}
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
            {isLoading ? 'Saving...' : 'Preview Image'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfigureImageMenu; 