import { test, expect, Locator } from '@playwright/test';
import { login, deleteTestImage, CreateNewImageAndGoToConfigureImage, configureDomain, assertNoErrorMessage, completeImageCreationAndConfiguration, configureConstraints, configurePreferences, previewImage, goBackToConfiguration, configureNamesAndTags, configureSolverSettings, logout, checkImageInPublicImages, checkImageInMyImages, configureImageVisibility, addEntryToSetInPreview } from './test-utils';

test.describe('Basic Guest Home Page', () => {
  test('should display welcome message for guest users', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('Welcome, Guest!')).toBeVisible();
  });

  test('should show login and signup buttons for guests', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('button', { name: /^Login$/ })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Sign Up' })).toBeVisible();
  });

  test('should not show create image button for guests', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('link', { name: 'Create new image' })).not.toBeVisible();
  });
});

test.describe('Image creation and configuration', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, 'max', '1234');
  });

  test('user can create an image with name, description, and sampleZimplCode without error', async ({ page }) => {
    const imageName = `E2E Test Image ${Date.now()}`;
    const imageDescription = 'E2E test image description';
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
  });

  test('user can create and configure domain, then complete image creation with no errors', async ({ page }) => {
    const imageName = `E2E Config Image ${Date.now()}`;
    const imageDescription = 'E2E config test image';
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' }
    });
    await assertNoErrorMessage(page);
    await completeImageCreationAndConfiguration(page);
    await assertNoErrorMessage(page);
  });

  test('user can create, configure domain, add constraint module, and verify in preview with no errors', async ({ page }) => {
    const imageName = `E2E Constraint Image ${Date.now()}`;
    const imageDescription = 'E2E constraint test image';
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
    // Configure domain: Shibutsim, PreAssignedShibutsim, People, shiftTime; untick restHours
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' },
      untick: { params: ['restHours'] }
    });
    await assertNoErrorMessage(page);
    // Configure constraint module
    const moduleName = `E2E Constraint Module ${Date.now()}`;
    await configureConstraints(page, [{
      moduleName,
      description: 'Test constraint module',
      constraints: ['EnforceRestTimes'],
      inputSets: [],
      inputParams: ['restHours']
    }]);
    await assertNoErrorMessage(page);
    // Move to image preview
    await previewImage(page);
    // Check that the constraint module exists in preview under 'constraints' tab
    await page.getByRole('button', { name: /constraints/i }).click();
    await expect(page.getByText(moduleName)).toBeVisible();
    // Finish image creation
    await completeImageCreationAndConfiguration(page);
    await assertNoErrorMessage(page);
  });

  test('user can create, configure domain, add preference module, and verify in preview with no errors', async ({ page }) => {
    const imageName = `E2E Preference Image ${Date.now()}`;
    const imageDescription = 'E2E preference test image';
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
    // Configure domain: Shibutsim, PreAssignedShibutsim, People, shiftTime; untick bias
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' },
      untick: { params: ['bias'] }
    });
    await assertNoErrorMessage(page);
    // Find the SECOND available preference

    // Configure preference module using the utility
    const moduleName = `E2E Preference Module ${Date.now()}`;
    await configurePreferences(page, [{
      moduleName,
      description: 'Test preference module',
      preference: '(sum<i,person,station,time>inindexSetOfPeople*Mishmarot|time<=card(People)/card(Station)*shiftTime:(...',
      inputSets: [],
      inputParams: [],
      costParam: 'bias'
    }]);
    await assertNoErrorMessage(page);
    // Move to image preview
    await previewImage(page);
    // Check that the preference module exists in preview under 'optimization goals' tab
    await page.getByRole('button', { name: /optimization goals/i }).click();
    await expect(page.getByText(moduleName)).toBeVisible();
    // Finish image creation
    await completeImageCreationAndConfiguration(page);
    await assertNoErrorMessage(page);
  });

  test('user can create, configure domain, configure names and tags, and verify aliases in preview', async ({ page }) => {
    const imageName = `E2E Names Tags Image ${Date.now()}`;
    const imageDescription = 'E2E names and tags test image';
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
    
    // Configure domain: Shibutsim, bound set PreAssignedShibutsim, set People, param shiftTime
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' }
    });
    await assertNoErrorMessage(page);
    
    // Configure names and tags
    await configureNamesAndTags(page, {
      sets: [{
        name: 'People',
        alias: 'Available People',
        tags: ['Person Name']
      }],
      params: [{
        name: 'shiftTime',
        alias: 'Time per shift'
      }]
    });
    await assertNoErrorMessage(page);
    
    // Go to preview
    await previewImage(page);
    
    // Click on Domain tab
    await page.getByRole('button', { name: /domain/i }).click();
    
    // Verify the set input box has the correct title and tag
    await expect(page.getByText('Available People')).toBeVisible();
    await expect(page.getByText('Person Name')).toBeVisible();
    
    // Verify the parameter box has the correct title
    await expect(page.getByText('Time per shift')).toBeVisible();
    
    // Add an entry to the Available People set
    await addEntryToSetInPreview(page, 'Available People', 'Shlomi', 'Person Name');
    await assertNoErrorMessage(page);
    
    // Verify the entry was added successfully by checking the input field value
    const shlomiInput = page.locator('input[value="Shlomi"]');
    await expect(shlomiInput).toBeVisible({ timeout: 10000 });
    
    // Finish image creation
    await completeImageCreationAndConfiguration(page);
    await assertNoErrorMessage(page);
  });

  test('user can create, configure domain, configure solver settings, and verify solver emphasis in preview', async ({ page }) => {
    const imageName = `E2E Names Tags Image ${Date.now()}`;
    const imageDescription = 'E2E names and tags test image';
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
    
  
    // Configure solver settings to select only Default and Optimallity
    await configureSolverSettings(page, ['Default', 'Optimallity']);
    await assertNoErrorMessage(page);
    
    // Go to preview
    await previewImage(page);
    
    // Verify that only Default and Optimallity radio buttons are present in solver emphasis
    const solverRadioButtons = page.locator('.script-options input[type="radio"]');
    const radioButtonCount = await solverRadioButtons.count();
    expect(radioButtonCount).toBe(2);
    
    // Verify the specific radio buttons exist
    await expect(page.locator('input[type="radio"][id="script-Default"]')).toBeVisible();
    await expect(page.locator('input[type="radio"][id="script-Optimallity"]')).toBeVisible();
    
    // Verify no other solver settings are present
    await expect(page.locator('input[type="radio"][id="script-Tree search"]')).toHaveCount(0);
    await expect(page.locator('input[type="radio"][id="script-Feasibility"]')).toHaveCount(0);
    await expect(page.locator('input[type="radio"][id="script-Aggressive static analysis"]')).toHaveCount(0);
    await expect(page.locator('input[type="radio"][id="script-Numerics"]')).toHaveCount(0);
    
    // Finish image creation
    await completeImageCreationAndConfiguration(page);
    await assertNoErrorMessage(page);
  });

  test('user can create a complete image and verify it appears in My Images panel', async ({ page }) => {
    // Use a slower pace for debug
    const DEBUG_DELAY = 1000;
    
    const imageName = `Complete E2E Image ${Date.now()}`;
    const imageDescription = `Complete E2E test image ${Date.now()}`;
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Go to preview
    await previewImage(page);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Click "Finish and save image" button
    await page.getByRole('button', { name: /Finish and save image/i }).click();
    
    // Wait for navigation back to main page
    await page.waitForSelector('.main-page', { timeout: 10000 });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify we're on the main page
    await expect(page.getByText('Welcome to Plan A!')).toBeVisible();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Look for the image in "My Images" section - use robust selector
    const myImagesSection = page.locator('.my-images-container:has(h2:text("My Images"))');
    await expect(myImagesSection.getByText('My Images')).toBeVisible();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Click refresh button in My Images section
    const refreshBtn = myImagesSection.locator('button.refresh-button');
    if (await refreshBtn.count() > 0) {
      await refreshBtn.click();
      await page.waitForTimeout(DEBUG_DELAY);
    }
    
    // Add debugging to see what images are actually present
    const allImageCards = myImagesSection.locator('.image-card h3');
    const imageCount = await allImageCards.count();
    // console.log(`Found ${imageCount} image cards in My Images section`);
    for (let i = 0; i < imageCount; i++) {
      const imageTitle = await allImageCards.nth(i).textContent();
      // console.log(`Image ${i + 1}: ${imageTitle}`);
    }
    // console.log(`Looking for image: ${imageName}`);
    
    // Verify the created image card is present (scoped to My Images section)
    await expect(myImagesSection.getByText(imageName)).toBeVisible({ timeout: 10000 });
    await expect(myImagesSection.getByText(imageDescription)).toBeVisible();
    
    // Optional: Verify the image card is clickable and has delete button
    const imageCard = myImagesSection.locator('.image-card', { hasText: imageName });
    await expect(imageCard).toBeVisible();
    await expect(imageCard.locator('.delete-button')).toBeVisible();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Optional: Test that clicking the image card navigates to solution preview
    await imageCard.click();
    await page.waitForSelector('.solution-preview-page', { timeout: 10000 });
    await expect(page.getByText(imageName)).toBeVisible();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Navigate back to main page
    await page.goto('/');
    await page.waitForSelector('.main-page', { timeout: 10000 });
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Final verification that image is still in My Images (scoped)
    const myImagesSection2 = page.locator('.my-images-container:has(h2:text("My Images"))');
    await expect(myImagesSection2.getByText(imageName)).toBeVisible({ timeout: 10000 });
  });

  test('user tries to create an image the same parameter configured for a constraint module and domain, leads to an error', async ({ page }) => {
    const imageName = `E2E Constraint Image ${Date.now()}`;
    const imageDescription = 'E2E constraint test image';
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
    // Configure domain: Shibutsim, PreAssignedShibutsim, People, shiftTime; untick restHours
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime', 'restHours'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' },
    });
    await assertNoErrorMessage(page);
    // Configure constraint module
    const moduleName = `E2E Constraint Module ${Date.now()}`;
    await configureConstraints(page, [{
      moduleName,
      description: 'Test constraint module',
      constraints: ['EnforceRestTimes'],
      inputSets: [],
      inputParams: ['restHours']
    }]);
    await assertNoErrorMessage(page);
    
    // Click "Preview Image" button to finish configuration
    const previewButton = page.locator('button.finish-button', { hasText: 'Preview Image' });
    await expect(previewButton).toBeVisible({ timeout: 10000 });
    await previewButton.click();
    
    // Wait for potential error to appear instead of navigation
    await page.waitForTimeout(2000);
    
    // Assert that an error message appears
    const errorContainer = page.locator('.error-container');
    await expect(errorContainer).toBeVisible({ timeout: 10000 });
    
    // Verify we're still on the configuration page (not navigated to preview)
    await expect(page.getByText('Configure Image')).toBeVisible();
    
    // Optional: Check that the error message contains relevant content about the conflict
    const errorText = await errorContainer.innerText();
    expect(errorText.toLowerCase()).toContain('conflict');
    
    console.log('Error message appeared as expected when trying to configure conflicting parameters');
  });

});

