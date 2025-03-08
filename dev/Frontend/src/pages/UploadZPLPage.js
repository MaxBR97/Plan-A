import React, { useState } from "react";
import axios from "axios";
import { useZPL } from "../context/ZPLContext";
import { useNavigate } from "react-router-dom";
import "./UploadZPLPage.css";

const UploadZPLPage = () => {
    const {
        imageName, setImageName,
        imageId, setImageId,
        variables, setVariables,
        setTypes, setSetTypes,
        paramTypes, setParamTypes,
        varTypes, setVarTypes,
        constraints, setConstraints,
        preferences, setPreferences,
        resetAll
    } = useZPL();

    const [fileContent, setFileContent] = useState(""); // Default or empty file content
    const [message, setMessage] = useState("");
    const navigate = useNavigate();

    const handleUpload = async () => {
        const requestData = {
            code: fileContent,
            imageName: imageName.trim() || "default_image", // Include image name, fallback if empty
        };

        try {
            const response = await axios.post("/images", requestData, {
                headers: { "Content-Type": "application/json" },
            });

            const responseData = response.data;
            resetAll();
            setImageName(responseData.imageName)
            setImageId(responseData.imageId);
            setVariables(responseData.model.variables);
            setConstraints(responseData.model.constraints);
            setPreferences(responseData.model.preferences);
            setSetTypes(responseData.model.setTypes);
            setParamTypes(responseData.model.paramTypes);
            setVarTypes(responseData.model.varTypes);

            console.log("Full Response Data:", responseData);

            setMessage("File uploaded successfully!");
            navigate("/configure-variables"); // Redirect to next page
        } catch (error) {
            if (error.response) {
                const errorMsg = error.response.data?.msg || "Unknown error occurred";
                setMessage(`Error: ${error.response.status} - ${errorMsg}`);
            } else if (error.request) {
                setMessage("Error: No response from server. Check if backend is running.");
            } else {
                setMessage(`Error: ${error.message}`);
            }
        }
    };

    return (
        <div className="upload-zpl-page">
            <h1 className="page-title">Upload ZPL File</h1>
            <div className="upload-container">
                {/* Image Name Input */}
                <label>Image Name:</label>
                <input
                    type="text"
                    value={imageName}
                    onChange={(e) => setImageName(e.target.value)}
                    className="image-name-input"
                    placeholder="Enter image name..."
                />

                {/* File Content */}
                <label>File Content:</label>
                <textarea
                    value={fileContent}
                    onChange={(e) => setFileContent(e.target.value)}
                    className="fixed-textarea"
                />

                {/* Upload Button */}
                <button className="upload-button" onClick={handleUpload}>Upload</button>
            </div>
            {message && <p className="upload-message">{message}</p>}
        </div>
    );
};

export default UploadZPLPage;
