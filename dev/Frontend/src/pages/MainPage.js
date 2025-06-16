import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './MainPage.css';
import axios from 'axios';
import { useZPL } from "../context/ZPLContext";
import ErrorDisplay from '../components/ErrorDisplay';
import { 
  login, 
  logout, 
  register,
  getUsername, 
  initKeycloak, 
  isAuthenticated,
  hasRole
} from './KeycloakService';

const MainPage = () => {
    const {
        user,
        updateUserField,
        updateImage,
        resetImage,
        resetModel,
        fetchAndSetImage,
        updateImageField
    } = useZPL();
    const navigate = useNavigate();
    const [myImages, setMyImages] = useState([]);
    const [searchResults, setSearchResults] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [loading, setLoading] = useState(true);
    const [searchLoading, setSearchLoading] = useState(false);
    const [error, setError] = useState(null);
    const [expandedDescriptions, setExpandedDescriptions] = useState({});
    
    const authInitialized = useRef(false);
    console.log("user:", user);
    useEffect(() => {
        const initializeAuth = async () => {
            if (!authInitialized.current) {
                authInitialized.current = true;
                
                try {
                    console.log('Starting auth initialization...');
                    const authenticated = await initKeycloak();
                    console.log("Keycloak auth status:", authenticated);
                    
                    if (authenticated) {
                        updateUserField('isLoggedIn', true);
                        updateUserField('username', getUsername());
                        await fetchImages();
                    } else {
                        updateUserField('isLoggedIn', false);
                        updateUserField('username', 'Guest');
                        setLoading(false);
                    }
                    await handleSearch();
                } catch (error) {
                    console.error("Auth initialization error:", error);
                    setError('Failed to initialize authentication. Please refresh the page.');
                    updateUserField('isLoggedIn', false);
                    updateUserField('username', 'Guest');
                    await handleSearch();
                }
            }
        };

        initializeAuth();

    }, []);

    const fetchImages = async () => {
        try {
            setLoading(true);
            const response = await axios.get('/api/images');
            setMyImages(response.data);
            setError(null);
        } catch (err) {
            console.error('Error fetching images:', err);
            setError(err.response?.data?.message || 'Failed to fetch images');
        } finally {
            setLoading(false);
        }
    };

    const navigateToImageDetails = (imageId) => {
        const allImages = [...myImages, ...searchResults];
        const selectedImage = allImages.find(image => image.imageId === imageId);
        
        console.log("allImages: ", allImages);
        console.log("searchResults: ", searchResults);
        console.log("selectedImage: ", selectedImage);
        updateImageField('isConfigured', true);
        if (selectedImage) {
            fetchAndSetImage(selectedImage.imageId);
            navigate(`/solution-preview`);
        } else {
            console.warn(`Image with ID ${imageId} not found in myImages or searchResults.`);
        }
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
        
        if (!isAuthenticated()) {
            setError('You must be logged in to delete images');
            return;
        }

        if (window.confirm('Are you sure you want to delete this image?')) {
            try {
                await axios.delete(`/api/images/${imageId}`);
                setMyImages(myImages.filter(image => image.imageId !== imageId));
                setSearchResults(searchResults.filter(image => image.imageId !== imageId));
            } catch (err) {
                console.error('Error deleting image:', err);
                setError(err.response?.data?.message || 'Failed to delete image');
            }
        }
    };

    const handleLogin = async () => {
        try {
            console.log('Starting login process...');
            localStorage.setItem('auth_redirect_uri', window.location.pathname);
            await login();
        } catch (error) {
            console.error('Login error:', error);
            setError('Failed to initialize login. Please try again or refresh the page.');
        }
    };

    const handleRegister = async () => {
        try {
            console.log('Starting registration process...');
            localStorage.setItem('auth_redirect_uri', window.location.pathname);
            await register();
        } catch (error) {
            console.error('Registration error:', error);
            setError('Failed to initialize registration. Please try again or refresh the page.');
        }
    };

    const handleLogout = async () => {
        try {
            console.log('Starting logout process...');
            localStorage.setItem('auth_redirect_uri', window.location.pathname);
            await logout();
            updateUserField('isLoggedIn', false);
            updateUserField('username', 'Guest');
            await handleSearch();
        } catch (error) {
            console.error('Logout error:', error);
            setError('Failed to logout. Please try again or refresh the page.');
        }
    };

    const handleSearch = async () => {
        // if (!searchQuery.trim()) {
        //     setSearchResults([]);
        //     return;
        // }

        try {
            setSearchLoading(true);
            const response = await axios.get(`/api/images/search?searchPhrase=${encodeURIComponent(searchQuery)}`);
            console.log("response: ", response.data);
            setSearchResults(response.data);
            setError(null);
        } catch (err) {
            console.error('Error searching images:', err);
            setError(err.response?.data?.message || 'Failed to search images');
            setSearchResults([]);
        } finally {
            setSearchLoading(false);
        }
    };

    const handleSearchKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
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
            
            <p className="main-description">
                Welcome to Plan A! Here you can manage your optimization problems by creating new images or working with existing ones.
                Each image represents a unique optimization problem with its own configuration. Choose one to get started, or create a new one.
            </p>

            <div className="my-images-container">
                <div className="my-images-header">
                    <label htmlFor="search-input" className="search-label">Public Images</label>
                </div>
                <div className="search-container">
                    <div className="search-input-group">
                        <input
                            type="text"
                            id="search-input"
                            className="search-input"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onKeyPress={handleSearchKeyPress}
                            placeholder="Enter image name..."
                        />
                        <button 
                            className="search-button"
                            onClick={handleSearch}
                            disabled={searchLoading}
                        >
                            {searchLoading ? 'Searching...' : 'Search'}
                        </button>
                    </div>
                </div>
                {searchResults.length == 20 && (
                    <div className="no-images-message">
                        Showing top 20 results.
                    </div>
                )}
                {searchResults.length > 0 && (
                    <div className="images-grid">
                        {searchResults.map((image, index) => (
                            <div 
                                className="image-card" 
                                key={image.imageId || index}
                                onClick={() => navigateToImageDetails(image.imageId)}
                                style={{ cursor: 'pointer' }}
                            >
                                <div className="image-card-header">
                                    <h3>{image.imageName}</h3>
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
                                        <div className="image-owner">Owner: {image.owner || 'Unknown'}</div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
            <div className="my-images-container">
                <div className="my-images-header">
                    <h2>My Images</h2>
                    <button 
                        className="refresh-button"
                        onClick={() => user.isLoggedIn ? fetchImages() : null}
                        disabled={loading}
                    >
                        Refresh
                    </button>
                </div>
    
                {loading && <div className="loading-spinner">Loading images...</div>}
                
                {error && <ErrorDisplay error={error} onClose={() => setError(null)} />}
                
                {!loading && !error && myImages.length === 0 && (
                    <div className="no-images-message">
                        No images found. {user.isLoggedIn ? 'Create your first image by clicking the button below.' : 'Please log in to create images.'}
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
                                {user.isLoggedIn && (
                                    <button 
                                        className="delete-button"
                                        onClick={(e) => handleDeleteImage(image.imageId, e)}
                                        aria-label="Delete image"
                                    >
                                        ×
                                    </button>
                                )}
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
                                    <div className="image-owner">Owner: {image.owner || 'Unknown'}</div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            
            <div className="footer-button-container">
                {user.isLoggedIn ? (
                    <Link 
                        to="/upload-zpl" 
                        className="footer-button create-button"
                        onClick={() => {
                            resetImage();
                            resetModel();
                        }}
                    >
                        Create new image
                    </Link>
                ) : (
                    <button 
                        className="footer-button create-button"
                        onClick={handleLogin}
                    >
                        Login to create images
                    </button>
                )}
            </div>
        </div>
    );
};

export default MainPage;