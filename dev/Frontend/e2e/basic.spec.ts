import { test, expect } from '@playwright/test';
import { login, createTestImage, deleteTestImage } from './test-utils';

test.describe('Basic functionality', () => {
  test('should display welcome message for guest users', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('Welcome, Guest!')).toBeVisible();
  });

  test('should show login and signup buttons for guests', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Sign Up' })).toBeVisible();
  });

  test('should not show create image button for guests', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('link', { name: 'Create new image' })).not.toBeVisible();
  });
});

test.describe('Image management', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, 'testuser', 'testpass');
  });

  test('should create a new image', async ({ page }) => {
    const imageName = `Test Image ${Date.now()}`;
    const imageDescription = 'This is a test image created by E2E tests';
    
    await createTestImage(page, imageName, imageDescription);
    
    // Verify image appears in the list
    await expect(page.getByText(imageName)).toBeVisible();
    await expect(page.getByText(imageDescription)).toBeVisible();
  });

  test('should delete an image', async ({ page }) => {
    const imageName = `Test Image ${Date.now()}`;
    const imageDescription = 'This is a test image to be deleted';
    
    // Create an image first
    await createTestImage(page, imageName, imageDescription);
    
    // Delete the image
    await deleteTestImage(page, imageName);
    
    // Verify image is removed
    await expect(page.getByText(imageName)).not.toBeVisible();
  });
}); 