import React, { useState } from "react";
import axios from "axios";
import { useZPL } from "../context/ZPLContext";
import { useNavigate } from "react-router-dom";
import "./UploadZPLPage.css";

const UploadZPLPage = () => {
    const {
        imageId, setImageId,
        variables, setVariables,
        setTypes, setSetTypes, // ✅ Use correct setter
        paramTypes, setParamTypes, // ✅ Use correct setter
        constraints, setConstraints,
        preferences, setPreferences
    } = useZPL();

    const [fileContent, setFileContent] = useState("");
    const [message, setMessage] = useState("");
    const navigate = useNavigate();

    const handleUpload = async () => {
        const requestData = { code: fileContent };

        try {
            const response = await axios.post("/images", requestData, {
                headers: { "Content-Type": "application/json" },
            });

            const responseData = response.data;

            // ✅ Store new data in the ZPL Context
            setImageId(responseData.imageId);
            setVariables(responseData.model.variables);
            setConstraints(responseData.model.constraints);
            setPreferences(responseData.model.preferences);
            setSetTypes(responseData.model.setTypes); // ✅ Store set types
            setParamTypes(responseData.model.paramTypes); // ✅ Store param types
            

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
