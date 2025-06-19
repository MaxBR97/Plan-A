import { Page, expect } from '@playwright/test';

export async function login(page: Page, username: string, password: string) {
  await page.goto('/');
  
  // Wait for the page to load and login button to be available
  await page.waitForLoadState('networkidle');
  
  // Wait for either the app's login button or the Keycloak login form
  const loginButton = page.getByRole('button', { name: /^Login$/ });
  await expect(loginButton).toBeVisible({ timeout: 10000 });
  await loginButton.click();
  
  // Wait for navigation to complete after clicking login
  await page.waitForLoadState('networkidle');
  
  // Wait for either Keycloak form or direct login form with proper error handling
  let isKeycloak = false;
  try {
    // Wait a bit for the page to settle after navigation
    await page.waitForTimeout(2000);
    isKeycloak = await page.locator('form#kc-form-login').count() > 0;
  } catch (error) {
    // If execution context was destroyed, wait and try again
    console.log('Execution context destroyed during navigation, retrying...');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);
    try {
      isKeycloak = await page.locator('form#kc-form-login').count() > 0;
    } catch (retryError) {
      // If still failing, assume it's not Keycloak
      console.log('Assuming non-Keycloak login form');
      isKeycloak = false;
    }
  }
  
  if (isKeycloak) {
    // On Keycloak login page
    await page.waitForSelector('input#username', { timeout: 10000 });
    await page.fill('input#username', username);
    await page.fill('input#password', password);
    await page.click('input[type="submit"][value="Sign In"]');
  } else {
    // Direct login form (if not using Keycloak)
    try {
      await page.waitForSelector('input[name="username"], input[type="text"]', { timeout: 10000 });
      await page.fill('input[name="username"], input[type="text"]', username);
      await page.fill('input[name="password"], input[type="password"]', password);
      await page.click('input[type="submit"], button[type="submit"]');
    } catch (error) {
      // If direct login form not found, try alternative selectors
      console.log('Direct login form not found, trying alternative selectors...');
      await page.waitForSelector('input[type="text"]', { timeout: 10000 });
      await page.fill('input[type="text"]', username);
      await page.fill('input[type="password"]', password);
      await page.click('button[type="submit"], input[type="submit"]');
    }
  }

  // Wait for successful login with multiple possible success indicators
  try {
    // First try the welcome message
    await expect(page.getByText(/Welcome,\s*[^!]+!/)).toBeVisible({ timeout: 15000 });
  } catch (error) {
    // If that fails, try alternative success indicators
    try {
      // Look for logout button (indicates successful login)
      await expect(page.getByRole('button', { name: /logout|sign out|log out/i })).toBeVisible({ timeout: 5000 });
    } catch (error2) {
      // If that also fails, try looking for the main page content
      try {
        await expect(page.locator('.main-page')).toBeVisible({ timeout: 5000 });
        // Additional check: verify we're not still on login page
        const loginButtonStillVisible = await page.getByRole('button', { name: /^Login$/ }).count() > 0;
        if (loginButtonStillVisible) {
          throw new Error('Still on login page after login attempt');
        }
      } catch (error3) {
        // If all checks fail, throw the original error with more context
        console.error('Login failed. Page content:', await page.content());
        throw new Error(`Login failed after multiple attempts. Original error: ${error.message}`);
      }
    }
  }
  
  // Additional wait to ensure the page is fully loaded
  await page.waitForLoadState('networkidle');
}

export async function CreateNewImageAndGoToConfigureImage(page: Page, name: string, description: string, zimplCode?: string) {
  await page.getByRole('link', { name: 'Create new image' }).click();

  // Fill in image details
  await page.fill('input.image-name-input', name);
  await page.fill('textarea.description-textarea', description);

  // Optionally fill in ZIMPL code if provided
  if (zimplCode) {
    await page.fill('textarea#zimpl-code', zimplCode);
  }

  // Click the upload button
  await page.click('button.upload-button', { timeout: 10000 });

  // Wait for navigation to configuration menu
  await page.getByText('Configure Image').waitFor();
}

