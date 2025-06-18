# Getting Started with Create React App

This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

## Available Scripts

In the project directory, you can run:

### `npm start`

Runs the app in the development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

The page will reload when you make changes.\
You may also see any lint errors in the console.

### `npm test`

Launches the test runner in the interactive watch mode.\
See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

### `npm run build`

Builds the app for production to the `build` folder.\
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.\
Your app is ready to be deployed!

See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.

### `npm run eject`

**Note: this is a one-way operation. Once you `eject`, you can't go back!**

If you aren't satisfied with the build tool and configuration choices, you can `eject` at any time. This command will remove the single build dependency from your project.

Instead, it will copy all the configuration files and the transitive dependencies (webpack, Babel, ESLint, etc) right into your project so you have full control over them. All of the commands except `eject` will still work, but they will point to the copied scripts so you can tweak them. At this point you're on your own.

You don't have to ever use `eject`. The curated feature set is suitable for small and middle deployments, and you shouldn't feel obligated to use this feature. However we understand that this tool wouldn't be useful if you couldn't customize it when you are ready for it.

## End-to-End (E2E) Testing with Docker Compose & Playwright

This project uses Playwright for E2E testing and Docker Compose to orchestrate the full microservices stack for realistic integration tests.

### How to Run E2E Tests Locally

1. **Start the stack:**
   ```sh
   docker-compose -f docker-compose.e2e.yml up --build
   ```
   This will launch all required services. The frontend will be available at http://localhost:4000.

2. **Run Playwright tests:**
   ```sh
   npx playwright test
   ```
   This will run all E2E tests in the `e2e/` directory against the running stack.

3. **View the test report:**
   ```sh
   npx playwright show-report
   ```

### How to Automate in CI

- In your CI pipeline, add steps to:
  1. Start the stack in the background:
     ```sh
     docker-compose -f docker-compose.e2e.yml up -d --build
     ```
  2. Wait for the frontend to be ready (e.g., poll http://localhost:4000 or use a wait script).
  3. Run Playwright tests:
     ```sh
     npx playwright test
     ```
  4. Tear down the stack:
     ```sh
     docker-compose -f docker-compose.e2e.yml down
     ```

- Make sure your Playwright config uses `http://localhost:4000` as the base URL.

- You can further automate readiness checks using tools like [`wait-on`](https://www.npmjs.com/package/wait-on) or custom scripts.

## Learn More

You can learn more in the [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).

To learn React, check out the [React documentation](https://reactjs.org/).

### Code Splitting

This section has moved here: [https://facebook.github.io/create-react-app/docs/code-splitting](https://facebook.github.io/create-react-app/docs/code-splitting)

### Analyzing the Bundle Size

This section has moved here: [https://facebook.github.io/create-react-app/docs/analyzing-the-bundle-size](https://facebook.github.io/create-react-app/docs/analyzing-the-bundle-size)

### Making a Progressive Web App

This section has moved here: [https://facebook.github.io/create-react-app/docs/making-a-progressive-web-app](https://facebook.github.io/create-react-app/docs/making-a-progressive-web-app)

### Advanced Configuration

This section has moved here: [https://facebook.github.io/create-react-app/docs/advanced-configuration](https://facebook.github.io/create-react-app/docs/advanced-configuration)

### Deployment

This section has moved here: [https://facebook.github.io/create-react-app/docs/deployment](https://facebook.github.io/create-react-app/docs/deployment)

### `npm run build` fails to minify

This section has moved here: [https://facebook.github.io/create-react-app/docs/troubleshooting#npm-run-build-fails-to-minify](https://facebook.github.io/create-react-app/docs/troubleshooting#npm-run-build-fails-to-minify)
