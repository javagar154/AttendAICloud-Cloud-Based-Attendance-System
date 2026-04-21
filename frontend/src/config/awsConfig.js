export const awsConfig = {
    region: import.meta.env.VITE_AWS_REGION || 'us-east-1',
    userPoolId: import.meta.env.VITE_COGNITO_USER_POOL_ID || 'us-east-1_dummyPoolId',
    userPoolWebClientId: import.meta.env.VITE_COGNITO_CLIENT_ID || 'dummyClientId1234567890',
};
