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

    test('ConfigureSolverOptionsPage - create and persist solver settings', async () => {
        const { rerender } = await performUpload();

        // Navigate to solver options page
        rerender(
            <TestWrapper>
                <ConfigureSolverOptionsPage />
            </TestWrapper>
        );

        // Wait for page to load
        await waitFor(() => {
            expect(screen.getByText(/configure solver settings/i)).toBeInTheDocument();
        });

        // Add a new script
        fireEvent.click(screen.getByText(/add new script/i));

        // Fill in script details
        const scriptInputs = screen.getAllByPlaceholderText(/script name/i);
        fireEvent.change(scriptInputs[scriptInputs.length - 1], {
            target: { value: 'New Script' }
        });

        const contentInputs = screen.getAllByPlaceholderText(/enter script content/i);
        fireEvent.change(contentInputs[contentInputs.length - 1], {
            target: { value: 'test content' }
        });

        // Navigate away
        rerender(
            <TestWrapper>
                <ConfigureImageMenu />
            </TestWrapper>
        );

        // Navigate back
        rerender(
            <TestWrapper>
                <ConfigureSolverOptionsPage />
            </TestWrapper>
        );

        // Verify settings persist
        await waitFor(() => {
            expect(screen.getByDisplayValue('New Script')).toBeInTheDocument();
            expect(screen.getByDisplayValue('test content')).toBeInTheDocument();
        });
    });

}); 