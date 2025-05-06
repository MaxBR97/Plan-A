import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';

import MainPage from './pages/MainPage';
import WorkAssignmentPage from './pages/WorkAssignmentPage';
import UploadZPLPage from './pages/UploadZPLPage';
import ConfigureVariablesPage from './pages/ConfigureVariablesPage';
import ConfigureConstraintsPage from './pages/ConfigureConstraintsPage';
import ConfigurePreferencesPage from './pages/ConfigurePreferencesPage';
import SolutionPreviewPage from './pages/SolutionPreviewPage';
import SolutionResultsPage from "./pages/SolutionResultsPage";
import ConfigureSetsAndParamsPage from "./pages/ConfigureSetsAndParamsPage";

function App() {
    return (
        <Router>
            <DndProvider backend={HTML5Backend}>
                <Routes>
                    <Route path="/" element={<MainPage />} />
                    <Route path="/work-assignment" element={<WorkAssignmentPage />} />
                    <Route path="/upload-zpl" element={<UploadZPLPage />} />
                    <Route path="/configure-variables" element={<ConfigureVariablesPage />} />
                    <Route path="/configure-constraints" element={<ConfigureConstraintsPage />} />
                    <Route path="/configure-preferences" element={<ConfigurePreferencesPage />} />
                    <Route path="/solution-preview" element={<SolutionPreviewPage />} />
                    <Route path="/configure-sets-params" element={<ConfigureSetsAndParamsPage />} />
                    {/* <Route path="/login" element={<LoginPage/>} /> */}
                    {/* <Route path="/solution-results" element={<SolutionResultsPage />} /> */}
                </Routes>
            </DndProvider>
        </Router>
    );
}

export default App;
