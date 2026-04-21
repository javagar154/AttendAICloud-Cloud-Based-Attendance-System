import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Auth/Login';
import ProtectedRoute from './components/Auth/ProtectedRoute';
import Sidebar from './components/Layout/Sidebar';
import Navbar from './components/Layout/Navbar';
import Dashboard from './components/Dashboard/Dashboard';
import MarkAttendance from './components/Dashboard/MarkAttendance';
import RegisterFace from './components/Dashboard/RegisterFace';
import AttendanceTable from './components/Dashboard/AttendanceTable';
import { getAttendanceList } from './services/attendanceService';
import './App.css';

// Layout wrapper for authenticated pages
const AppLayout = ({ children }) => (
    <div className="app-layout">
        <Sidebar />
        <div className="app-main">
            <Navbar />
            <div className="page-content">
                {children}
            </div>
        </div>
    </div>
);

// Attendance list page (standalone use of table)
const AttendanceListPage = () => {
    const [records, setRecords] = React.useState([]);
    const [loading, setLoading] = React.useState(true);

    React.useEffect(() => {
        getAttendanceList()
            .then(setRecords)
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    return <AttendanceTable records={records} loading={loading} />;
};

const App = () => {
    return (
        <Router>
            <Routes>
                {/* Public */}
                <Route path="/login" element={<Login />} />

                {/* Protected */}
                <Route path="/dashboard" element={
                    <ProtectedRoute>
                        <AppLayout><Dashboard /></AppLayout>
                    </ProtectedRoute>
                } />
                <Route path="/mark-attendance" element={
                    <ProtectedRoute>
                        <AppLayout><MarkAttendance /></AppLayout>
                    </ProtectedRoute>
                } />
                <Route path="/register-face" element={
                    <ProtectedRoute>
                        <AppLayout><RegisterFace /></AppLayout>
                    </ProtectedRoute>
                } />
                <Route path="/attendance-list" element={
                    <ProtectedRoute>
                        <AppLayout><AttendanceListPage /></AppLayout>
                    </ProtectedRoute>
                } />

                {/* Default */}
                <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
        </Router>
    );
};

export default App;
