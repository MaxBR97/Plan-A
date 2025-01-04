import React from 'react';
import { Link } from 'react-router-dom';
import './MainPage.css';

const MainPage = () => {
    return (
        <div className="main-page">
            <h1 className="main-title">Plan A</h1>
            <div className="button-container">
                <Link to="/work-assignment" className="main-button">
                    Shifts assignment
                </Link>
                <button className="main-button">Courses assignmnent</button>
                <button className="main-button">Upload new environment</button>
            </div>
            <div className="footer-button-container">
                <Link to="/upload-zpl" className="footer-button">
                    Create new environment (For developers)
                </Link>
            </div>
        </div>
    );
};

export default MainPage;
