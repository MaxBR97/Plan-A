import { Page, expect } from '@playwright/test';

export async function login(page: Page, username: string, password: string) {
  await page.goto('/');
  // Wait for either the app's login button or the Keycloak login form
  await page.getByRole('button', { name: /^Login$/ }).click();
  
  const isKeycloak = await page.locator('form#kc-form-login').count() > 0;

    // On Keycloak login page
    await page.fill('input#username', username);
    await page.fill('input#password', password);
    await page.click('input[type="submit"][value="Sign In"]');

  // Wait for successful login
  await expect(page.getByText(/Welcome,\s*[^!]+!/)).toBeVisible();
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
  // Find the image card
  const imageCard = page.getByRole('heading', { name: imageName }).locator('..');
  
  // Click delete button
  await imageCard.getByRole('button', { name: 'Delete' }).click();
  
  // Confirm deletion
  await page.getByRole('button', { name: 'Confirm' }).click();
  
  // Wait for success
  await expect(page.getByText('Image deleted successfully')).toBeVisible();
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