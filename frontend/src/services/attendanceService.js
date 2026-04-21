import apiService from './apiService';

/**
 * Submit attendance by sending a captured face image (Base64)
 * @param {string} imageBase64 - Base64 encoded image string
 * @returns {Promise<Object>} - { studentId, confidence, status, message }
 */
export const markAttendance = async (imageBase64) => {
    const response = await apiService.post('/attendance/mark', { imageBase64 });
    return response.data.data;
};

/**
 * Get attendance list for a student or all students
 * @param {string|null} studentId - Optional studentId filter
 * @returns {Promise<Array>} - Array of AttendanceRecord objects
 */
export const getAttendanceList = async (studentId = null) => {
    const params = studentId ? { studentId } : {};
    const response = await apiService.get('/attendance/list', { params });
    return response.data.data;
};

/**
 * Register a new student face in Rekognition collection
 * @param {Object} payload - { studentId, studentName, imageBase64, email }
 * @returns {Promise<Object>}
 */
export const registerFace = async (payload) => {
    const response = await apiService.post('/face/upload', payload);
    return response.data.data;
};

/**
 * Create a new Rekognition face collection (admin action)
 * @param {string} collectionId
 * @returns {Promise<Object>}
 */
export const createCollection = async (collectionId) => {
    const response = await apiService.post('/face/collection/create', { collectionId });
    return response.data.data;
};

/**
 * Get today's attendance summary statistics
 * @returns {Promise<Object>} - { totalStudents, present, absent, percentage }
 */
export const getAttendanceSummary = async () => {
    const response = await apiService.get('/attendance/summary');
    return response.data.data;
};
