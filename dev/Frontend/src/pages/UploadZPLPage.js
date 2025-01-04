import React, { useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import './UploadZPLPage.css';

const UploadZPLPage = () => {
    const [file, setFile] = useState(null);
    const [message, setMessage] = useState('');

    // Handle file selection
    const handleFileChange = (event) => {
        setFile(event.target.files[0]);
    };

    // Handle file upload
    const handleUpload = async () => {
        if (!file) {
            setMessage('Please select a file before uploading.');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await axios.post('http://localhost:8080/Images', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            setMessage('File uploaded successfully!');
        } catch (error) {
            setMessage('Failed to upload file. Please try again.');
            console.error(error);
        }
    };

    return (
        <div className="upload-zpl-page">
            <h1 className="page-title">Upload ZPL File</h1>
            <div className="file-input-container">
                <input type="file" accept=".zpl" onChange={handleFileChange} />
                <button className="upload-button" onClick={handleUpload}>
                    Upload
                </button>
            </div>
            {message && <p className="upload-message">{message}</p>}
            <Link to="/" className="back-button">
                Back
            </Link>
        </div>
    );
};

export default UploadZPLPage;
