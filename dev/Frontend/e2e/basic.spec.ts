import { test, expect, Locator } from '@playwright/test';
import { login, deleteTestImage, CreateNewImageAndGoToConfigureImage, configureDomain, assertNoErrorMessage, completeImageCreationAndConfiguration, configureConstraints, configurePreferences, previewImage, goBackToConfiguration } from './test-utils';

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
}); 