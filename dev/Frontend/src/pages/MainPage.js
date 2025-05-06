import React, { useState, useEffect } from 'react';
import { Link ,useNavigate } from 'react-router-dom';
import './MainPage.css';
import axios from 'axios';
import { useZPL } from "../context/ZPLContext";
import { login, logout, getUsername } from './KeycloakService.js';
import { keycloak, initKeycloak } from './KeycloakService.js';



const MainPage = () => {
    const {
        user,
        updateUserField,
        updateImage,
        image
      } = useZPL();
    const navigate = useNavigate();
    const [myImages, setMyImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [expandedDescriptions, setExpandedDescriptions] = useState({});

    useEffect(() => {
    initKeycloak().then(authenticated => {
      if (authenticated) {
        updateUserField('isLoggedIn', true);
        updateUserField('username', getUsername());
      }
    });
  
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
    const navigateToImageDetails = (imageId) => {
        const selectedImage = myImages.find(image => image.imageId == imageId)
        console.log("selected: ",selectedImage)
        updateImage(selectedImage)
        navigate(`/solution-preview`);
    };

    const toggleDescription = (imageId, event) => {
        if (event) {
            event.stopPropagation(); 
        }
        setExpandedDescriptions(prev => ({
            ...prev,
            [imageId]: !prev[imageId]
        }));
    };

    const handleDeleteImage = async (imageId) => {
        if (window.confirm('Are you sure you want to delete this image?')) {
            try {
                await axios.delete(`/images/${imageId}`);
                // Remove the deleted image from state
                setMyImages(myImages.filter(image => image.imageId !== imageId));
            } catch (err) {
                console.error('Error deleting image:', err);
                alert('Failed to delete the image. Please try again.');
            }
        }
    };

    return (
        <div className="main-page">
            <div className="auth-section">
            {user.isLoggedIn ? (
                <>
                <span>Welcome, {user.username}!</span>
                <button onClick={logout} className="auth-button">Logout</button>
                </>
            ) : (
                <button onClick={login} className="auth-button">Login</button>
            )}
            </div>

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
                        <div 
                            className="image-card" 
                            key={image.imageId || index}
                            onClick={() => navigateToImageDetails(image.imageId)}
                            style={{ cursor: 'pointer' }}
                        >
                            <div className="image-card-header">
                                <h3>{image.imageName}</h3>
                                <button 
                                    className="delete-button"
                                    onClick={(e) => {
                                        e.stopPropagation(); // Prevent card click event
                                        handleDeleteImage(image.imageId);
                                    }}
                                    aria-label="Delete image"
                                >
                                    ×
                                </button>
                            </div>
                            <div className="image-card-content">
                                <div className="image-info">
                                    <div className={`description-container ${expandedDescriptions[image.imageId] ? 'expanded' : ''}`}>
                                        <p>{image.imageDescription}</p>
                                    </div>
                                    {image.imageDescription && image.imageDescription.length > 150 && (
                                        <button 
                                            className="read-more-button"
                                            onClick={(e) => {
                                                e.stopPropagation(); // Prevent card click event
                                                toggleDescription(image.imageId);
                                            }}
                                        >
                                            {expandedDescriptions[image.imageId] ? 'Show less' : 'Read more'}
                                        </button>
                                    )}
                                </div>
                                {/* <div className="image-actions">
                                    <button className="view-button">View Details</button>
                                    <button className="edit-button">Edit</button>
                                </div> */}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            
            <div className="footer-button-container">
                <Link to="/upload-zpl" className="footer-button create-button">
                    Create new image
                </Link>
            </div>
        </div>
    );
};

export default MainPage;