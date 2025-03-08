import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './MainPage.css';
import axios from 'axios';

const MainPage = () => {
    const [myImages, setMyImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Fetch all images when component mounts
    useEffect(() => {
        fetchImages();
    }, []);

    const fetchImages = async () => {
        try {
            setLoading(true);
            const response = await axios.get('/images');
            console.log("GET /images response: ", response)
            setMyImages(response.data);
            setError(null);
        } catch (err) {
            console.error('Error fetching images:', err);
            const errorMessage = err.response?.data?.msg || err.message || 'An unknown error occurred';
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteImage = async (imageId) => {
        if (window.confirm('Are you sure you want to delete this image?')) {
            try {
                await axios.delete(`/images/${imageId}`);
                // Remove the deleted image from state
                setMyImages(myImages.filter(image => image.id !== imageId));
            } catch (err) {
                console.error('Error deleting image:', err);
                alert('Failed to delete the image. Please try again.');
            }
        }
    };

    return (
        <div className="main-page">
            <h1 className="main-title">Plan A</h1>
            
            <div className="my-images-container">
                <div className="my-images-header">
                    <h2>My Images</h2>
                    <button 
                        className="refresh-button"
                        onClick={fetchImages}
                        disabled={loading}
                    >
                        Refresh
                    </button>
                </div>

                {loading && <div className="loading-spinner">Loading images...</div>}
                
                {error && <div className="error-message">{error}</div>}
                
                {!loading && !error && myImages.length === 0 && (
                    <div className="no-images-message">
                        No images found. Create your first image by clicking the button below.
                    </div>
                )}

                <div className="images-grid">
                    {myImages.map((image, index) => (
                        <div className="image-card" key={image.id || index}>
                            <div className="image-card-header">
                                <h3>Image {index + 1}</h3>
                                <button 
                                    className="delete-button"
                                    onClick={() => handleDeleteImage(image.id)}
                                    aria-label="Delete image"
                                >
                                    ×
                                </button>
                            </div>
                            <div className="image-card-content">
                                <div className="image-info">
                                    <p><strong>Variables:</strong> {image.variablesModule.variables?.length || 0}</p>
                                    <p><strong>Constraints:</strong> {image.constraintModules?.length || 0}</p>
                                    <p><strong>Preferences:</strong> {image.preferenceModules?.length || 0}</p>
                                </div>
                                <div className="image-actions">
                                    <button className="view-button">View Details</button>
                                    <button className="edit-button">Edit</button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            
            <div className="footer-button-container">
                <Link to="/upload-zpl" className="footer-button create-button">
                    Create new environment (For developers)
                </Link>
            </div>
        </div>
    );
};

export default MainPage;