import Keycloak from 'keycloak-js';

// Create a singleton KeycloakService class
class KeycloakService {
  constructor() {
    if (KeycloakService.instance) {
      return KeycloakService.instance;
    }

    this.keycloak = new Keycloak({
      url: 'http://localhost:8080/', // your Keycloak server
      realm: 'Plan-A-dev',
      clientId: 'dev-client',
    });
    
    this.initialized = false;
    this.initPromise = null;
    
    KeycloakService.instance = this;
  }

  init() {
    // If already initialized, return authentication state
    if (this.initialized) {
      return Promise.resolve(this.keycloak.authenticated);
    }

    // If initialization is in progress, return that promise
    if (this.initPromise) {
      return this.initPromise;
    }

    // Store the current location to redirect back after login/logout
    const currentPath = window.location.pathname;
    localStorage.setItem('auth_redirect_uri', currentPath);

    // Start a new initialization
    this.initPromise = new Promise((resolve) => {
      this.keycloak.init({ 
        onLoad: 'check-sso', 
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        checkLoginIframe: false // Disable login iframe checking
      })
        .then(authenticated => {
          console.log("Keycloak initialized, authenticated:", authenticated);
          this.initialized = true;
          
          // Setup token refresh
          this.setupTokenRefresh();
          
          resolve(authenticated);
        })
        .catch(error => {
          console.log("Keycloak initialization error:", error);
          this.initPromise = null; // Reset promise so we can try again
          resolve(false);
        });
    });

    return this.initPromise;
  }

  setupTokenRefresh() {
    // Setup token refresh
    if (this.keycloak.authenticated) {
      // Refresh token 30 seconds before it expires
      this.keycloak.onTokenExpired = () => {
        console.log('Token expired, refreshing...');
        this.keycloak.updateToken(30).catch(() => {
          console.log('Failed to refresh token, logging out');
          this.logout();
        });
      };
    }
  }

  login() {
    // Store the current URL to redirect back after login
    const redirectUri = window.location.origin + (localStorage.getItem('auth_redirect_uri') || '/');
    return this.keycloak.login({
      redirectUri: redirectUri
    });
  }

  logout() {
    // Clear the instance state
    this.initialized = false;
    this.initPromise = null;
    
    // Store the current URL to redirect back after logout
    const redirectUri = window.location.origin + (localStorage.getItem('auth_redirect_uri') || '/');
    
    // Perform logout
    return this.keycloak.logout({
      redirectUri: redirectUri
    });
  }

  register() {
    const redirectUri = window.location.origin + (localStorage.getItem('auth_redirect_uri') || '/');
    return this.keycloak.register({
      redirectUri: redirectUri
    });
  }

  getToken() {
    return this.keycloak.token;
  }

  getUsername() {
    return this.keycloak.tokenParsed?.preferred_username || 'Guest';
  }

  isAuthenticated() {
    return !!this.keycloak.authenticated;
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

export { 
  keycloakService, 
  initKeycloak, 
  login, 
  logout, 
  register,
  getToken, 
  getUsername,
  isAuthenticated
};