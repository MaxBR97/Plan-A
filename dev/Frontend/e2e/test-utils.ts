import { Page, expect } from '@playwright/test';

export async function login(page: Page, username: string, password: string) {
  await page.goto('/');
  await page.getByRole('button', { name: 'Login' }).click();
  
  // Wait for login form
  await page.waitForSelector('input[name="username"]');
  
  // Fill in credentials
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', password);
  
  // Submit form
  await page.click('button[type="submit"]');
  
  // Wait for successful login
  await expect(page.getByText(`Welcome, ${username}!`)).toBeVisible();
}

export async function createTestImage(page: Page, name: string, description: string) {
  await page.getByRole('link', { name: 'Create new image' }).click();
  
  // Fill in image details
  await page.fill('input[name="imageName"]', name);
  await page.fill('textarea[name="imageDescription"]', description);
  
  // Upload a test file
  await page.setInputFiles('input[type="file"]', 'e2e/fixtures/test.zpl');
  
  // Submit form
  await page.click('button[type="submit"]');
  
  // Wait for success
  await expect(page.getByText('Image created successfully')).toBeVisible();
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