import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './MainPage.css';
import axios from 'axios';
import { useZPL } from "../context/ZPLContext";
import { 
  login, 
  logout, 
  register,
  getUsername, 
  initKeycloak, 
  isAuthenticated 
} from './KeycloakService';

const MainPage = () => {
    const {
        user,
        updateUserField,
        updateImage,
        resetImage,
        resetModel
    } = useZPL();
    const navigate = useNavigate();
    const [myImages, setMyImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [expandedDescriptions, setExpandedDescriptions] = useState({});
    
    // Use a ref to track whether we've initialized authentication on mount
    const authInitialized = useRef(false);
    useEffect(() => {
        // Only initialize Keycloak once on mount
        if (!authInitialized.current) {
            authInitialized.current = true;
            
            initKeycloak()
                .then(authenticated => {
                    console.log("Keycloak auth status:", authenticated);
                    if (authenticated) {
                        updateUserField('isLoggedIn', true);
                        updateUserField('username', getUsername());
                    } else {
                        // If not authenticated, still show "Guest"
                        updateUserField('isLoggedIn', false);
                        updateUserField('username', 'Guest');
                    }
                })
                .catch(error => {
                    console.error("Auth initialization error:", error);
                    // On error, default to "Guest"
                    updateUserField('isLoggedIn', false);
                    updateUserField('username', 'Guest');
                })
                .finally(() => {
                    // Fetch images regardless of auth status
                    fetchImages();
                });
        } 
            // Fetch images even if not doing auth init
            fetchImages();
        
        
        // Cleanup function to run on unmount
        return () => {
            // Any cleanup needed when component unmounts
        };
    }, []); // Empty dependency array so it only runs once on mount

    const fetchImages = async () => {
        try {
            setLoading(true);
            console.log("getting images")
            const response = await axios.get('/images');
            console.log("GET /images response: ", response);
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
        const selectedImage = myImages.find(image => image.imageId == imageId);
        console.log("selected: ", selectedImage);
        updateImage(selectedImage);
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

    const handleDeleteImage = async (imageId, event) => {
        if (event) {
            event.stopPropagation(); 
        }
        
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

    const handleLogin = () => {
        // Save current path for redirect after login
        localStorage.setItem('auth_redirect_uri', window.location.pathname);
        login();
    };

    const handleRegister = () => {
        // Save current path for redirect after registration
        localStorage.setItem('auth_redirect_uri', window.location.pathname);
        register();
    };

    const handleLogout = () => {
        // Save current path for redirect after logout
        localStorage.setItem('auth_redirect_uri', window.location.pathname);
        logout();
        updateUserField('isLoggedIn', false);
        updateUserField('username', 'Guest');
    };

    const handleRefresh = () => {
        fetchImages();
    };

    return (
        <div className="main-page">
            <div className="auth-section">
                <span>Welcome, {user.isLoggedIn ? user.username : 'Guest'}!</span>
                {user.isLoggedIn ? (
                    <button onClick={handleLogout} className="auth-button">Logout</button>
                ) : (
                    <div className="auth-buttons">
                        <button onClick={handleLogin} className="auth-button">Login</button>
                        <button onClick={handleRegister} className="auth-button">Sign Up</button>
                    </div>
                )}
            </div>

            <h1 className="main-title">Plan A</h1>
            
            <div className="my-images-container">
                <div className="my-images-header">
                    <h2>My Images</h2>
                    <button 
                        className="refresh-button"
                        onClick={handleRefresh}
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
                                    onClick={(e) => handleDeleteImage(image.imageId, e)}
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
                                            onClick={(e) => toggleDescription(image.imageId, e)}
                                        >
                                            {expandedDescriptions[image.imageId] ? 'Show less' : 'Read more'}
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            
            <div className="footer-button-container">
                <Link 
                    to="/upload-zpl" 
                    className="footer-button create-button"
                >
                    Create new image
                </Link>
            </div>
        </div>
    );
};

export default MainPage;