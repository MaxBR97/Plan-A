import React, { useState, useRef, useEffect } from "react";
const DraggableBar = ({ 
    min = 0, 
    max = 100, 
    markers = [{ myParam: 50 }], 
    onChange,
    allowFloats = false // should the bar represnt integers or continous (floats) numbers?
  }) => {
    const barRef = useRef(null);
    
    // Function to round the value based on allowFloats setting
    const roundValue = (value) => {
      return allowFloats ? value : Math.round(value);
    };
    
    // Create a constrained version of the markers
    const constrainMarkerValues = (markersArray) => {
      return markersArray.map(marker => {
        const constrainedMarker = { ...marker };
        
        // Iterate through each property in the marker object
        Object.keys(constrainedMarker).forEach(key => {
          if (typeof constrainedMarker[key] === 'number') {
            // Constrain numeric values to be within min and max, and round if needed
            constrainedMarker[key] = roundValue(
              Math.min(Math.max(constrainedMarker[key], min), max)
            );
          }
        });
        
        return constrainedMarker;
      });
    };
    
    // Initialize with constrained values
    const [markerValues, setMarkerValues] = useState(constrainMarkerValues(markers));
    const [isDragging, setIsDragging] = useState(false);
    
    useEffect(() => {
        setMarkerValues(prevMarkers => {
            const newMarkers = constrainMarkerValues(markers);
            
            newMarkers.forEach((newMarker, index) => {
                const prevMarker = prevMarkers[index];
                Object.keys(newMarker).forEach(key => {
                    if (newMarker[key] !== prevMarker?.[key]) {
                        onChange(newMarker); // Call onChange only for changed markers
                    }
                });
            });
    
            return newMarkers;
        });
    }, [markers, min, max, allowFloats]); // Added allowFloats as dependency
    
    
    // Handle mouse down event to start dragging
    const handleMouseDown = (e, markerIndex) => {
      e.preventDefault(); // Prevent default browser behavior
      setIsDragging(true);
      
      const handleMouseMove = (moveEvent) => {
        if (barRef.current) {
          const rect = barRef.current.getBoundingClientRect();
          const barWidth = rect.width;
          const offsetX = moveEvent.clientX - rect.left;
          
          // Calculate position as percentage of width
          let position = (offsetX / barWidth) * (max - min) + min;
          position = Math.min(Math.max(position, min), max); // Constrain to min/max
          
          // Round to integer if allowFloats is false
          position = roundValue(position);
          
          // Update the marker value
          const updatedMarkers = [...markerValues];
          const key = Object.keys(updatedMarkers[markerIndex])[0];
          updatedMarkers[markerIndex] = { [key]: position };
          
          setMarkerValues(updatedMarkers);
          
          // Call onChange with the updated marker
          if (onChange) {
            onChange(updatedMarkers[markerIndex]);
          }
        }
      };
      
      const handleMouseUp = () => {
        setIsDragging(false);
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
      };
      
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    };
    
    // Add a style for the body when dragging to prevent cursor change
    useEffect(() => {
      if (isDragging) {
        // Apply style to prevent cursor change during drag
        document.body.style.userSelect = 'none';
        document.body.style.cursor = 'grabbing';
        
        // Add a hidden div to capture events that might cause the forbidden sign
        const captureDiv = document.createElement('div');
        captureDiv.id = 'drag-capture';
        captureDiv.style.position = 'fixed';
        captureDiv.style.top = '0';
        captureDiv.style.left = '0';
        captureDiv.style.width = '100%';
        captureDiv.style.height = '100%';
        captureDiv.style.zIndex = '9999';
        captureDiv.style.pointerEvents = 'none'; // Allow events to pass through
        document.body.appendChild(captureDiv);
        
        return () => {
          // Clean up
          document.body.style.userSelect = '';
          document.body.style.cursor = '';
          if (document.getElementById('drag-capture')) {
            document.body.removeChild(document.getElementById('drag-capture'));
          }
        };
      }
    }, [isDragging]);
    
    // Format the displayed value based on allowFloats
    const formatValue = (value) => {
      return allowFloats ? value.toFixed(1) : value.toString();
    };
    
    return (
      <div 
        className="draggable-bar" 
        ref={barRef}
        style={{
          position: 'relative',
          width: '100%',
          height: '30px',
          backgroundColor: '#e0e0e0',
          borderRadius: '15px',
          margin: '20px 0',
          cursor: isDragging ? 'grabbing' : 'default'
        }}
      >
        {markerValues.map((marker, index) => {
          const key = Object.keys(marker)[0];
          const value = marker[key];
          const position = ((value - min) / (max - min)) * 100;
          
          return (
            <div
              key={key}
              className="marker"
              style={{
                position: 'absolute',
                left: `${position}%`,
                top: '50%',
                transform: 'translate(-50%, -50%)',
                width: '20px',
                height: '20px',
                borderRadius: '50%',
                backgroundColor: '#4285f4',
                cursor: isDragging ? 'grabbing' : 'grab',
                boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                color: 'white',
                fontSize: '10px',
                userSelect: 'none'
              }}
              onMouseDown={(e) => handleMouseDown(e, index)}
            >
              {/* {key} */}
            </div>
          );
        })}
        
        {/* Display marker values and labels */}
        {markerValues.map((marker, index) => {
          const key = Object.keys(marker)[0];
          const value = marker[key];
          const position = ((value - min) / (max - min)) * 100;
          
          return (
            <div
              key={`label-${key}`}
              style={{
                position: 'absolute',
                left: `${position}%`,
                bottom: '-25px',
                transform: 'translateX(-50%)',
                fontSize: '16px', /* Increased font size */
                fontWeight: 'bold',
                whiteSpace: 'nowrap',
                userSelect: 'none'
              }}
            >
              {key}: {formatValue(value)}
            </div>
          );
        })}
      </div>
    );
  };
  
  export default DraggableBar;
