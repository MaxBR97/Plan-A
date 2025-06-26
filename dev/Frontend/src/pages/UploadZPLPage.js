import React, { useState, useEffect } from "react";
import axios from "axios";
import { useZPL } from "../context/ZPLContext";
import { Link, useNavigate } from "react-router-dom";
import ErrorDisplay from '../components/ErrorDisplay';
import InfoIcon from '../reusableComponents/InfoIcon';
import "./UploadZPLPage.css";

const UploadZPLPage = () => {
    const {
        user,
        image,
        model,
        updateImageField,
        updateModel,
        resetImage,
        resetModel
    } = useZPL();

    const [fileContent, setFileContent] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    // Load the example ZPL file content
    useEffect(() => {
        const loadExampleZPL = async () => {
            try {
                // Try to load from public folder first
                const response = await fetch('/example.zpl');
                if (response.ok) {
                    const content = await response.text();
                    setFileContent(content);
                } else {
                    // If not in public folder, try the src folder
                    const srcResponse = await fetch('/src/example.zpl');
                    if (srcResponse.ok) {
                        const content = await srcResponse.text();
                        setFileContent(content);
                    } else {
                        throw new Error('Example file not found');
                    }
                }
            } catch (error) {
                console.error('Failed to load example ZPL file:', error);
                // Fallback to a simple example if file loading fails
                setFileContent(`param example := 1;
set ExampleSet := {"item1", "item2"};
var ExampleVar[ExampleSet] binary;
minimize objective: sum <item> in ExampleSet : ExampleVar[item];`);
            }
        };
        
        loadExampleZPL();
    }, []);

    const handleUpload = async () => {
        if (!image.imageName.trim()) {
            setMessage("Error: Image name cannot be empty.");
            return;
        }

        setLoading(true);
        setMessage("");

        const requestData = {
            code: fileContent,
            imageName: image.imageName.trim(),
            owner: user.username,
            isPrivate : true,
            imageDescription: image.imageDescription,
        };

        console.log("Sending POST request:", requestData);

        try {
            const response = await axios.post("/api/images", requestData, {
                headers: { "Content-Type": "application/json" },
            });

            console.log("Response:", response);

            const { imageId, model } = response.data;
            resetImage()
            resetModel()
            // Update image state
            updateImageField("imageId", imageId);
            updateImageField("imageName", image.imageName.trim());
            updateImageField("imageDescription", image.imageDescription);

            // Update model state
            updateModel(model);

            setMessage("File uploaded successfully!");
            navigate("/configuration-menu");
        } catch (error) {
            if (error.response) {
                setMessage(`Error: ${error.response.status} - ${error.response.data?.msg || "Unknown error occurred"}`);
            } else if (error.request) {
                setMessage("Error: No response from server. Check if backend is running.");
            } else {
                setMessage(`Error: ${error.message}`);
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="upload-zpl-page">
            <h1 className="page-title">Create A Model</h1>
            <p className="page-description">
                Create a new model. Write your model's core logic in ZIMPL language: <a href="https://zimpl.zib.de/download/zimpl.pdf" target="_blank" rel="noopener noreferrer">https://zimpl.zib.de/download/zimpl.pdf</a>
            </p>
            <div className="upload-container">
                <label>Image Name:</label>
                <input
                    type="text"
                    value={image.imageName}
                    onChange={(e) => updateImageField("imageName", e.target.value)}
                    className="image-name-input"
                    placeholder="Enter image name..."
                />

                <div className="code-section">
                    <div className="label-with-info">
                        <label htmlFor="zimpl-code">Model code:</label>
                        <InfoIcon tooltip={
                            <div>
                                <p>Write your Zimpl code here. In addition to the Zimpl language syntax, there are additional rules:</p>
                                <ul>
                                    <li>Do not leave parameters and sets empty. Don't worry, you will be able to edit them in the image.</li>
                                    <li>Each do-check statement must be preceded by a do-print statement.</li>
                                    <li>Use clear and descriptive variable names</li>
                                    <li>Do not use the syntax: param paramName[setName] := ... ; , we don't support it yet.</li>
                                    <li>Do not SOS (Special Ordered Sets) variables. We don't support it yet.</li>
                                    <li>All variables must depend on a set. meanning: var myVar[mySet] ...</li>
                                </ul>
                            </div>
                        } />
                    </div>
                    <textarea
                        id="zimpl-code"
                        value={fileContent}
                        onChange={(e) => setFileContent(e.target.value)}
                        className="fixed-textarea code"
                    />
                </div>

                <label>Image Description:</label>
                <textarea
                    value={image.imageDescription}
                    onChange={(e) => updateImageField("imageDescription", e.target.value)}
                    className="description-textarea"
                    placeholder="Enter image description..."
                />
                {message && message.startsWith('Error:') ? (
                <ErrorDisplay error={message} onClose={() => setMessage("")} />
            ) : message ? (
                <p className="upload-message success-message">{message}</p>
            ) : null}
                <button className="upload-button" onClick={handleUpload} disabled={loading}>
                    {loading ? "Uploading..." : "Upload"}
                </button>
            </div>
               
            <Link to="/" className="back-button">
                Back
            </Link>
        </div>
    );
};

export default UploadZPLPage;
