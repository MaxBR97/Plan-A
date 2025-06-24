import Keycloak from 'keycloak-js';


// Create a singleton KeycloakService class
class KeycloakService {
  constructor() {
    if (KeycloakService.instance) {
      return KeycloakService.instance;
    }

    this.keycloak = null;
    this.initialized = false;
    this.initPromise = null;
    this.tokenRefreshInterval = null;
    
    KeycloakService.instance = this;
  }

  init() {
    console.log('Initializing Keycloak...');
    
    if (this.initialized && this.keycloak) {
      console.log('Keycloak already initialized');
      return Promise.resolve(this.keycloak.authenticated);
    }

    if (this.initPromise) {
      console.log('Keycloak initialization already in progress');
      return this.initPromise;
    }

    // Initialize Keycloak instance
    console.log('Creating new Keycloak instance...');
    this.keycloak = new Keycloak({
      url: 'http://localhost:8080',
      realm: 'plan-a',
      clientId: 'spring-gateway'
    });

    const currentPath = window.location.pathname;
    localStorage.setItem('auth_redirect_uri', currentPath);

    this.initPromise = new Promise((resolve) => {
      console.log('Starting Keycloak initialization...');
      this.keycloak.init({ 
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        checkLoginIframe: false,
        pkceMethod: 'S256',
        enableLogging: true
      })
      // this.keycloak.init({ 
      //   onLoad: 'login-required',
      //   pkceMethod: 'S256',
      //   checkLoginIframe: false,
      // })
        .then(authenticated => {
          console.log("Keycloak initialization successful, authenticated:", authenticated);
          this.initialized = true;
          this.setupAxiosInterceptor();
          if (authenticated) {
            console.log('Setting up token refresh and axios interceptor...');
            this.setupTokenRefresh();
            // this.setupAxiosInterceptor();
          }
          
          resolve(authenticated);
        })
        .catch(error => {
          console.error("Keycloak initialization error:", error);
          console.error("Error details:", {
            message: error.message,
            stack: error.stack,
            keycloak: this.keycloak
          });
          this.initPromise = null;
          this.keycloak = null;
          this.initialized = false;
          resolve(false);
        });
    });

    return this.initPromise;
  }

  setupTokenRefresh() {
    if (this.keycloak && this.keycloak.authenticated) {
      console.log('Setting up token refresh...');
      
      // Set up token refresh before expiration (refresh 60 seconds before expiry)
      this.keycloak.onTokenExpired = () => {
        console.log('Token expired, refreshing...');
        this.keycloak.updateToken(60).catch((error) => {
          console.error('Failed to refresh token:', error);
          this.logout();
        });
      };

      // Proactive token refresh - check every 5 minutes and refresh if needed
      this.tokenRefreshInterval = setInterval(() => {
        if (this.keycloak && this.keycloak.authenticated) {
          // Refresh token if it expires in less than 5 minutes
          this.keycloak.updateToken(300).catch((error) => {
            console.error('Failed to refresh token proactively:', error);
            // Don't logout immediately on proactive refresh failure
            // Only logout if the token is actually expired
            if (this.keycloak.isTokenExpired()) {
              this.logout();
            }
          });
        }
      }, 5 * 60 * 1000); // Check every 5 minutes
    }
  }

  setupAxiosInterceptor() {
    console.log('Setting up axios interceptors...');
    const axios = require('axios');

    axios.interceptors.request.use(
      (config) => {
        // Only add Authorization header if logged in and token is valid
        if (this.keycloak && this.keycloak.authenticated && this.keycloak.token && typeof this.keycloak.token === 'string' && this.keycloak.token.trim() !== '') {
          config.headers.Authorization = `Bearer ${this.keycloak.token}`;
        }
        // If not logged in, do not set Authorization header
        return config;
      },
      (error) => {
        console.error('Axios request interceptor error:', error);
        return Promise.reject(error);
      }
    );

    axios.interceptors.response.use(
      (response) => response,
      (error) => {
        console.error('Axios response interceptor error:', error);
        if (error.response?.status === 401) {
          console.log('Received 401, attempting login...');
          this.login();
        }
        return Promise.reject(error);
      }
    );
  }

  login() {
    console.log('Attempting login...');
    if (!this.keycloak) {
      console.error('Keycloak not initialized, attempting to initialize...');
      return this.init().then(() => {
        if (!this.keycloak) {
          throw new Error('Failed to initialize Keycloak');
        }
        return this.performLogin();
      });
    }
    return this.performLogin();
  }

  performLogin() {
    console.log('Performing login...');
    const redirectUri = window.location.origin + (localStorage.getItem('auth_redirect_uri') || '/');
    console.log('Redirect URI:', redirectUri);
    
    // Add error handler for login
    this.keycloak.onAuthError = (error) => {
      console.error('Keycloak auth error:', error);
    };

    this.keycloak.onAuthSuccess = () => {
      console.log('Keycloak auth success');
    };

    return this.keycloak.login({
      redirectUri: redirectUri,
      prompt: 'login'
    }).catch(error => {
      console.error('Login error:', error);
      throw error;
    });
  }

  logout() {
    console.log('Attempting logout...');
    if (!this.keycloak) {
      console.error('Keycloak not initialized');
      return Promise.reject('Keycloak not initialized');
    }

    // Clear the token refresh interval
    if (this.tokenRefreshInterval) {
      clearInterval(this.tokenRefreshInterval);
      this.tokenRefreshInterval = null;
    }

    this.initialized = false;
    this.initPromise = null;
    
    const redirectUri = window.location.origin + (localStorage.getItem('auth_redirect_uri') || '/');
    console.log('Logout redirect URI:', redirectUri);
    
    return this.keycloak.logout({
      redirectUri: redirectUri
    }).catch(error => {
      console.error('Logout error:', error);
      throw error;
    });
  }

  register() {
    console.log('Attempting registration...');
    if (!this.keycloak) {
      console.error('Keycloak not initialized, attempting to initialize...');
      return this.init().then(() => {
        if (!this.keycloak) {
          throw new Error('Failed to initialize Keycloak');
        }
        return this.performRegister();
      });
    }
    return this.performRegister();
  }

  performRegister() {
    console.log('Performing registration...');
    const redirectUri = window.location.origin + (localStorage.getItem('auth_redirect_uri') || '/');
    console.log('Register redirect URI:', redirectUri);
    return this.keycloak.register({
      redirectUri: redirectUri
    }).catch(error => {
      console.error('Registration error:', error);
      throw error;
    });
  }

  getToken() {
    return this.keycloak?.token;
  }

  getUsername() {
    return this.keycloak?.tokenParsed?.preferred_username || 'Guest';
  }

  isAuthenticated() {
    return !!this.keycloak?.authenticated;
  }

  hasRole(role) {
    return this.keycloak?.hasRealmRole(role) || false;
  }
}

// Create a single instance
const keycloakService = new KeycloakService();

// Export functions that use the singleton
const initKeycloak = () => keycloakService.init();
const login = () => keycloakService.login();
const logout = () => keycloakService.logout();
const register = () => keycloakService.register();
const getToken = () => keycloakService.getToken();
const getUsername = () => keycloakService.getUsername();
const isAuthenticated = () => keycloakService.isAuthenticated();
const hasRole = (role) => keycloakService.hasRole(role);

export { 
  keycloakService, 
  initKeycloak, 
  login, 
  logout, 
  register,
  getToken, 
  getUsername,
  isAuthenticated,
  hasRole
};