export async function deleteTestImage(page: Page, imageName: string) {
  // Set up dialog handler to automatically accept the confirmation dialog
  page.on('dialog', dialog => dialog.accept());
  
  // Find the image card by name in the My Images section
  const myImagesSection = page.locator('.my-images-container:has(h2:text("My Images"))');
  const imageCard = myImagesSection.locator('.image-card', { hasText: imageName });
  await expect(imageCard).toBeVisible({ timeout: 10000 });
  
  // Click delete button (× button) within the image card
  const deleteButton = imageCard.locator('.delete-button');
  await expect(deleteButton).toBeVisible({ timeout: 10000 });
  await deleteButton.click();
  
  // Wait for the image card to disappear (indicating successful deletion)
  await expect(imageCard).not.toBeVisible({ timeout: 10000 });
  
  // Wait a bit more to ensure the API call completes
  await page.waitForTimeout(2000);
}

/**
 * Configure the domain (inputs/outputs) for an image.
 * @param page Playwright page
 * @param options {
 *   variables: string[] (identifiers to select as outputs),
 *   sets: string[] (set names to select as inputs),
 *   params: string[] (param names to select as inputs),
 *   boundSets?: { [variable: string]: string },
 *   tags?: { [variable: string]: string[] },
 *   untick?: {
 *     variables?: string[] (identifiers to ensure are UNchecked as outputs),
 *     sets?: string[] (set names to ensure are UNchecked as inputs),
 *     params?: string[] (param names to ensure are UNchecked as inputs)
 *   }
 * }
 *
 * This function will tick the requested variables/sets/params, then untick any specified in the 'untick' option, before clicking Continue.
 */
export async function configureDomain(page: Page, options: {
  variables: string[],
  sets: string[],
  params: string[],
  boundSets?: { [variable: string]: string },
  tags?: { [variable: string]: string[] },
  untick?: {
    variables?: string[],
    sets?: string[],
    params?: string[]
  }
}) {
  // Navigate to Configure Inputs/Outputs
  await page.locator('.configure-menu-item', { hasText: 'Configure Domain Inputs & Outputs' }).click();
  // Select variables (outputs)
  for (const variable of options.variables) {
    const checkbox = page.getByRole('checkbox', { name: variable, exact: true });
    if (!(await checkbox.isChecked())) {
      await checkbox.check();
    }
    // Set tags if provided
    if (options.tags && options.tags[variable]) {
      for (let i = 0; i < options.tags[variable].length; ++i) {
        const tagInput = page.locator(`.variable-config-item:has(.variable-name:text-is("${variable}")) .tag-inputs-row input`).nth(i);
        await tagInput.fill(options.tags[variable][i]);
      }
    }
    // Set bound set if provided
    if (options.boundSets && options.boundSets[variable]) {
      const select = page.locator(`.variable-config-item:has(.variable-name:text-is("${variable}")) select`);
      await select.selectOption({ label: options.boundSets[variable] });
    }
  }
  // Select sets
  for (const set of options.sets) {
    const checkbox = page.getByLabel(set);
    if (!(await checkbox.isChecked())) {
      await checkbox.check();
    }
  }
  // Select params
  for (const param of options.params) {
    const checkbox = page.getByLabel(param);
    if (!(await checkbox.isChecked())) {
      await checkbox.check();
    }
  }
  // Untick variables if requested
  if (options.untick && options.untick.variables) {
    for (const variable of options.untick.variables) {
      const checkbox = page.getByRole('checkbox', { name: variable, exact: true });
      if (await checkbox.isChecked()) {
        await checkbox.uncheck();
      }
    }
  }
  // Untick sets if requested
  if (options.untick && options.untick.sets) {
    for (const set of options.untick.sets) {
      const checkbox = page.getByLabel(set);
      if (await checkbox.isChecked()) {
        await checkbox.uncheck();
      }
    }
  }
  // Untick params if requested
  if (options.untick && options.untick.params) {
    for (const param of options.untick.params) {
      const checkbox = page.getByLabel(param);
      if (await checkbox.isChecked()) {
        await checkbox.uncheck();
      }
    }
  }
  // Continue back to ConfigureImageMenu
  await page.getByRole('link', { name: /Continue/i }).click();
  await page.getByText('Configure Image').waitFor();
}

/**
 * Configure constraint modules for an image.
 * @param page Playwright page
 * @param modules Array of modules to create, each with:
 *   - moduleName: string
 *   - description: string
 *   - constraints: string[] (identifiers)
 *   - inputSets: string[]
 *   - inputParams: string[]
 */
