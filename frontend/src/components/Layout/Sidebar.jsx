import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { signOut } from '../../services/authService';
import './Layout.css';

const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '⬛' },
    { path: '/mark-attendance', label: 'Mark Attendance', icon: '📸' },
    { path: '/register-face', label: 'Register Student', icon: '👤' },
    { path: '/attendance-list', label: 'Records', icon: '📋' },
];

const Sidebar = () => {
    const location = useLocation();
    const navigate = useNavigate();

    const handleSignOut = () => {
        signOut();
        navigate('/login');
    };

    return (
        <aside className="sidebar">
            <div className="sidebar-logo">
                <div className="sidebar-logo-icon">
                    <svg width="24" height="24" viewBox="0 0 32 32" fill="none">
                        <circle cx="16" cy="10" r="6" stroke="currentColor" strokeWidth="2"/>
                        <path d="M8 28c0-4.418 3.582-8 8-8s8 3.582 8 8" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        <circle cx="26" cy="8" r="3" fill="#6366f1"/>
                        <path d="M24 8l1.5 1.5L29 6" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </div>
                <span>AttendAI</span>
            </div>

            <nav className="sidebar-nav">
                {navItems.map(({ path, label, icon }) => (
                    <Link
                        key={path}
                        to={path}
                        id={`nav-${label.toLowerCase().replace(/\s+/g, '-')}`}
                        className={`sidebar-nav-item ${location.pathname === path ? 'active' : ''}`}
                    >
                        <span className="nav-icon">{icon}</span>
                        <span>{label}</span>
                    </Link>
                ))}
            </nav>

            <div className="sidebar-footer">
                <div className="sidebar-user">
                    <div className="user-avatar">
                        {localStorage.getItem('userEmail')?.[0]?.toUpperCase() || 'A'}
                    </div>
                    <div className="user-info">
                        <span className="user-email">{localStorage.getItem('userEmail') || 'Admin'}</span>
                        <span className="user-role">Administrator</span>
                    </div>
                </div>
                <button id="btn-signout" className="btn-signout" onClick={handleSignOut} title="Sign Out">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                        <polyline points="16 17 21 12 16 7"/>
                        <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
