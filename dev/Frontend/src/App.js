import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import WorkAssignmentPage from './pages/WorkAssignmentPage';
import UploadZPLPage from './pages/UploadZPLPage';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/work-assignment" element={<WorkAssignmentPage />} />
                <Route path="/upload-zpl" element={<UploadZPLPage />} />
            </Routes>
        </Router>
    );
}

export default App;
