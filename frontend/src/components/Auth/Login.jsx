import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { signIn, signUp } from '../../services/authService';
import './Auth.css';

const Login = () => {
    const navigate = useNavigate();
    const [mode, setMode] = useState('login'); // 'login' | 'signup'
    const [formData, setFormData] = useState({ name: '', email: '', password: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
        setError('');
    };

    // Redirect to dashboard if already logged in
    React.useEffect(() => {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            navigate('/dashboard');
        }
    }, [navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            if (mode === 'login') {
                await signIn(formData.email, formData.password);
                navigate('/dashboard');
            } else {
                await signUp(formData.email, formData.password, formData.name);
                setError('');
                // Check if we are in dummy mode (token gets set immediately in dummy signUp)
                if (localStorage.getItem('jwtToken')) {
                    navigate('/dashboard');
                } else {
                    // After real signup, show confirmation message
                    setMode('confirm');
                }
            }
        } catch (err) {
            setError(err.message || 'Authentication failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            {/* Background particles */}
            <div className="auth-bg">
                <div className="particle particle-1"></div>
                <div className="particle particle-2"></div>
                <div className="particle particle-3"></div>
            </div>

            <div className="auth-card">
                {/* Logo / Branding */}
                <div className="auth-logo">
                    <div className="logo-icon">
                        <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                            <circle cx="16" cy="10" r="6" stroke="currentColor" strokeWidth="2"/>
                            <path d="M8 28c0-4.418 3.582-8 8-8s8 3.582 8 8" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="26" cy="8" r="3" fill="#6366f1"/>
                            <path d="M24 8l1.5 1.5L29 6" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <h1 className="logo-text">AttendAI Cloud</h1>
                    <p className="logo-sub">Face Recognition Attendance System</p>
                </div>

                {mode === 'confirm' ? (
                    <div className="confirm-message">
                        <div className="confirm-icon">✓</div>
                        <h2>Account Created!</h2>
                        <p>Please check your email for a confirmation code from AWS Cognito, then log in below.</p>
                        <button className="btn-primary" onClick={() => setMode('login')}>Go to Login</button>
                    </div>
                ) : (
                    <>
                        {/* Tab toggle */}
                        <div className="auth-tabs">
                            <button
                                id="tab-login"
                                className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
                                onClick={() => { setMode('login'); setError(''); }}
                            >Sign In</button>
                            <button
                                id="tab-signup"
                                className={`auth-tab ${mode === 'signup' ? 'active' : ''}`}
                                onClick={() => { setMode('signup'); setError(''); }}
                            >Sign Up</button>
                        </div>

                        <form className="auth-form" onSubmit={handleSubmit}>
                            {mode === 'signup' && (
                                <div className="form-group">
                                    <label htmlFor="name">Full Name</label>
                                    <input
                                        id="name"
                                        type="text"
                                        name="name"
                                        placeholder="John Doe"
                                        value={formData.name}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            )}
                            <div className="form-group">
                                <label htmlFor="email">Email Address</label>
                                <input
                                    id="email"
                                    type="email"
                                    name="email"
                                    placeholder="you@example.com"
                                    value={formData.email}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="password">Password</label>
                                <input
                                    id="password"
                                    type="password"
                                    name="password"
                                    placeholder="••••••••"
                                    value={formData.password}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            {error && <div className="auth-error">{error}</div>}

                            <button id="btn-submit-auth" type="submit" className="btn-primary" disabled={loading}>
                                {loading
                                    ? <span className="spinner"></span>
                                    : mode === 'login' ? 'Sign In' : 'Create Account'
                                }
                            </button>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
};

export default Login;
