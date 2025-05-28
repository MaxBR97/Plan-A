import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useZPL } from "../context/ZPLContext";
import "./UploadZPLPage.css";

const UploadZPLPage = () => {
  const navigate = useNavigate();
  const { image, updateImageField, updateModel } = useZPL();
  const [error, setError] = useState("");
  const [zimplCode, setZimplCode] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!image.imageName) {
      setError("Image name cannot be empty");
      return;
    }

    try {
      const response = await axios.post("/api/upload-zpl", {
        imageName: image.imageName,
        imageDescription: image.imageDescription,
        zimplCode,
      });

      updateImageField("imageId", response.data.imageId);
      updateModel(response.data.model);
      navigate("/configuration-menu");
    } catch (err) {
      setError(`Error: ${err.response.status} - ${err.response.data.msg}`);
    }
  };

  return (
    <div className="upload-zpl-container">
      <h2>Upload ZIMPL Code</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="imageName">Image Name</label>
          <input
            type="text"
            id="imageName"
            value={image.imageName}
            onChange={(e) => updateImageField("imageName", e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="zimplCode">ZIMPL Code</label>
          <textarea
            id="zimplCode"
            value={zimplCode}
            onChange={(e) => setZimplCode(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="imageDescription">Image Description</label>
          <textarea
            id="imageDescription"
            value={image.imageDescription}
            onChange={(e) => updateImageField("imageDescription", e.target.value)}
          />
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="button-group">
          <button type="button" onClick={() => navigate(-1)}>
            Back
          </button>
          <button type="submit">Upload</button>
        </div>
      </form>
    </div>
  );
};

export default UploadZPLPage;
