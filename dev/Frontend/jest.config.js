module.exports = {
  testEnvironment: 'jsdom',
  transformIgnorePatterns: [
    '/node_modules/(?!(axios)/)'
  ],
  transform: {
    '^.+\\.(js|jsx|ts|tsx)$': 'babel-jest',
  },
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy'
  },
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.js']
}; 