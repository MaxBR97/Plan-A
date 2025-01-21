import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './UploadZPLPage.css';

const UploadZPLPage = () => {
    const [file, setFile] = useState(null);
    const [message, setMessage] = useState('');
    const navigate = useNavigate(); // Hook for navigation

    const handleFileChange = (event) => {
        setFile(event.target.files[0]);
        setMessage('');
    };

    const handleUpload = async () => {
        if (!file) {
            setMessage('Please select a file before uploading.');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        try {
           /* await axios.post('/Images', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            */
            setMessage('File uploaded successfully!');
            navigate('/configure-variables'); // Redirect to Configure Variables Page
            
        } catch (error) {
            setMessage('Failed to upload file. Please try again.');
        }
    };

    return (
        <div className="upload-zpl-page">
            <h1 className="page-title">Upload ZPL File</h1>
            <div className="file-input-container">
                
                <input type="file" onChange={handleFileChange} />
                <button className="upload-button" onClick={handleUpload}>
                    Upload
                </button>
            </div>
            {message && <p className="upload-message">{message}</p>}
        </div>
    );
};

export default UploadZPLPage;
