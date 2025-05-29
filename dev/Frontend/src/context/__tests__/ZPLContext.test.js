import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { useZPL, ZPLProvider } from '../ZPLContext';


// Sample test data
const mockModelData = {
    constraints: new Set(['constraint1', 'constraint2']),
    preferences: new Set(['preference1', 'preference2']),
    variables: new Set(['var1', 'var2']),
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

const mockImageData = {
    imageId: 'test-image',
    imageName: 'Test Image',
    imageDescription: 'Test Description',
    owner: 'testuser',
    isPrivate: true,
    solverSettings: { 'default': 'test setting' },
    constraintModules: [{
        moduleName: 'Module 1',
        description: 'Test Module',
        constraints: ['constraint1'],
        inputSets: [{ name: 'set1', tags: ['tag1'], type: ['type1'] }],
        inputParams: [{ name: 'param1', tag: 'tag1', type: 'INT' }]
    }],
    preferenceModules: [{
        moduleName: 'Pref Module 1',
        description: 'Test Pref Module',
        preferences: ['preference1'],
        costParams: [{ name: 'param1', tag: 'cost1', type: 'INT', alias: 'param1' }],
        inputSets: [{ name: 'set2', tags: ['tag3'], type: ['type3'] }],
        inputParams: [{ name: 'param2', tag: 'tag2', type: 'FLOAT' }]
    }],
    variablesModule: {
        variablesOfInterest: [{
            identifier: 'var1',
            tags: ['tag1', 'tag2'],
            boundSet: 'set1'
        }],
        inputSets: [{
            name: 'set1',
            tags: ['tag1', 'tag2'],
            type: ['type1', 'type2']
        }],
        inputParams: [{
            name: 'param1',
            tag: 'tag1',
            type: 'INT'
        }]
    }
};

// Test component that uses the context
const TestComponent = () => {
  const {
    user,
    image,
    model,
    solutionResponse,
    updateUserField,
    updateImage,
    updateImageField,
    updateModel,
    resetImage,
    resetModel,
    resetSolutionResponse
  } = useZPL();

  return (
    <div>
      <div data-testid="username">{user.username}</div>
      <div data-testid="imageName">{image.imageName}</div>
      <div data-testid="imageId">{image.imageId}</div>
      <div data-testid="constraintModules">{JSON.stringify(image.constraintModules)}</div>
      <div data-testid="preferenceModules">{JSON.stringify(image.preferenceModules)}</div>
      <div data-testid="modelConstraints">{Array.from(model.constraints).join(',')}</div>
      <div data-testid="modelPreferences">{Array.from(model.preferences).join(',')}</div>
      <button onClick={() => updateUserField('username', 'testUser')}>Update User</button>
      <button onClick={() => updateImageField('imageName', 'testImage')}>Update Image Name</button>
      <button onClick={resetImage}>Reset Image</button>
      <button onClick={resetModel}>Reset Model</button>
      <button onClick={resetSolutionResponse}>Reset Solution</button>
      <button onClick={() => updateModel({
        constraints: ['newConstraint'],
        preferences: ['newPreference'],
        variables: ['newVariable'],
        setTypes: { type1: 'set1' },
        paramTypes: { param1: 'type1' },
        varTypes: { var1: 'type1' }
      })}>Update Model</button>
    </div>
  );
};

describe('ZPLContext', () => {
  const renderWithProvider = (initialState = {}) => {
    return render(
      <ZPLProvider initialState={initialState}>
        <TestComponent />
      </ZPLProvider>
    );
  };

  it('provides initial state', () => {
    renderWithProvider();
    
    expect(screen.getByTestId('username')).toHaveTextContent('guest');
    expect(screen.getByTestId('imageName')).toHaveTextContent('My Image');
  });

  it('properly initializes with custom initialState', () => {
    // Clear localStorage first
    localStorage.clear();
    
    renderWithProvider({
      model: mockModelData,
      image: mockImageData
    });
    
    // Verify image state is initialized
    expect(screen.getByTestId('imageName')).toHaveTextContent('Test Image');
    expect(screen.getByTestId('imageId')).toHaveTextContent('test-image');
    
    // Verify constraint modules are initialized
    const constraintModules = JSON.parse(screen.getByTestId('constraintModules').textContent);
    expect(constraintModules).toHaveLength(1);
    expect(constraintModules[0].moduleName).toBe('Module 1');
    
    // Verify preference modules are initialized
    const preferenceModules = JSON.parse(screen.getByTestId('preferenceModules').textContent);
    expect(preferenceModules).toHaveLength(1);
    expect(preferenceModules[0].moduleName).toBe('Pref Module 1');
    
    // Verify model state is initialized with Sets properly converted
    const modelConstraints = screen.getByTestId('modelConstraints').textContent.split(',');
    expect(modelConstraints).toContain('constraint1');
    expect(modelConstraints).toContain('constraint2');
    
    const modelPreferences = screen.getByTestId('modelPreferences').textContent.split(',');
    expect(modelPreferences).toContain('preference1');
    expect(modelPreferences).toContain('preference2');
  });

  it('preserves state after updates with custom initialState', () => {
    // Clear localStorage first
    localStorage.clear();
    
    renderWithProvider({
      model: mockModelData,
      image: mockImageData
    });
    
    // Verify initial state
    expect(screen.getByTestId('imageName')).toHaveTextContent('Test Image');
    
    // Update image name
    act(() => {
      screen.getByText('Update Image Name').click();
    });
    
    // Verify update worked
    expect(screen.getByTestId('imageName')).toHaveTextContent('testImage');
    
    // Verify other state remains unchanged
    const constraintModules = JSON.parse(screen.getByTestId('constraintModules').textContent);
    expect(constraintModules[0].moduleName).toBe('Module 1');
    
    // Verify model state is still intact
    const modelConstraints = screen.getByTestId('modelConstraints').textContent.split(',');
    expect(modelConstraints).toContain('constraint1');
    expect(modelConstraints).toContain('constraint2');
  });

  it('updates user field', () => {
    renderWithProvider();
    
    act(() => {
      screen.getByText('Update User').click();
    });
    
    expect(screen.getByTestId('username')).toHaveTextContent('testUser');
  });

  it('updates image field', () => {
    renderWithProvider();
    
    act(() => {
      screen.getByText('Update Image Name').click();
    });
    
    expect(screen.getByTestId('imageName')).toHaveTextContent('testImage');
  });

  it('resets image to initial state', () => {
    renderWithProvider();
    
    // First update the image
    act(() => {
      screen.getByText('Update Image Name').click();
    });
    expect(screen.getByTestId('imageName')).toHaveTextContent('testImage');
    
    // Then reset it
    act(() => {
      screen.getByText('Reset Image').click();
    });
    expect(screen.getByTestId('imageName')).toHaveTextContent('My Image');
  });

  it('updates and resets model state', () => {
    const { container } = renderWithProvider();
    
    // Update model
    act(() => {
      screen.getByText('Update Model').click();
    });
    
    // Reset model
    act(() => {
      screen.getByText('Reset Model').click();
    });
  });

  it('throws error when used outside provider', () => {
    // Suppress console.error for this test
    const consoleSpy = jest.spyOn(console, 'error');
    consoleSpy.mockImplementation(() => {});

    expect(() => {
      render(<TestComponent />);
    }).toThrow('useZPL must be used within a ZPLProvider');

    consoleSpy.mockRestore();
  });
}); 