export async function configureConstraints(page: Page, modules: Array<{
  moduleName: string,
  description: string,
  constraints: string[],
  inputSets: string[],
  inputParams: string[]
}>) {
  // Navigate to Configure Constraints
  await page.locator('.configure-menu-item', { hasText: /Configure Constraints/i }).click();
  for (const module of modules) {
    // Fill module name and description
    await page.fill('input[placeholder="Enter module name"]', module.moduleName);
    await page.fill('textarea[placeholder="Enter module description"]', module.description);
    // Select constraints
    for (const constraint of module.constraints) {
      const constraintItem = page.locator('.constraint-item', { hasText: constraint });
      const classAttr = await constraintItem.getAttribute('class');
      if (!classAttr || !classAttr.includes('selected')) {
        await constraintItem.click();
      }
    }
    // Select input sets
    for (const set of module.inputSets) {
      const checkbox = page.getByLabel(set);
      if (!(await checkbox.isChecked())) {
        await checkbox.check();
      }
    }
    // Select input params
    for (const param of module.inputParams) {
      const checkbox = page.getByLabel(param);
      if (!(await checkbox.isChecked())) {
        await checkbox.check();
      }
    }
    // Save module
    await page.getByRole('button', { name: /Create Module/i }).click();
  }
  // Continue back to ConfigureImageMenu
  await page.getByRole('button', { name: /Continue/i }).click();
  await page.getByText('Configure Image').waitFor();
}

/**
 * Configure preference modules for an image.
 * @param page Playwright page
 * @param modules Array of modules to create, each with:
 *   - moduleName: string
 *   - description: string
 *   - preference: string (identifier)
 *   - inputSets: string[]
 *   - inputParams: string[]
 *   - costParam: string
 */
export async function configurePreferences(page: Page, modules: Array<{
  moduleName: string,
  description: string,
  preference: string,
  inputSets: string[],
  inputParams: string[],
  costParam: string
}>) {
  // Navigate to Configure Optimization Goals
  await page.locator('.configure-menu-item', { hasText: /Configure Optimization Goals/i }).click();
  for (const module of modules) {
    // Fill module name and description
    await page.fill('input[placeholder="Enter module name"]', module.moduleName);
    await page.fill('textarea[placeholder="Enter module description"]', module.description);
    // Select preference
    const preferenceItem = page.locator('.preference-item', { hasText: module.preference });
    const classAttr = await preferenceItem.getAttribute('class');
    if (!classAttr || !classAttr.includes('selected')) {
      await preferenceItem.click();
    }
    // Select input sets
    for (const set of module.inputSets) {
      const checkbox = page.getByLabel(set);
      if (!(await checkbox.isChecked())) {
        await checkbox.check();
      }
    }
    // Select input params
    for (const param of module.inputParams) {
      const checkbox = page.getByLabel(param);
      if (!(await checkbox.isChecked())) {
        await checkbox.check();
      }
    }
    // Select cost param
    await page.selectOption('select.cost-param-select', { label: module.costParam });
    // Save module
    await page.getByRole('button', { name: /Create Module/i }).click();
  }
  // Continue back to ConfigureImageMenu
  await page.getByRole('button', { name: /Continue/i }).click();
  await page.getByText('Configure Image').waitFor();
}

/**
 * Preview the image from ConfigureImageMenu (go to preview page)
 */
export async function previewImage(page: Page) {
  // Click the 'Preview Image' button (class 'finish-button')
  const previewBtn = page.locator('button.finish-button', { hasText: 'Preview Image' });
  await previewBtn.click();
  // Wait for the preview page to load (look for a unique element)
  await page.waitForSelector('.solution-preview-page, .preview-title, .page-title');
}

/**
 * Go back to configuration from SolutionPreviewPage
 */
export async function goBackToConfiguration(page: Page) {
  // The button is a link with class 'back-button' and text 'Back to Configuration'
  const backButton = page.locator('a.back-button', { hasText: 'Back to Configuration' });
  await backButton.click();
  // Wait for ConfigureImageMenu to load
  await page.getByText('Configure Image').waitFor();
}

/**
 * Cancel image creation from either ConfigureImageMenu or SolutionPreviewPage
 * (returns to homepage, deletes image if needed)
 */
