import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import UploadZPLPage from "../UploadZPLPage";
import { BrowserRouter } from "react-router-dom";
import axios from "axios";
import { useZPL } from "../../context/ZPLContext";

jest.mock("axios");

const mockContext = {
  user: { username: "testuser" },
  image: { 
    imageName: "", 
    imageDescription: "",
    imageId: null,
  },
  model: {},
  updateImageField: jest.fn(),
  updateModel: jest.fn(),
  resetImage: jest.fn(),
  resetModel: jest.fn(),
};

// Mock the useZPL hook
jest.mock("../../context/ZPLContext", () => ({
  useZPL: () => mockContext
}));

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

describe("UploadZPLPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockContext.image.imageName = "";
  });

  const renderPage = () =>
    render(
      <BrowserRouter>
        <UploadZPLPage />
      </BrowserRouter>
    );

  test("renders all form elements", () => {
    renderPage();

    // Check for the title
    expect(screen.getByText("Upload ZPL File")).toBeInTheDocument();
    
    // Check for labels
    expect(screen.getByText("Image Name:")).toBeInTheDocument();
    expect(screen.getByText("ZIMPL code:")).toBeInTheDocument();
    expect(screen.getByText("Image Description:")).toBeInTheDocument();
    
    // Check for input fields
    expect(screen.getByPlaceholderText("Enter image name...")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Enter image description...")).toBeInTheDocument();
    
    // Check for textareas (ZIMPL code)
    const textareas = screen.getAllByRole("textbox");
    expect(textareas.length).toBeGreaterThanOrEqual(3); // Image name input, ZIMPL code textarea, and description textarea
    
    // Check for buttons
    expect(screen.getByRole("button", { name: "Upload" })).toBeInTheDocument();
    expect(screen.getByText("Back")).toBeInTheDocument();
  });

  test("shows error message if image name is empty", async () => {
    renderPage();

    const uploadButton = screen.getByRole("button", { name: /upload/i });
    fireEvent.click(uploadButton);

    await waitFor(() => {
      expect(screen.getByText(/error: image name cannot be empty/i)).toBeInTheDocument();
    });
  });

  test("submits data and navigates on success", async () => {
    mockContext.image.imageName = "My Image";
    const mockResponse = {
      data: {
        imageId: "123",
        model: { dummy: true },
      }
    };

    axios.post.mockResolvedValue(mockResponse);

    renderPage();

    const uploadButton = screen.getByRole("button", { name: /upload/i });
    fireEvent.click(uploadButton);

    await waitFor(() => {
      // Verify the POST request
      expect(axios.post).toHaveBeenCalledWith("/images", expect.objectContaining({
        imageName: "My Image",
        owner: "testuser",
        isPrivate: true,
      }), expect.any(Object));

      // Verify state updates
      expect(mockContext.resetImage).toHaveBeenCalled();
      expect(mockContext.resetModel).toHaveBeenCalled();
      expect(mockContext.updateImageField).toHaveBeenCalledWith("imageId", "123");
      expect(mockContext.updateImageField).toHaveBeenCalledWith("imageName", "My Image");
      expect(mockContext.updateModel).toHaveBeenCalledWith({ dummy: true });
      expect(mockNavigate).toHaveBeenCalledWith("/configuration-menu");
      expect(screen.getByText(/file uploaded successfully/i)).toBeInTheDocument();
    });
  });

  test("handles axios error gracefully", async () => {
    mockContext.image.imageName = "My Image";

    axios.post.mockRejectedValue({
      response: {
        status: 400,
        data: { msg: "Invalid data" },
      },
    });

    renderPage();
    fireEvent.click(screen.getByRole("button", { name: /upload/i }));

    await waitFor(() => {
      expect(screen.getByText(/error: 400 - invalid data/i)).toBeInTheDocument();
    });
  });

  test("handles network error gracefully", async () => {
    mockContext.image.imageName = "My Image";

    axios.post.mockRejectedValue({
      request: {}, // This simulates a request that was made but got no response
    });

    renderPage();
    fireEvent.click(screen.getByRole("button", { name: /upload/i }));

    await waitFor(() => {
      expect(screen.getByText(/error: no response from server/i)).toBeInTheDocument();
    });
  });

  test("updates ZIMPL code when textarea changes", () => {
    renderPage();
    
    const codeTextarea = screen.getByLabelText("ZIMPL code:");
    fireEvent.change(codeTextarea, { target: { value: "new code" } });
    
    expect(codeTextarea.value).toBe("new code");
  });
});
