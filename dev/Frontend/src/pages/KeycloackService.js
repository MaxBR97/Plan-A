// src/auth/KeycloakService.js
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:8080/', // your Keycloak server
  realm: 'your-realm',
  clientId: 'your-client-id',
});

const initKeycloak = () =>
  new Promise((resolve, reject) => {
    keycloak.init({ onLoad: 'check-sso', silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html' })
      .then(authenticated => {
        resolve(authenticated);
      })
      .catch(reject);
  });

const login = () => keycloak.login();
const logout = () => keycloak.logout({ redirectUri: window.location.origin });
const getToken = () => keycloak.token;
const getUsername = () => keycloak.tokenParsed?.preferred_username;

export { keycloak, initKeycloak, login, logout, getToken, getUsername };
