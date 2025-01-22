import React, { useState } from "react";
import axios from "axios";
import { useZPL } from "../context/ZPLContext";
import { useNavigate } from "react-router-dom";
import "./UploadZPLPage.css";

const UploadZPLPage = () => {
    const { imageId, setImageId, variables, setVariables, types, setTypes, constraints, setConstraints, preferences, setPreferences } = useZPL();

    const [fileName, setFileName] = useState("");
    const [fileContent, setFileContent] = useState("");
    const [message, setMessage] = useState("");
    const navigate = useNavigate();

    const handleUpload = async () => {
        if (!fileName || !fileContent) {
            setMessage("Please provide a file name and content before uploading.");
            return;
        }

        const requestData = {
            name: fileName,
            code: fileContent,
        };

        try {
            const response = await axios.post("/images", requestData, {
                headers: {
                    "Content-Type": "application/json",
                },
            });

            const responseData = response.data;

            // Store response in the ZPL Context correctly
            setImageId(responseData.imageId);
            setVariables(responseData.model.variables);
            setConstraints(responseData.model.constraints);
            setPreferences(responseData.model.preferences);
            setTypes(responseData.model.types);

            console.log("Full Response Data:", responseData);

            // Create a JSON file and trigger a download
            const jsonData = JSON.stringify(responseData, null, 2);
            const blob = new Blob([jsonData], { type: "application/json" });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = "responseZPL.json";
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);

            setMessage("File uploaded successfully!");

            console.log("Stored Data:", {
                imageId,
                variables,
                types,
                constraints,
                preferences
            });


            navigate("/configure-variables"); // Redirect to Configure Variables Page
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
                <label>File Name:</label>
                <input
                    type="text"
                    value={fileName}
                    onChange={(e) => setFileName(e.target.value)}
                />
                <label>File Content:</label>
                <textarea
                    value={fileContent}
                    onChange={(e) => setFileContent(e.target.value)}
                />
                <button className="upload-button" onClick={handleUpload}>Upload</button>
            </div>
            {message && <p className="upload-message">{message}</p>}
        </div>
    );
};

export default UploadZPLPage;