export async function cancelImageCreation(page: Page) {
  // Try to detect which page we're on
  const isOnPreview = await page.locator('.solution-preview-page').count() > 0;
  if (isOnPreview) {
    // On preview page: click the home/back button
    // Prefer the 'Back to Home' button if present
    const homeButton = page.locator('button.home-button');
    if (await homeButton.count() > 0) {
      await homeButton.click();
      // Wait for homepage (look for welcome message or main page element)
      await page.waitForSelector('.main-page', { timeout: 10000 });
      return;
    }
    // Or, if in configuration mode, click 'Back to Configuration' first
    const backToConfig = page.locator('a.back-button', { hasText: 'Back to Configuration' });
    if (await backToConfig.count() > 0) {
      await backToConfig.click();
      // Now fall through to config menu handling
    }
  }
  // If on ConfigureImageMenu, look for a quit/cancel/back/home button
  // Try 'Quit Image Creation' button (class 'quit-button')
  const quitButton = page.locator('button.quit-button', { hasText: 'Quit Image Creation' });
  if (await quitButton.count() > 0) {
    await quitButton.click();
    await page.waitForSelector('.main-page', { timeout: 10000 });
    return;
  }
  // Try a link to home
  const homeLink = page.getByRole('link', { name: /Back to Home|Home|Main/i });
  if (await homeLink.count() > 0) {
    await homeLink.click();
    await page.waitForSelector('.main-page', { timeout: 10000 });
    return;
  }
  // Fallback: reload to home
  await page.goto('/');
  await page.waitForSelector('.main-page', { timeout: 10000 });
}

/**
 * Assert that no error message is visible (ErrorDisplay)
 */
export async function assertNoErrorMessage(page: Page) {
  // ErrorDisplay renders .error-container if visible
  await expect(page.locator('.error-container')).toHaveCount(0);
}

/**
 * Assert that an error message is visible and contains a given string in its title or details
 * @param page Playwright page
 * @param text string to search for in error title or details
 */
export async function assertErrorMessageContains(page: Page, text: string) {
  const errorContainer = page.locator('.error-container');
  await expect(errorContainer).toBeVisible();
  const errorText = await errorContainer.innerText();
  expect(errorText.toLowerCase()).toContain(text.toLowerCase());
}

/**
 * Complete image creation and configuration: if in configuration menu, preview and finish; if in preview, just finish.
 */
export async function completeImageCreationAndConfiguration(page: Page) {
  // Check if on ConfigureImageMenu (look for .configure-menu-container)
  const isOnConfigMenu = await page.locator('.configure-menu-container').count() > 0;
  if (isOnConfigMenu) {
    // Click the 'Preview Image' button
    const previewBtn = page.locator('button.finish-button', { hasText: 'Preview Image' });
    await previewBtn.click();
    await page.waitForSelector('.solution-preview-page, .preview-title, .page-title');
    // Now on preview page, fall through to finish
  }
  // On SolutionPreviewPage, click the finish button if present
  // Only visible if image is not configured
  const finishBtn = page.locator('button.save-image-button', { hasText: /Finish and save image/i });
  if (await finishBtn.count() > 0) {
    await finishBtn.click();
    // Wait for homepage (main page)
    await page.waitForSelector('.main-page', { timeout: 10000 });
  }
}

/**
 * Configure names and tags for sets and parameters.
 * @param page Playwright page
 * @param options {
 *   sets?: Array<{ name: string, alias: string, tags?: string[] }>,
 *   params?: Array<{ name: string, alias: string }>
 * }
 */
export async function configureNamesAndTags(page: Page, options: {
  sets?: Array<{ name: string, alias: string, tags?: string[] }>,
  params?: Array<{ name: string, alias: string }>
}) {
  // Navigate to Configure Names & Tags
  await page.locator('.configure-menu-item', { hasText: 'Configure Names & Tags' }).click();
  
  // Configure sets
  if (options.sets) {
    for (const set of options.sets) {
      // Find the set card by original name
      const setCard = page.locator('.item-card', { hasText: set.name });
      
      // Set the alias
      const aliasInput = setCard.locator('input[type="text"]').first();
      await aliasInput.fill(set.alias);
      
      // Set tags if provided
      if (set.tags) {
        const tagInputs = setCard.locator('.tag-inputs-row input[type="text"]');
        const tagCount = await tagInputs.count();
        for (let i = 0; i < Math.min(set.tags.length, tagCount); i++) {
          await tagInputs.nth(i).fill(set.tags[i]);
        }
      }
    }
  }
  
  // Configure params
  if (options.params) {
    for (const param of options.params) {
      // Find the param card by original name
      const paramCard = page.locator('.item-card', { hasText: param.name });
      
      // Set the alias
      const aliasInput = paramCard.locator('input[type="text"]').first();
      await aliasInput.fill(param.alias);
    }
  }
  
  // Continue back to ConfigureImageMenu
  await page.locator('.continue-button').click();
  await page.getByText('Configure Image').waitFor();
}

