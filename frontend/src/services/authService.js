import { CognitoUserPool, AuthenticationDetails, CognitoUser, CognitoUserAttribute } from 'amazon-cognito-identity-js';
import { awsConfig } from '../config/awsConfig';

const poolData = {
    UserPoolId: awsConfig.userPoolId,
    ClientId: awsConfig.userPoolWebClientId
};

const userPool = new CognitoUserPool(poolData);
const isDummyMode = poolData.UserPoolId.includes('dummy');

export const signUp = (email, password, name) => {
    return new Promise((resolve, reject) => {
        if (isDummyMode) {
            console.log("Mocking successful sign up for dummy mode");
            // Automatically "log in" the user in dummy mode
            localStorage.setItem('jwtToken', 'dummy-jwt-token');
            localStorage.setItem('userEmail', email);
            setTimeout(() => resolve({ user: { username: email } }), 500);
            return;
        }

        const attributeList = [
            new CognitoUserAttribute({ Name: 'name', Value: name })
        ];

        userPool.signUp(email, password, attributeList, null, (err, result) => {
            if (err) {
                reject(err);
                return;
            }
            resolve(result.user);
        });
    });
};

export const signIn = (email, password) => {
    return new Promise((resolve, reject) => {
        if (isDummyMode) {
            console.log("Mocking successful sign in for dummy mode");
            localStorage.setItem('jwtToken', 'dummy-jwt-token');
            localStorage.setItem('userEmail', email);
            setTimeout(() => resolve({ idToken: { jwtToken: 'dummy-jwt-token' } }), 500);
            return;
        }

        const authenticationDetails = new AuthenticationDetails({
            Username: email,
            Password: password,
        });

        const cognitoUser = new CognitoUser({
            Username: email,
            Pool: userPool
        });

        cognitoUser.authenticateUser(authenticationDetails, {
            onSuccess: (result) => {
                const token = result.getIdToken().getJwtToken();
                localStorage.setItem('jwtToken', token);
                localStorage.setItem('userEmail', email);
                resolve(result);
            },
            onFailure: (err) => {
                reject(err);
            },
        });
    });
};

export const signOut = () => {
    if (!isDummyMode) {
        const cognitoUser = userPool.getCurrentUser();
        if (cognitoUser != null) {
            cognitoUser.signOut();
        }
    }
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userEmail');
};

export const getCurrentUser = () => {
    if (isDummyMode) {
        const email = localStorage.getItem('userEmail');
        return email ? { username: email } : null;
    }
    return userPool.getCurrentUser();
};

export const getToken = () => {
    return localStorage.getItem('jwtToken');
};
