import React from 'react';
import { Navigate } from 'react-router-dom';
import { getToken } from '../../services/authService';

/**
 * ProtectedRoute - Wraps any route that requires authentication.
 * If no JWT token is found, redirects to /login.
 */
const ProtectedRoute = ({ children }) => {
    const token = getToken();
    if (!token) {
        return <Navigate to="/login" replace />;
    }
    return children;
};

export default ProtectedRoute;