/**
 * Configure solver settings for an image.
 * @param page Playwright page
 * @param selectedSettings string[] Array of solver setting names to select (e.g., ['Default', 'Optimallity'])
 */
export async function configureSolverSettings(page: Page, selectedSettings: string[]) {
  // Navigate to Configure Solver Settings
  await page.locator('.configure-menu-item', { hasText: 'Configure Solver Settings' }).click();
  
  // Wait for the predefined options to be visible
  await page.locator('.predefined-options').waitFor();
  
  // Get all setting options
  const settingOptions = page.locator('.setting-option');
  const count = await settingOptions.count();
  
  // Loop through all setting options and set them according to selectedSettings
  for (let i = 0; i < count; i++) {
    const settingOption = settingOptions.nth(i);
    const checkbox = settingOption.locator('input[type="checkbox"]');
    
    // Get the label text from the setting header
    const labelText = await settingOption.locator('.setting-header label').textContent();
    
    if (labelText && selectedSettings.includes(labelText.trim())) {
      // Should be checked
      if (!(await checkbox.isChecked())) {
        await checkbox.check();
      }
    } else {
      // Should be unchecked
      if (await checkbox.isChecked()) {
        await checkbox.uncheck();
      }
    }
  }
  
  // Continue back to ConfigureImageMenu
  await page.locator('.continue-button').click();
  await page.getByText('Configure Image').waitFor();
}

/**
 * Logout the current user and return to guest state
 */
export async function logout(page: Page) {
  // Look for logout button - could be in header, user menu, etc.
  const logoutButton = page.getByRole('button', { name: /logout|sign out|log out/i });
  if (await logoutButton.count() > 0) {
    await logoutButton.click();
    // Wait for logout to complete (should see guest welcome message)
    await expect(page.getByText('Welcome, Guest!')).toBeVisible({ timeout: 10000 });
    return;
  }
  
  // Alternative: look for user menu/dropdown
  const userMenu = page.locator('.user-menu, .profile-menu, [data-testid="user-menu"]');
  if (await userMenu.count() > 0) {
    await userMenu.click();
    const logoutOption = page.getByRole('button', { name: /logout|sign out|log out/i });
    if (await logoutOption.count() > 0) {
      await logoutOption.click();
      await expect(page.getByText('Welcome, Guest!')).toBeVisible({ timeout: 10000 });
      return;
    }
  }
  
  // Fallback: navigate to home and check if already logged out
  await page.goto('/');
  const guestWelcome = page.getByText('Welcome, Guest!');
  if (await guestWelcome.count() > 0) {
    return; // Already logged out
  }
  
  // If still logged in, try to find any logout mechanism
  throw new Error('Could not find logout button or mechanism');
}

/**
 * Check if an image is visible in the public images section (search results)
 */
export async function checkImageInPublicImages(page: Page, imageName: string, shouldBeVisible: boolean = true) {
  // The public images are displayed through search functionality
  // First, search for the image name
  const searchInput = page.locator('#search-input');
  await searchInput.fill(imageName);
  await page.locator('.search-button').click();
  
  // Wait for search results to load
  await page.waitForTimeout(2000);
  
  // Look for the image in the search results area
  // The search results are displayed in the same area as the search input
  const searchResultsArea = page.locator('.search-container').locator('..');
  
  if (shouldBeVisible) {
    await expect(searchResultsArea.getByText(imageName)).toBeVisible({ timeout: 10000 });
  } else {
    await expect(searchResultsArea.getByText(imageName)).toHaveCount(0);
  }
}

/**
 * Check if an image is visible in the user's private images section
 */
