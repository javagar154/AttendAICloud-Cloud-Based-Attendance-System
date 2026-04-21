import React from 'react';
import { useLocation } from 'react-router-dom';
import './Layout.css';

const pageTitles = {
    '/dashboard': { title: 'Dashboard', subtitle: 'Overview of attendance system' },
    '/mark-attendance': { title: 'Mark Attendance', subtitle: 'Capture face to mark attendance' },
    '/register-face': { title: 'Register Student', subtitle: 'Enroll a new student face' },
    '/attendance-list': { title: 'Attendance Records', subtitle: 'View and export attendance logs' },
};

const Navbar = () => {
    const location = useLocation();
    const page = pageTitles[location.pathname] || { title: 'AttendAI Cloud', subtitle: '' };
    const now = new Date();
    const dateStr = now.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });

    return (
        <header className="navbar">
            <div className="navbar-left">
                <h2 className="page-title">{page.title}</h2>
                <p className="page-subtitle">{page.subtitle}</p>
            </div>
            <div className="navbar-right">
                <div className="navbar-date">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                        <line x1="16" y1="2" x2="16" y2="6"/>
                        <line x1="8" y1="2" x2="8" y2="6"/>
                        <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                    {dateStr}
                </div>
                <div className="navbar-badge">
                    <span className="status-dot"></span>
                    System Online
                </div>
            </div>
        </header>
    );
};

export default Navbar;
