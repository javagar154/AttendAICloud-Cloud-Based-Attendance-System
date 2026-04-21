import React, { useRef, useState, useCallback } from 'react';
import Webcam from 'react-webcam';
import './Camera.css';

const videoConstraints = {
    width: 480,
    height: 360,
    facingMode: 'user',
};

/**
 * CameraCapture — captures webcam screenshot and returns Base64 image.
 * Props:
 *   onCapture(base64String) — callback with the captured image
 */
const CameraCapture = ({ onCapture }) => {
    const webcamRef = useRef(null);
    const [isCameraReady, setIsCameraReady] = useState(false);
    const [captured, setCaptured] = useState(null);
    const [error, setError] = useState('');

    const handleCapture = useCallback(() => {
        const imageSrc = webcamRef.current?.getScreenshot();
        if (!imageSrc) {
            setError('Could not capture image. Please allow camera access.');
            return;
        }
        // imageSrc is "data:image/jpeg;base64,..." - strip the prefix
        const base64 = imageSrc.split(',')[1];
        setCaptured(imageSrc); // keep full src for preview
        onCapture(base64);
        setError('');
    }, [onCapture]);

    const handleRetake = () => {
        setCaptured(null);
        onCapture(null);
    };

    return (
        <div className="camera-wrapper">
            {error && <div className="camera-error">{error}</div>}

            <div className="camera-frame">
                {!captured ? (
                    <>
                        <Webcam
                            ref={webcamRef}
                            audio={false}
                            screenshotFormat="image/jpeg"
                            videoConstraints={videoConstraints}
                            onUserMedia={() => setIsCameraReady(true)}
                            onUserMediaError={() => setError('Camera access denied. Please allow camera permission.')}
                            className="webcam-video"
                        />
                        {/* Face guide overlay */}
                        <div className="face-guide">
                            <div className="guide-corner tl"></div>
                            <div className="guide-corner tr"></div>
                            <div className="guide-corner bl"></div>
                            <div className="guide-corner br"></div>
                        </div>
                        {!isCameraReady && (
                            <div className="camera-loading">
                                <div className="spinner-large"></div>
                                <p>Initializing Camera...</p>
                            </div>
                        )}
                    </>
                ) : (
                    <img src={captured} alt="Captured face" className="captured-preview" />
                )}
            </div>

            <div className="camera-controls">
                {!captured ? (
                    <button
                        id="btn-capture"
                        className="btn-capture"
                        onClick={handleCapture}
                        disabled={!isCameraReady}
                    >
                        <div className="capture-ring">
                            <div className="capture-dot"></div>
                        </div>
                        Capture
                    </button>
                ) : (
                    <button id="btn-retake" className="btn-retake" onClick={handleRetake}>
                        🔄 Retake Photo
                    </button>
                )}
            </div>

            <p className="camera-hint">
                {captured
                    ? '✅ Photo captured — position your face in the frame for best results.'
                    : '📸 Center your face in the frame, then click Capture.'}
            </p>
        </div>
    );
};

export default CameraCapture;
