import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { useZPL, ZPLProvider } from '../ZPLContext';

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
  const renderWithProvider = () => {
    return render(
      <ZPLProvider>
        <TestComponent />
      </ZPLProvider>
    );
  };

  it('provides initial state', () => {
    renderWithProvider();
    
    expect(screen.getByTestId('username')).toHaveTextContent('guest');
    expect(screen.getByTestId('imageName')).toHaveTextContent('My Image');
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