export async function checkImageInMyImages(page: Page, imageName: string, shouldBeVisible: boolean = true) {
  // Find the my images section
  const myImagesSection = page.locator('.my-images-container:has(h2:text("My Images"))');
  
  if (shouldBeVisible) {
    await expect(myImagesSection.getByText(imageName)).toBeVisible({ timeout: 10000 });
  } else {
    await expect(myImagesSection.getByText(imageName)).toHaveCount(0);
  }
}

/**
 * Configure image visibility (public/private) in the main configuration page
 */
export async function configureImageVisibility(page: Page, makePublic: boolean) {
  // Look for the "Make the image public?" checkbox in the configuration menu
  // The checkbox is inside a label with text "Make the image public?"
  const publicCheckbox = page.locator('label:has-text("Make the image public?") input[type="checkbox"]');
  
  // Wait for the checkbox to be visible
  await expect(publicCheckbox).toBeVisible({ timeout: 10000 });
  
  const currentState = await publicCheckbox.isChecked();
  console.log(`Current checkbox state: ${currentState}, Target state (makePublic): ${makePublic}`);
  
  if (makePublic) {
    if (!currentState) {
      await publicCheckbox.check();
      console.log('Checked the checkbox to make image public');
    } else {
      console.log('Checkbox already checked (image is public)');
    }
  } else {
    if (currentState) {
      await publicCheckbox.uncheck();
      console.log('Unchecked the checkbox to make image private');
    } else {
      console.log('Checkbox already unchecked (image is private)');
    }
  }
}

/**
 * Add an entry to a set in the preview page
 * @param page Playwright page
 * @param setName string The name of the set (e.g., "Available People")
 * @param entryValue string The value to add to the set (e.g., "Shlomi")
 * @param tagName string The tag name for the column (e.g., "Person Name")
 */
export async function addEntryToSetInPreview(page: Page, setName: string, entryValue: string, tagName: string) {
  // First, make sure we're on the Domain tab in preview
  // const domainTab = page.getByRole('button', { name: /domain/i });
  // await expect(domainTab).toBeVisible({ timeout: 10000 });
  // await domainTab.click();
  
  // Wait for the tab content to be visible (only appears after tab is clicked)
  await page.waitForSelector('.tab-content', { timeout: 10000 });
  
  // Wait a bit more for the content to load
  await page.waitForTimeout(2000);
  
  // Debug: Log all available set names
  const allSetNames = page.locator('.set-input h3.set-name');
  const setCount = await allSetNames.count();
  console.log(`Found ${setCount} sets in the Domain tab:`);
  for (let i = 0; i < setCount; i++) {
    const setText = await allSetNames.nth(i).textContent();
    console.log(`  Set ${i + 1}: "${setText}"`);
  }
  console.log(`Looking for set: "${setName}"`);
  
  // Find the set container by its title (set-name class contains the alias/name)
  // Use a more flexible selector that looks for the set name in the h3 element
  const setContainer = page.locator('.set-input h3.set-name', { hasText: setName }).locator('..');
  await expect(setContainer).toBeVisible({ timeout: 10000 });
  
  // Look for the "Add Entry" button within this set container
  const addButton = setContainer.locator('button.add-set-entry-button');
  await expect(addButton).toBeVisible({ timeout: 10000 });
  await addButton.click();
  
  // Wait for the new row to appear
  await page.waitForTimeout(1000);
  
  // Find the new entry in the entries-container
  const entriesContainer = setContainer.locator('.entries-container');
  const newEntry = entriesContainer.locator('.set-entry').last();
  
  // Find the input field in the new entry
  // The input should correspond to the tag name (column header)
  const tagRow = entriesContainer.locator('.tag-row');
  const tagLabels = tagRow.locator('.tag-label');
  
  // Find the index of the tag we want to fill
  let targetIndex = 0; // Default to first input
  const tagCount = await tagLabels.count();
  for (let i = 0; i < tagCount; i++) {
    const tagText = await tagLabels.nth(i).textContent();
    if (tagText && tagText.includes(tagName)) {
      targetIndex = i;
      break;
    }
  }
  
  // Fill the input field at the target index
  const inputField = newEntry.locator('input[type="text"]').nth(targetIndex);
  await inputField.fill(entryValue);
} 