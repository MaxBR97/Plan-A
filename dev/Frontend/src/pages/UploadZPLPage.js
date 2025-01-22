import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './UploadZPLPage.css';

const UploadZPLPage = () => {
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

            alert(`Response received: ${JSON.stringify(response.data, null, 2)}`); // Show response in an alert

            setMessage('File uploaded successfully!');
            navigate('/configure-variables'); // Redirect to next page
        } catch (error) {
            const errorMsg = error.response?.data?.msg || "Unknown error";
            setMessage(`Error: ${error.response?.status} - ${errorMsg}`);
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
