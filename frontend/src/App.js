import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

import { Amplify } from 'aws-amplify';
import { fetchAuthSession } from 'aws-amplify/auth';
import { withAuthenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';

import { cognitoConfig } from './config';

Amplify.configure({
    Auth: {
        Cognito: cognitoConfig,
    }
});

const API_URL = '/api/prompt';

const App = ({ signOut, user }) => {
    const [prompt, setPrompt] = useState('');
    const [response, setResponse] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const getAuthHeader = async () => {
        try {
            const session = await fetchAuthSession();
            const token = session.tokens?.idToken?.toString();
            return { 'Authorization': `Bearer ${token}` };
        } catch (error) {
            console.error('Error getting auth session:', error);
            return {};
        }
    };

    const handlePromptSubmit = async (event) => {
        event.preventDefault();
        if (!prompt.trim()) return;
        setIsLoading(true);
        setResponse('');

        try {
            const headers = { 'Content-Type': 'application/json', ...(await getAuthHeader()) };
            const res = await fetch(API_URL, {
                method: 'POST',
                headers,
                body: JSON.stringify({ prompt })
            });
            const data = await res.json();
            setResponse(data.response);
        } catch (error) {
            console.error('Error submitting prompt:', error);
            setResponse('Error: Could not get a response from the server.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1>AI Prompt</h1>
                {user && <button className="btn btn-secondary" onClick={signOut}>Sign Out</button>}
            </div>

            <div className="card shadow-sm mb-4">
                <div className="card-body">
                    <form onSubmit={handlePromptSubmit}>
                        <div className="form-group mb-3">
                            <label htmlFor="prompt-textarea">Enter your prompt</label>
                            <textarea
                                id="prompt-textarea"
                                className="form-control"
                                rows="3"
                                value={prompt}
                                onChange={(e) => setPrompt(e.target.value)}
                                placeholder="e.g., Explain the theory of relativity in simple terms"
                            ></textarea>
                        </div>
                        <button className="btn btn-primary" type="submit" disabled={isLoading}>
                            {isLoading ? 'Loading...' : 'Submit'}
                        </button>
                    </form>
                </div>
            </div>

            {response && (
                <div className="card shadow-sm">
                    <div className="card-body">
                        <h5 className="card-title">Response</h5>
                        <p className="card-text">{response}</p>
                    </div>
                </div>
            )}
        </div>
    );
};

export default withAuthenticator(App, {
    signUpAttributes: ['email'],
});
