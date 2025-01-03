import React from 'react';
import './MainPage.css';

const MainPage = () => {
    return (
        <div className="main-page">
            <h1 className="main-title">Plan A</h1>
            <div className="button-container">
                <button className="main-button">טעינת סביבה חדשה</button>
                <button className="main-button">תכנון שיבוץ קורסים</button>
                <button className="main-button">תכנון שיבוץ עבודה</button>
            </div>
            <div className="footer-button-container">
                <button className="footer-button">יצירת סביבה חדשה (למפתחים)</button>
            </div>
        </div>
    );
};

export default MainPage;
