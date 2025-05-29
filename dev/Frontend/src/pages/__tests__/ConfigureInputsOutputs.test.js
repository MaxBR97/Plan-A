import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ZPLProvider } from '../../context/ZPLContext';
import ConfigureImageMenu from '../ConfigureImageMenu';
import ConfigureInputsOutputs from '../ConfigureInputsOutputs';
import ConfigureConstraintsPage from '../ConfigureConstraintsPage';
import ConfigurePreferencesPage from '../ConfigurePreferencesPage';
import ConfigureSolverOptionsPage from '../ConfigureSolverOptionsPage';
import ConfigureSetsAndParamsPage from '../ConfigureSetsAndParamsPage';
import UploadZPLPage from '../UploadZPLPage';

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockNavigate,
}));

// Mock axios for upload simulation
jest.mock('axios');

// Sample model data that would come from backend after upload
const mockModelResponse = {
    constraints: new Set([
        { 
            identifier: 'constraint1',
            dep: {
                setDependencies: ['set1'],
                paramDependencies: ['param1']
            }
        },
        { 
            identifier: 'constraint2',
            dep: {
                setDependencies: ['set2'],
                paramDependencies: ['param2']
            }
        }
    ]),
    preferences: new Set([
        { 
            identifier: 'preference1',
            dep: {
                setDependencies: ['set1'],
                paramDependencies: ['param1', 'param2']  // param1 is a dependency
            }
        },
        { 
            identifier: 'preference2',
            dep: {
                setDependencies: ['set2'],
                paramDependencies: ['param2']
            }
        }
    ]),
    variables: new Set([
        { 
            identifier: 'var1',
            tags: ['type1', 'type2'],
            type: ['type1', 'type2'],
            dep: {
                setDependencies: ['set1'],
                paramDependencies: ['param1']
            }
        },
        { 
            identifier: 'var2',
            tags: ['type3'],
            type: ['type3'],
            dep: {
                setDependencies: ['set2'],
                paramDependencies: ['param2']
            }
        }
    ]),
    setTypes: {
        'set1': ['type1', 'type2'],
        'set2': ['type3']
    },
    paramTypes: {
        'param1': 'INT',
        'param2': 'FLOAT'
    },
    varTypes: {
        'var1': ['type1', 'type2'],
        'var2': ['type3']
    }
};

// Test wrapper
const TestWrapper = ({ children }) => {
    return (
        <BrowserRouter>
            <ZPLProvider>
                {children}
            </ZPLProvider>
        </BrowserRouter>
    );
};

