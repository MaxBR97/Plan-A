import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import UploadZPLPage from "../UploadZPLPage";
import { BrowserRouter } from "react-router-dom";
import axios from "axios";
import { useZPL } from "../context/ZPLContext";


const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

describe("UploadZPLPage", () => {
  const mockContext = {
    user: { username: "testuser" },
    image: { imageName: "", imageDescription: "" },
    model: {},
    updateImageField: jest.fn(),
    updateModel: jest.fn(),
    resetImage: jest.fn(),
    resetModel: jest.fn(),
  };

  beforeEach(() => {
    useZPL.mockReturnValue(mockContext);
  });

  const renderPage = () =>
    render(
      <BrowserRouter>
        <UploadZPLPage />
      </BrowserRouter>
    );

  test("renders all form elements", () => {
    renderPage();

    expect(screen.getByLabelText(/image name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/zimpl code/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/image description/i)).toBeInTheDocument();
    expect(screen.getByText(/upload/i)).toBeInTheDocument();
    expect(screen.getByText(/back/i)).toBeInTheDocument();
  });

  test("shows error message if image name is empty", async () => {
    renderPage();

    const uploadButton = screen.getByRole("button", { name: /upload/i });
    fireEvent.click(uploadButton);

    await waitFor(() => {
      expect(screen.getByText(/image name cannot be empty/i)).toBeInTheDocument();
    });
  });

  test("submits data and navigates on success", async () => {
    mockContext.image.imageName = "My Image";

    axios.post.mockResolvedValue({
      data: {
        imageId: "123",
        model: { dummy: true },
      },
    });

    renderPage();

    const uploadButton = screen.getByRole("button", { name: /upload/i });
    fireEvent.click(uploadButton);

    await waitFor(() => {
      expect(mockContext.updateImageField).toHaveBeenCalledWith("imageId", "123");
      expect(mockContext.updateModel).toHaveBeenCalledWith({ dummy: true });
      expect(mockNavigate).toHaveBeenCalledWith("/configuration-menu");
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
});
