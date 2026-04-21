import React from 'react';
import './Dashboard.css';

const statsConfig = [
    {
        id: 'stat-total',
        label: 'Total Students',
        key: 'totalStudents',
        icon: '👥',
        gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
        shadow: 'rgba(99,102,241,0.4)',
    },
    {
        id: 'stat-present',
        label: 'Present Today',
        key: 'present',
        icon: '✅',
        gradient: 'linear-gradient(135deg, #22c55e, #16a34a)',
        shadow: 'rgba(34,197,94,0.4)',
    },
    {
        id: 'stat-absent',
        label: 'Absent Today',
        key: 'absent',
        icon: '❌',
        gradient: 'linear-gradient(135deg, #ef4444, #b91c1c)',
        shadow: 'rgba(239,68,68,0.4)',
    },
    {
        id: 'stat-rate',
        label: 'Attendance Rate',
        key: 'percentage',
        icon: '📊',
        gradient: 'linear-gradient(135deg, #f59e0b, #d97706)',
        shadow: 'rgba(245,158,11,0.4)',
        suffix: '%',
    },
];

const StatsCards = ({ summary = {}, loading }) => {
    return (
        <div className="stats-grid">
            {statsConfig.map(({ id, label, key, icon, gradient, shadow, suffix }) => (
                <div key={key} id={id} className="stat-card">
                    <div className="stat-icon" style={{ background: gradient, boxShadow: `0 8px 20px ${shadow}` }}>
                        {icon}
                    </div>
                    <div className="stat-info">
                        <p className="stat-label">{label}</p>
                        {loading ? (
                            <div className="stat-skeleton"></div>
                        ) : (
                            <h3 className="stat-value">
                                {summary[key] ?? '—'}{suffix || ''}
                            </h3>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default StatsCards;