describe('Configuration Flow Tests', () => {
    beforeEach(() => {
        mockNavigate.mockClear();
        localStorage.clear();
    });

    // Helper function to perform initial upload
    const performUpload = async () => {
        // Mock successful upload response
        const mockUploadResponse = {
            data: {
                imageId: 'test-image',
                model: mockModelResponse
            }
        };
        
        require('axios').post.mockResolvedValue(mockUploadResponse);

        const { rerender } = render(
            <TestWrapper>
                <UploadZPLPage />
            </TestWrapper>
        );

        // Fill in required fields
        fireEvent.change(screen.getByPlaceholderText(/enter image name/i), {
            target: { value: 'Test Image' }
        });

        // Click upload
        fireEvent.click(screen.getByText('Upload'));

        // Wait for navigation
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/configuration-menu');
        });

        return { rerender };
    };

    test('ConfigureInputsOutputs - initial state and configuration persistence', async () => {
        const { rerender } = await performUpload();

        // Navigate to inputs/outputs page
        rerender(
            <TestWrapper>
                <ConfigureInputsOutputs />
            </TestWrapper>
        );

        // Wait for page to load and sections to be visible
        await waitFor(() => {
            expect(screen.getByText('Configure Variables')).toBeInTheDocument();
            expect(screen.getByText('Outputs')).toBeInTheDocument();
            expect(screen.getByText('Inputs')).toBeInTheDocument();
            expect(screen.getByText('Sets')).toBeInTheDocument();
            expect(screen.getByText('Parameters')).toBeInTheDocument();
        });

        // Get sections
        const outputsSection = screen.getByText('Outputs').closest('.available-variables');
        const inputsSection = screen.getByText('Inputs').closest('.involved-section');

        // Get all checkboxes and verify they're checked initially
        const allCheckboxes = screen.getAllByRole('checkbox');
        allCheckboxes.forEach(checkbox => {
            expect(checkbox).toBeChecked();
        });

        // Find specific checkboxes in their respective sections
        const var1Checkbox = within(outputsSection).getByText('var1').closest('label').querySelector('input');
        const var2Checkbox = within(outputsSection).getByText('var2').closest('label').querySelector('input');
        const set1Checkbox = within(inputsSection).getByText('set1').closest('label').querySelector('input');
        const set2Checkbox = within(inputsSection).getByText('set2').closest('label').querySelector('input');
        const param1Checkbox = within(inputsSection).getByText('param1').closest('label').querySelector('input');
        const param2Checkbox = within(inputsSection).getByText('param2').closest('label').querySelector('input');

        // Uncheck everything except our chosen ones
        fireEvent.click(var2Checkbox);
        fireEvent.click(set1Checkbox);
        fireEvent.click(param1Checkbox);

        // Verify our selections
        expect(var1Checkbox).toBeChecked();
        expect(var2Checkbox).not.toBeChecked();
        expect(set2Checkbox).toBeChecked();
        expect(set1Checkbox).not.toBeChecked();
        expect(param2Checkbox).toBeChecked();
        expect(param1Checkbox).not.toBeChecked();

        // Wait for tag configuration section to appear
        await waitFor(() => {
            expect(screen.getByText("Tag Variables' Output Tuple")).toBeInTheDocument();
        });

        // Find var1's configuration section
        const tagsContainer = screen.getByText("Tag Variables' Output Tuple").closest('.tags-container');
        const var1Config = within(tagsContainer).getByText('var1').closest('.variable-config-item');

        // Set custom tags for var1
        const tagInputs = within(var1Config).getAllByRole('textbox');
        tagInputs.forEach((input, index) => {
            fireEvent.change(input, { target: { value: `CustomTag_${index + 1}` } });
        });

        // Find and set bound set for var1
        const boundSetSelect = within(var1Config).getByRole('combobox');
        fireEvent.change(boundSetSelect, { target: { value: 'set1' } });

        // Click continue
        fireEvent.click(screen.getByRole('link', { name: /continue/i }));

        // Navigate away
        rerender(
            <TestWrapper>
                <ConfigureImageMenu />
            </TestWrapper>
        );

        // Navigate back
        rerender(
            <TestWrapper>
                <ConfigureInputsOutputs />
            </TestWrapper>
        );

        // Wait for page to reload
        await waitFor(() => {
            expect(screen.getByText('Configure Variables')).toBeInTheDocument();
        });

        // Get sections again after navigation
        const outputsSectionAfter = screen.getByText('Outputs').closest('.available-variables');
        const inputsSectionAfter = screen.getByText('Inputs').closest('.involved-section');

        // Verify checkbox states persisted
        await waitFor(() => {
            // Verify outputs
            expect(within(outputsSectionAfter).getByText('var1').closest('label').querySelector('input')).toBeChecked();
            expect(within(outputsSectionAfter).getByText('var2').closest('label').querySelector('input')).not.toBeChecked();
            
            // Verify inputs
            expect(within(inputsSectionAfter).getByText('set2').closest('label').querySelector('input')).toBeChecked();
            expect(within(inputsSectionAfter).getByText('set1').closest('label').querySelector('input')).not.toBeChecked();
            expect(within(inputsSectionAfter).getByText('param2').closest('label').querySelector('input')).toBeChecked();
            expect(within(inputsSectionAfter).getByText('param1').closest('label').querySelector('input')).not.toBeChecked();
        });

        // Verify var1 configuration persisted
        const tagsContainerAfter = screen.getByText("Tag Variables' Output Tuple").closest('.tags-container');
        const var1ConfigAfter = within(tagsContainerAfter).getByText('var1').closest('.variable-config-item');

        // Verify tags
        const tagInputsAfter = within(var1ConfigAfter).getAllByRole('textbox');
        tagInputsAfter.forEach((input, index) => {
            expect(input).toHaveValue(`CustomTag_${index + 1}`);
        });

        // Verify bound set
        const boundSetSelectAfter = within(var1ConfigAfter).getByRole('combobox');
        expect(boundSetSelectAfter).toHaveValue('set1');
    });
}); 