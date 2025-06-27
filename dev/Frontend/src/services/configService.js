import axios from 'axios';

class ConfigService {
  constructor() {
    this.config = null;
    this.isInitialized = false;
  }

  async initialize() {
    if (this.isInitialized) {
      return this.config;
    }

    try {
      // Load runtime configuration from public/config.json
      const response = await fetch('/config.json');
      this.config = await response.json();
      
      // Set up axios with the configured base URL
      this.setupAxios();
      
      this.isInitialized = true;
      console.log('Configuration loaded:', this.config);
      
      return this.config;
    } catch (error) {
      console.error('Failed to load configuration:', error);
      // Fallback to default values
      this.config = {
        REACT_APP_API_HOST: 'http://localhost',
        REACT_APP_API_PORT: '3000'
      };
      this.setupAxios();
      this.isInitialized = true;
      return this.config;
    }
  }

  setupAxios() {
    const apiHost = this.config.REACT_APP_API_HOST || 'http://localhost';
    const apiPort = this.config.REACT_APP_API_PORT || '3000';
    
    const baseURL = `${apiHost}:${apiPort}`;
    
    // Set axios default base URL
    axios.defaults.baseURL = baseURL;
    
    console.log('Axios configured with base URL:', baseURL);
  }

  getConfig() {
    return this.config;
  }

  getApiUrl() {
    if (!this.config) {
      throw new Error('Configuration not initialized. Call initialize() first.');
    }
    const apiHost = this.config.REACT_APP_API_HOST || 'http://localhost';
    const apiPort = this.config.REACT_APP_API_PORT || '3000';
    return `${apiHost}:${apiPort}`;
  }

  getKeycloakConfig() {
    return this.config?.keycloak || {};
  }

  isDesktop() {
    return this.config?.IS_DESKTOP ?? false; // Default to true if not specified
  }
}

// Create a singleton instance
const configService = new ConfigService();

export default configService; 