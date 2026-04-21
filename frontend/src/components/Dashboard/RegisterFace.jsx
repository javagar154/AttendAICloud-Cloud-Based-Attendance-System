import React, { useState } from 'react';
import CameraCapture from '../Camera/CameraCapture';
import { registerFace } from '../../services/attendanceService';
import '../Camera/Camera.css';

const RegisterFace = () => {
    const [imageBase64, setImageBase64] = useState(null);
    const [formData, setFormData] = useState({ studentId: '', studentName: '', email: '', department: '' });
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState('');
    const [error, setError] = useState('');

    const handleCapture = (base64) => {
        setImageBase64(base64);
        setSuccess('');
        setError('');
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!imageBase64) {
            setError('Please capture a face photo first.');
            return;
        }
        setLoading(true);
        setError('');
        setSuccess('');
        try {
            await registerFace({ ...formData, imageBase64 });
            setSuccess(`✅ Student "${formData.studentName}" (${formData.studentId}) registered successfully!`);
            setFormData({ studentId: '', studentName: '', email: '', department: '' });
            setImageBase64(null);
        } catch (err) {
            // Priority: backend custom message > axios error message > fallback
            const msg = err.response?.data?.message || err.message || 'Registration failed. Please try again.';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="register-page">
            {/* Camera panel */}
            <div className="register-panel">
                <h3>📷 Capture Student Face</h3>
                <CameraCapture onCapture={handleCapture} />
            </div>

            {/* Form panel */}
            <div className="register-panel">
                <h3>📝 Student Information</h3>
                <form className="register-form" onSubmit={handleSubmit}>
                    <div className="form-field">
                        <label htmlFor="studentId">Student ID *</label>
                        <input
                            id="studentId" name="studentId" type="text"
                            placeholder="e.g. STU2024001"
                            value={formData.studentId}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-field">
                        <label htmlFor="studentName">Full Name *</label>
                        <input
                            id="studentName" name="studentName" type="text"
                            placeholder="e.g. Priya Sharma"
                            value={formData.studentName}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-field">
                        <label htmlFor="regEmail">Email Address</label>
                        <input
                            id="regEmail" name="email" type="email"
                            placeholder="student@college.edu"
                            value={formData.email}
                            onChange={handleChange}
                        />
                    </div>
                    <div className="form-field">
                        <label htmlFor="department">Department</label>
                        <select id="department" name="department" value={formData.department} onChange={handleChange}>
                            <option value="">Select Department</option>
                            <option value="CS">Computer Science</option>
                            <option value="EC">Electronics</option>
                            <option value="ME">Mechanical</option>
                            <option value="CE">Civil</option>
                            <option value="IT">Information Technology</option>
                        </select>
                    </div>

                    {success && <div className="register-success">{success}</div>}
                    {error && <div className="register-error">{error}</div>}

                    <button id="btn-register-face" type="submit" className="btn-submit" disabled={loading || !imageBase64}>
                        {loading ? <span className="spinner-sm"></span> : '👤'}
                        {loading ? 'Registering...' : 'Register Student'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default RegisterFace;
