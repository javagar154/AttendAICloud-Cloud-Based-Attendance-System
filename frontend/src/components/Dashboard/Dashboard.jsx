import React, { useEffect, useState } from 'react';
import StatsCards from './StatsCards';
import AttendanceTable from './AttendanceTable';
import { getAttendanceSummary, getAttendanceList } from '../../services/attendanceService';
import './Dashboard.css';

const Dashboard = () => {
    const [summary, setSummary] = useState({});
    const [records, setRecords] = useState([]);
    const [loadingSummary, setLoadingSummary] = useState(true);
    const [loadingRecords, setLoadingRecords] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [summaryData, recordsData] = await Promise.all([
                    getAttendanceSummary(),
                    getAttendanceList(),
                ]);
                if (summaryData) setSummary(summaryData);
                if (recordsData) setRecords(recordsData);
            } catch (err) {
                console.error('Failed to load dashboard data:', err);
            } finally {
                setLoadingSummary(false);
                setLoadingRecords(false);
            }
        };
        fetchData();
    }, []);

    return (
        <div className="dashboard-page">
            {/* Welcome Banner */}
            <div className="welcome-banner">
                <div className="welcome-text">
                    <h2>Welcome back 👋</h2>
                    <p>Here's your attendance overview for today.</p>
                </div>
                <div className="welcome-icon">🎓</div>
            </div>

            {/* Stats */}
            <StatsCards summary={summary} loading={loadingSummary} />

            {/* Attendance Table */}
            <div style={{ marginTop: 32 }}>
                <AttendanceTable records={records} loading={loadingRecords} />
            </div>
        </div>
    );
};

export default Dashboard;
