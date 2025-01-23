import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import WorkAssignmentPage from './pages/WorkAssignmentPage';
import UploadZPLPage from './pages/UploadZPLPage';
import ConfigureVariablesPage from './pages/ConfigureVariablesPage';
import ConfigureConstraintsPage from './pages/ConfigureConstraintsPage';
import ConfigurePreferencesPage from './pages/ConfigurePreferencesPage';
import SolutionPreviewPage from './pages/SolutionPreviewPage';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/work-assignment" element={<WorkAssignmentPage />} />
                <Route path="/upload-zpl" element={<UploadZPLPage />} />
                <Route path="/configure-variables" element={<ConfigureVariablesPage />} />
                <Route path="/configure-constraints" element={<ConfigureConstraintsPage />} />
                <Route path="/configure-preferences" element={<ConfigurePreferencesPage />} />
                <Route path="/solution-preview" element={<SolutionPreviewPage />} />
            </Routes>
        </Router>
    );
}

export default App;
