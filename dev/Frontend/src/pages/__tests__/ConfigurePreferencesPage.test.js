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

    test('trivial test', async () => {
    })
    
    // test('ConfigurePreferencesPage - create and persist preference module', async () => {
    //     const { rerender } = await performUpload();

    //     // Navigate to preferences page
    //     rerender(
    //         <TestWrapper>
    //             <ConfigurePreferencesPage />
    //         </TestWrapper>
    //     );

    //     // Wait for page to load
    //     await waitFor(() => {
    //         expect(screen.getByText(/configure preference modules/i)).toBeInTheDocument();
    //     });

    //     // Create a new module
    //     fireEvent.change(screen.getByPlaceholderText(/enter module name/i), {
    //         target: { value: 'New Preference' }
    //     });

    //     // Find and select preference1 from the available preferences
    //     const preference = screen.getByText('preference1');
    //     fireEvent.click(preference);

    //     // Wait for cost parameter dropdown to appear
    //     await waitFor(() => {
    //         const costSelect = screen.getByRole('combobox');
    //         expect(costSelect).toBeInTheDocument();
    //     });

    //     // Open the combobox and verify param1 is in the options
    //     const costSelect = screen.getByRole('combobox');
    //     fireEvent.click(costSelect);

    //     // Verify param1 is available as an option
    //     await waitFor(() => {
    //         const param1Option = screen.getByRole('option', { name: 'param1' });
    //         expect(param1Option).toBeInTheDocument();
    //     });

    //     // Select param1 as cost parameter
    //     fireEvent.change(costSelect, { target: { value: 'param1' } });

    //     // Verify param1 is selected
    //     await waitFor(() => {
    //         expect(costSelect).toHaveValue('param1');
    //     });

    //     // Save module
    //     const createButton = screen.getByText(/create module/i);
    //     expect(createButton).not.toBeDisabled();
    //     fireEvent.click(createButton);

    //     // Verify module was created and appears in the list
    //     await waitFor(() => {
    //         const moduleItem = screen.getByText('New Preference');
    //         expect(moduleItem).toBeInTheDocument();
            
    //         // Verify the module details are correct
    //         const moduleDetails = moduleItem.closest('.module-item');
    //         expect(moduleDetails).toHaveTextContent('preference1');
    //         expect(moduleDetails).toHaveTextContent('param1');
    //     });

    //     // Navigate away
    //     rerender(
    //         <TestWrapper>
    //             <ConfigureImageMenu />
    //         </TestWrapper>
    //     );

    //     // Navigate back
    //     rerender(
    //         <TestWrapper>
    //             <ConfigurePreferencesPage />
    //         </TestWrapper>
    //     );
        
    //     // Verify module still exists after navigation
    //     await waitFor(() => {
    //         const moduleItem = screen.getByText('New Preference');
    //         expect(moduleItem).toBeInTheDocument();
            
    //         // Verify the module details are still correct
    //         const moduleDetails = moduleItem.closest('.module-item');
    //         expect(moduleDetails).toHaveTextContent('preference1');
    //         expect(moduleDetails).toHaveTextContent('param1');
    //     });
    // });

}); 