import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import './App.css';

import MainPage from './pages/MainPage';
import WorkAssignmentPage from './pages/WorkAssignmentPage';
import UploadZPLPage from './pages/UploadZPLPage';
import ConfigureConstraintsPage from './pages/ConfigureConstraintsPage';
import ConfigurePreferencesPage from './pages/ConfigurePreferencesPage';
import SolutionPreviewPage from './pages/SolutionPreviewPage';
import SolutionResultsPage from "./pages/SolutionResultsPage";
import ConfigureSetsAndParamsPage from "./pages/ConfigureSetsAndParamsPage";
import ConfigureSolverOptionsPage from "./pages/ConfigureSolverOptionsPage";
import ConfigureImageMenu from "./pages/ConfigureImageMenu";
import ConfigureInputsOutputs from "./pages/ConfigureInputsOutputs";

function App() {
    return (
        <div className="App">
            <Router>
                <DndProvider backend={HTML5Backend}>
                    <div 
                        className="scaled-content"
                    >
                        <Routes >
                            <Route path="/" element={<MainPage />} />
                            <Route path="/work-assignment" element={<WorkAssignmentPage />} />
                            <Route path="/upload-zpl" element={<UploadZPLPage />} />
                            <Route path="/configuration-menu" element={<ConfigureImageMenu />} />
                            <Route path="/configure-input-outputs" element={<ConfigureInputsOutputs />} />
                            <Route path="/configure-constraints" element={<ConfigureConstraintsPage />} />
                            <Route path="/configure-preferences" element={<ConfigurePreferencesPage />} />
                            <Route path="/solution-preview" element={<SolutionPreviewPage isDesktop={true}/>} />
                            <Route path="/configure-sets-params" element={<ConfigureSetsAndParamsPage />} />
                            <Route path="/configure-solver-options" element={<ConfigureSolverOptionsPage />} />
                            {/* <Route path="/login" element={<LoginPage/>} /> */}
                            {/* <Route path="/solution-results" element={<SolutionResultsPage />} /> */}
                        </Routes>
                    </div>
                </DndProvider>
            </Router>
        </div>
    );
}

export default App;
