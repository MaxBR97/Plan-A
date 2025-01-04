import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import WorkAssignmentPage from './pages/WorkAssignmentPage';
import UploadZPLPage from './pages/UploadZPLPage';
import ConfigureVariablesPage from './pages/ConfigureVariablesPage';
import ConfigureConstraintsPage from './pages/ConfigureConstraintsPage';
import ConfigurePreferencesPage from './pages/ConfigurePreferencesPage';

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
            </Routes>
        </Router>
    );
}

export default App;