test.describe('Public and Private Image Functionality', () => {
  test('two users can create images with different visibility settings and verify access', async ({ page }) => {
    const DEBUG_DELAY = 1000;
    
    // First user: max
    await login(page, 'max', '1234');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Create first image (public)
    const publicImageName = `Public E2E Image ${Date.now()}`;
    const publicImageDescription = 'Public E2E test image';
    await CreateNewImageAndGoToConfigureImage(page, publicImageName, publicImageDescription);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Configure domain for the public image
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' }
    });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Make the image public in the configuration menu
    await configureImageVisibility(page, true);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Go to preview and finish configuration
    await previewImage(page);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Finish image creation
    await page.getByRole('button', { name: /Finish and save image/i }).click();
    await page.waitForSelector('.main-page', { timeout: 10000 });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify the public image appears in My Images for the first user
    await checkImageInMyImages(page, publicImageName, true);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Create second image (private)
    const privateImageName = `Private E2E Image ${Date.now()}`;
    const privateImageDescription = 'Private E2E test image';
    await CreateNewImageAndGoToConfigureImage(page, privateImageName, privateImageDescription);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Configure domain for the private image
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' }
    });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Make sure the image is NOT public in the configuration menu
    await configureImageVisibility(page, false);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Go to preview and finish configuration
    await previewImage(page);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Finish image creation
    await page.getByRole('button', { name: /Finish and save image/i }).click();
    await page.waitForSelector('.main-page', { timeout: 10000 });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify both images appear in My Images for the first user
    await checkImageInMyImages(page, publicImageName, true);
    await checkImageInMyImages(page, privateImageName, true);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Logout first user
    await logout(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Login as second user: alice
    await login(page, 'alice', '1234');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify the public image appears in Public Images for the second user
    await checkImageInPublicImages(page, publicImageName, true);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify the private image does NOT appear in Public Images for the second user
    await checkImageInPublicImages(page, privateImageName, false);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify neither image appears in My Images for the second user (since they didn't create them)
    await checkImageInMyImages(page, publicImageName, false);
    await checkImageInMyImages(page, privateImageName, false);
    await page.waitForTimeout(DEBUG_DELAY);
  });

  test('user can create public image and another user can access and try to optimize it', async ({ page }) => {
    const DEBUG_DELAY = 1000;
    
    // First user: max creates a public image
    await login(page, 'max', '1234');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Create a public image
    const publicImageName = `Optimizable Public Image ${Date.now()}`;
    const publicImageDescription = 'Public image for optimization test';
    await CreateNewImageAndGoToConfigureImage(page, publicImageName, publicImageDescription);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Configure domain
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' }
    });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Make the image public
    await configureImageVisibility(page, true);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Go to preview and finish configuration
    await previewImage(page);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Finish image creation
    await page.getByRole('button', { name: /Finish and save image/i }).click();
    await page.waitForSelector('.main-page', { timeout: 10000 });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify the image appears in My Images for the first user
    await checkImageInMyImages(page, publicImageName, true);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Logout first user
    await logout(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Login as Alice
    await login(page, 'alice', '1234');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Search for the public image
    const searchInput = page.locator('#search-input');
    await searchInput.fill(publicImageName);
    await page.locator('.search-button').click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Wait for search results and click on the image card
    const imageCard = page.locator('.image-card', { hasText: publicImageName });
    await expect(imageCard).toBeVisible({ timeout: 10000 });
    await imageCard.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Wait for navigation to solution preview page
    await page.waitForSelector('.solution-preview-page', { timeout: 10000 });
    await expect(page.getByText(publicImageName)).toBeVisible();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Look for and click the optimize button
    const optimizeButton = page.getByRole('button', { name: /optimize/i });
    await expect(optimizeButton).toBeVisible({ timeout: 10000 });
    await optimizeButton.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify no error messages are displayed after clicking optimize
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Select the variable "Shibutsim" in the variable selector
    const variableSelector = page.locator('select');
    await expect(variableSelector).toBeVisible({ timeout: 10000 });
    await variableSelector.selectOption('Shibutsim');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Set timeout to 9 seconds in the Timeout Setting
    const timeoutInput = page.locator('input[type="text"]').first();
    await expect(timeoutInput).toBeVisible({ timeout: 10000 });
    await timeoutInput.fill('9');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // In "Solver Emphasis" choose the "Optimallity" radio button
    const optimallityRadio = page.locator('input[type="radio"][id="script-Optimallity"]');
    await expect(optimallityRadio).toBeVisible({ timeout: 10000 });
    await optimallityRadio.check();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // In "Dimension Order" drag the first draggable object to be second in the list
    const dimensionList = page.locator('.dimension-list');
    await expect(dimensionList).toBeVisible({ timeout: 10000 });
    
    // Get the first and second dimension items
    const firstDimension = dimensionList.locator('.dimension-item').first();
    const secondDimension = dimensionList.locator('.dimension-item').nth(1);
    
    // Perform drag and drop: drag first item to second position
    await firstDimension.dragTo(secondDimension);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Inside the superTable, click on the cell/header that says "Max" to select it
    // First, wait for the table to be visible
    const tableContainer = page.locator('.table-container');
    await expect(tableContainer).toBeVisible({ timeout: 10000 });
    
    // Look for the cell/header with "Max" text
    const maxCell = page.locator('.super-table, .table-container', { hasText: 'Max' }).first();
    await expect(maxCell).toBeVisible({ timeout: 10000 });
    await maxCell.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Click "Edit Table" to enter edit mode
    const editTableButton = page.getByRole('button', { name: /edit table/i });
    await expect(editTableButton).toBeVisible({ timeout: 10000 });
    await editTableButton.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Get the initial number of columns (headers)
    const initialColumns = page.locator('.super-table th, .table-container th, .super-table .header, .table-container .header');
    const initialColumnCount = await initialColumns.count();
    console.log(`Initial column count: ${initialColumnCount}`);
    
    // Click on a '+' sign to add a column
    // First, let's see what add buttons are available
    const allAddButtons = page.locator('.add-button');
    const addButtonCount = await allAddButtons.count();
    console.log(`Found ${addButtonCount} add buttons in the table`);
    
    // Click on the first available add button
    const addColumnButton = allAddButtons.first();
    await expect(addColumnButton).toBeVisible({ timeout: 10000 });
    await addColumnButton.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify a new column was added by checking that the number of columns increased by 1
    const newColumns = page.locator('.super-table th, .table-container th, .super-table .header, .table-container .header');
    const newColumnCount = await newColumns.count();
    console.log(`New column count: ${newColumnCount}`);
    expect(newColumnCount).toBe(initialColumnCount + 1);
    
    // Click "Done Editing" to exit edit mode
    const doneEditingButton = page.getByRole('button', { name: /done editing/i });
    await expect(doneEditingButton).toBeVisible({ timeout: 10000 });
    await doneEditingButton.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Click "Optimize" again
    await optimizeButton.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Validate no error messages are given throughout the entire process
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // THIRD OPTIMIZATION ITERATION - WITH INVALID PARAMETER
    // Click on the "Domain" tab to access parameter inputs
    const domainTab = page.getByRole('button', { name: /domain/i });
    await expect(domainTab).toBeVisible({ timeout: 10000 });
    await domainTab.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Wait for the Domain tab content to be visible
    await page.waitForSelector('.tab-content', { timeout: 10000 });
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Look for the 'shiftTime' parameter input box
    // First, find the parameter section
    const parameterSection = page.locator('.module-section', { hasText: 'Parameters' });
    await expect(parameterSection).toBeVisible({ timeout: 10000 });
    
    // Find the shiftTime parameter input within the parameter section
    const shiftTimeInput = parameterSection.locator('input[type="text"], input[type="number"]', { hasText: '' }).first();
    await expect(shiftTimeInput).toBeVisible({ timeout: 10000 });
    
    // Clear the input and set it to '-1'
    await shiftTimeInput.clear();
    await shiftTimeInput.fill('-1');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Click "Optimize" button
    const optimizeButtonThird = page.getByRole('button', { name: /optimize/i });
    await expect(optimizeButtonThird).toBeVisible({ timeout: 10000 });
    await optimizeButtonThird.click();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Expect an error message to appear
    const errorContainer = page.locator('.error-container');
    await expect(errorContainer).toBeVisible({ timeout: 10000 });
    
    // Verify we're still on the solution results page (not navigated away)
    await expect(page.getByText('Timeout Setting')).toBeVisible();
    
    // Optional: Check that the error message contains relevant content
    const errorText = await errorContainer.innerText();
    console.log('Error message content:', errorText);
    
    console.log('All optimization interactions completed successfully');
  });

  test('guest user can see public images created by other users', async ({ page }) => {
    const DEBUG_DELAY = 1000;
    
    // First user: max creates a public image
    await login(page, 'max', '1234');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Create a public image
    const publicImageName = `Guest Visible Public Image ${Date.now()}`;
    const publicImageDescription = 'Public image visible to guest users';
    await CreateNewImageAndGoToConfigureImage(page, publicImageName, publicImageDescription);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Configure domain
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' }
    });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Make the image public
    await configureImageVisibility(page, true);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Go to preview and finish configuration
    await previewImage(page);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Finish image creation
    await page.getByRole('button', { name: /Finish and save image/i }).click();
    await page.waitForSelector('.main-page', { timeout: 10000 });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify the image appears in My Images for the first user
    await checkImageInMyImages(page, publicImageName, true);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Logout first user (now guest user)
    await logout(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify we're now a guest user
    await expect(page.getByText('Welcome, Guest!')).toBeVisible();
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify the public image is visible in Public Images for guest user
    await checkImageInPublicImages(page, publicImageName, true);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify guest user cannot see the image in My Images (since they're not logged in)
    await checkImageInMyImages(page, publicImageName, false);
    await page.waitForTimeout(DEBUG_DELAY);
    
    console.log('Guest user can successfully see public images created by other users');
  });

  test('user can delete their created image and verify it is not visible anywhere', async ({ page }) => {
    const DEBUG_DELAY = 600;
    
    // First user: max creates a public image
    await login(page, 'max', '1234');
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Create a public image
    const imageName = `Deletable Public Image ${Date.now()}`;
    const imageDescription = 'Public image to be deleted';
    await CreateNewImageAndGoToConfigureImage(page, imageName, imageDescription);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Configure domain
    await configureDomain(page, {
      variables: ['Shibutsim'],
      sets: ['People'],
      params: ['shiftTime'],
      boundSets: { Shibutsim: 'PreAssignedShibutsim' }
    });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Make the image public
    await configureImageVisibility(page, true);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Go to preview and finish configuration
    await previewImage(page);
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Finish image creation
    await page.getByRole('button', { name: /Finish and save image/i }).click();
    await page.waitForSelector('.main-page', { timeout: 10000 });
    await assertNoErrorMessage(page);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify the image appears in My Images for the creator
    await checkImageInMyImages(page, imageName, true);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Verify the image appears in Public Images for the creator
    await checkImageInPublicImages(page, imageName, true);
    await page.waitForTimeout(DEBUG_DELAY);
    
    // Delete the image using the deleteTestImage utility
    await deleteTestImage(page, imageName);
    await page.waitForTimeout(DEBUG_DELAY);
    console.log('Image deleted1');
    // Verify the image is no longer in My Images
    await checkImageInMyImages(page, imageName, false);
    await page.waitForTimeout(DEBUG_DELAY);
    console.log('Image deleted2');
    // Verify the image is no longer in Public Images
    await checkImageInPublicImages(page, imageName, false);
    await page.waitForTimeout(DEBUG_DELAY);
    console.log('Image deleted3');
    // Logout to become guest user
    await logout(page);
    await page.waitForTimeout(DEBUG_DELAY);
    console.log('Image deleted4');
    // Verify we're now a guest user
    await expect(page.getByText('Welcome, Guest!')).toBeVisible();
    await page.waitForTimeout(DEBUG_DELAY);
    console.log('Image deleted5');
    // Verify the deleted image is not visible in Public Images for guest user
    await checkImageInPublicImages(page, imageName, false);
    await page.waitForTimeout(DEBUG_DELAY);
    console.log('Image deleted6');
    // Verify guest user cannot see the image in My Images
    await checkImageInMyImages(page, imageName, false);
    await page.waitForTimeout(DEBUG_DELAY);
    
    console.log('Image deletion verified - image is not visible anywhere after deletion');
  });
}); 