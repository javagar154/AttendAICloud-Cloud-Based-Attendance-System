import React, { useState } from 'react';
import './Dashboard.css';

const STATUS_COLORS = {
    PRESENT: { bg: 'rgba(34,197,94,0.15)', color: '#4ade80', border: 'rgba(34,197,94,0.3)' },
    ABSENT:  { bg: 'rgba(239,68,68,0.15)', color: '#f87171', border: 'rgba(239,68,68,0.3)' },
};

const AttendanceTable = ({ records = [], loading }) => {
    const [search, setSearch] = useState('');

    const filtered = Array.isArray(records) ? records.filter(r =>
        r.studentId?.toLowerCase().includes(search.toLowerCase()) ||
        r.studentName?.toLowerCase().includes(search.toLowerCase())
    ) : [];

    return (
        <div className="table-card">
            <div className="table-header">
                <h3>Attendance Records</h3>
                <div className="table-search">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                    </svg>
                    <input
                        id="search-records"
                        type="text"
                        placeholder="Search student..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>

            {loading ? (
                <div className="table-loading">
                    {[...Array(5)].map((_, i) => (
                        <div key={i} className="row-skeleton"></div>
                    ))}
                </div>
            ) : filtered.length === 0 ? (
                <div className="table-empty">
                    <span>📭</span>
                    <p>No attendance records found.</p>
                </div>
            ) : (
                <div className="table-wrapper">
                    <table className="attendance-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Student ID</th>
                                <th>Name</th>
                                <th>Timestamp</th>
                                <th>Confidence</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filtered.map((rec, idx) => {
                                const style = STATUS_COLORS[rec.status] || STATUS_COLORS.ABSENT;
                                return (
                                    <tr key={`${rec.studentId}-${rec.timestamp}`}>
                                        <td className="row-num">{idx + 1}</td>
                                        <td className="student-id">{rec.studentId}</td>
                                        <td>{rec.studentName || '—'}</td>
                                        <td className="timestamp">{new Date(rec.timestamp).toLocaleString()}</td>
                                        <td>
                                            <div className="confidence-bar">
                                                <div className="bar-fill" style={{ width: `${rec.confidence || 0}%` }}></div>
                                                <span>{rec.confidence ? `${rec.confidence.toFixed(1)}%` : '—'}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <span className="status-badge" style={{
                                                background: style.bg,
                                                color: style.color,
                                                border: `1px solid ${style.border}`
                                            }}>
                                                {rec.status}
                                            </span>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default AttendanceTable;
