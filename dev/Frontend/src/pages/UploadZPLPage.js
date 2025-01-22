import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext'; // Import context
import './UploadZPLPage.css';

const UploadZPLPage = () => {
    const { setImageId, setVariables, setTypes, setConstraints, setPreferences } = useZPL(); // Use context to store response
    const [fileName, setFileName] = useState('');
    const [fileContent, setFileContent] = useState('');
    const [message, setMessage] = useState('');
    const navigate = useNavigate(); // Hook for navigation

    const handleUpload = async () => {
        if (!fileName || !fileContent) {
            setMessage('Please enter both file name and content before uploading.');
            return;
        }

        const requestData = {
            name: fileName,
            code: fileContent
        };

        try {
            const response = await axios.post('/images', requestData, {
                headers: { 'Content-Type': 'application/json' }
            });

            console.log("Full Response Data:", response.data); // Debugging log

            const { imageId, model } = response.data;

            if (!model) {
                throw new Error("Invalid response format: 'model' is missing");
            }

            // Store extracted data in the global context
            setImageId(imageId || "No ID received");
            setVariables(model.variables || {});
            setTypes(model.types || {});
            setConstraints(model.constraints || {});
            setPreferences(model.preferences || {});

            setMessage('File uploaded successfully!');
            navigate('/configure-variables'); // Redirect to next page
        } catch (error) {
            console.error("Upload Error:", error);
            if (error.response) {
                const errorMsg = error.response.data?.msg || "Unknown error occurred";
                setMessage(`Error: ${error.response.status} - ${errorMsg}`);
            } else if (error.request) {
                setMessage('Error: No response from server. Check if backend is running.');
            } else {
                setMessage(`Error: ${error.message}`);
            }
        }
    };

    return (
        <div className="upload-zpl-page">
            <h1 className="page-title">Upload ZPL File</h1>

            <div className="input-container">
                <label>File Name:</label>
                <input 
                    type="text" 
                    value={fileName} 
                    onChange={(e) => setFileName(e.target.value)} 
                    placeholder="Enter file name (e.g., myfile.zpl)" 
                />
            </div>

            <div className="input-container">
                <label>File Content:</label>
                <textarea 
                    value={fileContent} 
                    onChange={(e) => setFileContent(e.target.value)} 
                    placeholder="Enter ZPL file content here..."
                    rows="5"
                />
            </div>

            <button className="upload-button" onClick={handleUpload}>
                Upload
            </button>

            {message && <p className="upload-message">{message}</p>}
        </div>
    );
};

export default UploadZPLPage;
