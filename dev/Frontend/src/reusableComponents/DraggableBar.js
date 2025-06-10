import React, { useState, useRef, useEffect } from "react";
import InfoIcon from './InfoIcon';

const DraggableBar = ({ 
    min = 0, 
    max = 100, 
    markers = [{ myParam: 50 }], 
    costParams = new Map(), // Pass costParams to access aliases
    onChange,
    allowFloats = false
}) => {
    const barRef = useRef(null);
    
    const roundValue = (value) => allowFloats ? value : Math.round(value);

    const constrainMarkerValues = (markersArray) => {
        return markersArray.map(marker => {
            const constrainedMarker = { ...marker };
            Object.keys(constrainedMarker).forEach(key => {
                if (typeof constrainedMarker[key] === 'number') {
                    constrainedMarker[key] = roundValue(
                        Math.min(Math.max(constrainedMarker[key], min), max)
                    );
                }
            });
            return constrainedMarker;
        });
    };

    const [markerValues, setMarkerValues] = useState(constrainMarkerValues(markers));
    const [isDragging, setIsDragging] = useState(false);

    useEffect(() => {
        setMarkerValues(prevMarkers => {
            const newMarkers = constrainMarkerValues(markers);
            newMarkers.forEach((newMarker, index) => {
                const prevMarker = prevMarkers[index];
                Object.keys(newMarker).forEach(key => {
                    if (newMarker[key] !== prevMarker?.[key]) {
                        onChange(newMarker);
                    }
                });
            });
            return newMarkers;
        });
    }, [markers, min, max, allowFloats]);

    const handleMouseDown = (e, markerIndex) => {
        e.preventDefault();
        setIsDragging(true);

        const handleMouseMove = (moveEvent) => {
            if (barRef.current) {
                const rect = barRef.current.getBoundingClientRect();
                const barWidth = rect.width;
                const offsetX = moveEvent.clientX - rect.left;
                let position = (offsetX / barWidth) * (max - min) + min;
                position = Math.min(Math.max(position, min), max);
                position = roundValue(position);

                const updatedMarkers = [...markerValues];
                const key = Object.keys(updatedMarkers[markerIndex])[0];
                updatedMarkers[markerIndex] = { [key]: position };

                setMarkerValues(updatedMarkers);

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

    useEffect(() => {
        if (isDragging) {
            document.body.style.userSelect = 'none';
            document.body.style.cursor = 'grabbing';

            const captureDiv = document.createElement('div');
            captureDiv.id = 'drag-capture';
            captureDiv.style.position = 'fixed';
            captureDiv.style.top = '0';
            captureDiv.style.left = '0';
            captureDiv.style.width = '100%';
            captureDiv.style.height = '100%';
            captureDiv.style.zIndex = '9999';
            captureDiv.style.pointerEvents = 'none';
            document.body.appendChild(captureDiv);

            return () => {
                document.body.style.userSelect = '';
                document.body.style.cursor = '';
                if (document.getElementById('drag-capture')) {
                    document.body.removeChild(document.getElementById('drag-capture'));
                }
            };
        }
    }, [isDragging]);

    const colors = ['#4285f4', '#fbbc05', '#34a853', '#ea4335', '#9b59b6', '#ff7f50', '#6a5acd'];

    const formatValue = (value) => allowFloats ? value.toFixed(1) : value.toString();

    return (
        <div style={{
            display: 'flex',
            alignItems: 'flex-start',
            padding: '15px',
            backgroundColor: '#f9f9f9',
            borderRadius: '10px',
            boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
            width: '100%',
            boxSizing: 'border-box',
            gap: '20px'
        }}>
            {/* Left Side - Alias/Color Legend with Title */}
            <div style={{ display: 'flex', flexDirection: 'column', minWidth: '120px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                    <span style={{ fontSize: '14px', fontWeight: '500', color: '#2c3e50' }}></span>
                    <InfoIcon tooltip="Adjust the relative importance of different cost factors in your optimization" />
                </div>
                {markerValues.map((marker, index) => {
                    const key = Object.keys(marker)[0];
                    const alias = costParams.get(key)?.alias || key;
                    const color = colors[index % colors.length];
                    return (
                        <div key={`legend-${key}`} style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
                            <div style={{
                                width: '12px',
                                height: '12px',
                                borderRadius: '50%',
                                backgroundColor: color,
                                marginRight: '8px'
                            }} />
                            <span style={{ fontSize: '13px', color: '#333' }}>{alias}</span>
                        </div>
                    );
                })}
            </div>

            {/* Right Side - Bar and Top Labels */}
            <div style={{ flexGrow: 1 }}>
                {/* Top Labels */}
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px', padding: '0 5px' }}>
                    <span style={{ fontSize: '12px', color: '#666' }}>Agnostic</span>
                    <span style={{ fontSize: '12px', color: '#666' }}>Highly Desired</span>
                </div>

                {/* Bar */}
                <div 
                    ref={barRef}
                    style={{
                        position: 'relative',
                        width: '100%',
                        height: '30px',
                        backgroundColor: '#e0e0e0',
                        borderRadius: '15px',
                        cursor: isDragging ? 'grabbing' : 'default'
                    }}
                >
                    {markerValues.map((marker, index) => {
                        const key = Object.keys(marker)[0];
                        const value = marker[key];
                        const position = ((value - min) / (max - min)) * 100;
                        const color = colors[index % colors.length];

                        return (
                            <div
                                key={key}
                                className="marker"
                                style={{
                                    position: 'absolute',
                                    left: `${position}%`,
                                    top: '50%',
                                    transform: 'translate(-50%, -50%)',
                                    width: '18px',
                                    height: '18px',
                                    borderRadius: '50%',
                                    backgroundColor: color,
                                    cursor: isDragging ? 'grabbing' : 'grab',
                                    boxShadow: '0 2px 5px rgba(0,0,0,0.3)',
                                    userSelect: 'none'
                                }}
                                onMouseDown={(e) => handleMouseDown(e, index)}
                            />
                        );
                    })}
                </div>
            </div>
        </div>
    );
};

export default DraggableBar;
