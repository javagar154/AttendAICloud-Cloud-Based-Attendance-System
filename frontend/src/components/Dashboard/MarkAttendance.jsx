import React, { useState } from 'react';
import CameraCapture from '../Camera/CameraCapture';
import { markAttendance } from '../../services/attendanceService';
import '../Camera/Camera.css';

const MarkAttendance = () => {
    const [imageBase64, setImageBase64] = useState(null);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleCapture = (base64) => {
        setImageBase64(base64);
        setResult(null);
        setError('');
    };

    const handleSubmit = async () => {
        if (!imageBase64) {
            setError('Please capture a photo first.');
            return;
        }
        setLoading(true);
        setError('');
        setResult(null);
        try {
            const data = await markAttendance(imageBase64);
            setResult(data);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to mark attendance. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="mark-page">
            {/* Camera Panel */}
            <div className="mark-panel">
                <h3>📸 Camera Capture</h3>
                <CameraCapture onCapture={handleCapture} />
                {error && <div className="camera-error" style={{ marginTop: 16 }}>{error}</div>}
                <button
                    id="btn-mark-attendance"
                    className="btn-submit"
                    onClick={handleSubmit}
                    disabled={!imageBase64 || loading}
                >
                    {loading ? <span className="spinner-sm"></span> : '✅'}
                    {loading ? 'Processing...' : 'Mark Attendance'}
                </button>
            </div>

            {/* Result Panel */}
            <div className="mark-panel">
                <h3>📊 Recognition Result</h3>
                {!result ? (
                    <div style={{ textAlign: 'center', padding: '40px 0', color: 'rgba(255,255,255,0.2)' }}>
                        <div style={{ fontSize: '4rem', marginBottom: 16 }}>🔍</div>
                        <p style={{ margin: 0 }}>Capture a face and click<br/>"Mark Attendance" to see results.</p>
                    </div>
                ) : (
                    <div className={`result-card ${result.status?.toLowerCase() === 'present' ? 'present' : 'absent'}`}>
                        <div className="result-icon">
                            {result.status?.toLowerCase() === 'present' ? '✅' : '❌'}
                        </div>
                        <div className="result-status">{result.status || 'UNKNOWN'}</div>
                        <div className="result-detail">
                            <span>Student ID</span>
                            <span>{result.studentId || '—'}</span>
                        </div>
                        <div className="result-detail">
                            <span>Confidence</span>
                            <span>{result.confidence ? `${result.confidence.toFixed(2)}%` : '—'}</span>
                        </div>
                        <div className="result-detail">
                            <span>Message</span>
                            <span>{result.message || '—'}</span>
                        </div>
                        <div className="result-detail">
                            <span>Timestamp</span>
                            <span>{new Date().toLocaleString()}</span>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MarkAttendance;
