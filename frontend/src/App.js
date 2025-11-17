import React, { useState, useEffect } from 'react';
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

const PROMPT_API_URL = '/api/prompt';
const HISTORY_API_URL = '/api/history';

const App = ({ signOut, user }) => {
    const [prompt, setPrompt] = useState('');
    const [response, setResponse] = useState('');
    const [history, setHistory] = useState([]);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        fetchHistory();
    }, []);

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

    const fetchHistory = async () => {
        try {
            const headers = await getAuthHeader();
            const res = await fetch(HISTORY_API_URL, { headers });
            const data = await res.json();
            setHistory(data.sort((a, b) => b.createdAt - a.createdAt));
        } catch (error) {
            console.error('Error fetching history:', error);
        }
    };

    const handlePromptSubmit = async (event) => {
        event.preventDefault();
        if (!prompt.trim()) return;
        setIsLoading(true);
        setResponse('');

        try {
            const headers = { 'Content-Type': 'application/json', ...(await getAuthHeader()) };
            const res = await fetch(PROMPT_API_URL, {
                method: 'POST',
                headers,
                body: JSON.stringify({ prompt })
            });
            const data = await res.json();
            setResponse(data.response);
            fetchHistory();
        } catch (error) {
            console.error('Error submitting prompt:', error);
            setResponse('Error: Could not get a response from the server.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleHistoryClick = (item) => {
        setPrompt(item.prompt);
        setResponse(item.response);
    };

    const handleDelete = async (createdAt) => {
        try {
            const headers = await getAuthHeader();
            await fetch(`${HISTORY_API_URL}/${createdAt}`, {
                method: 'DELETE',
                headers,
            });
            // Remove the deleted item from the local state
            setHistory(history.filter(item => item.createdAt !== createdAt));
        } catch (error) {
            console.error('Error deleting history item:', error);
        }
    };

    const formatTimestamp = (epoch) => epoch ? new Date(epoch).toLocaleString() : 'N/A';

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
                <div className="card shadow-sm mb-4">
                    <div className="card-body">
                        <h5 className="card-title">Current Response</h5>
                        <p className="card-text">{response}</p>
                    </div>
                </div>
            )}

            <div className="card shadow-sm">
                <div className="card-body">
                    <h5 className="card-title">Chat History</h5>
                    {history.length > 0 ? (
                        <ul className="list-group list-group-flush">
                            {history.map((item) => (
                                <li key={item.createdAt} className="list-group-item">
                                    <div onClick={() => handleHistoryClick(item)} style={{cursor: 'pointer'}}>
                                        <p className="mb-1"><strong>You:</strong> {item.prompt}</p>
                                        <p className="mb-1"><strong>AI:</strong> {item.response}</p>
                                        <small className="text-muted">{formatTimestamp(item.createdAt)}</small>
                                    </div>
                                    <button 
                                        className="btn btn-danger btn-sm mt-2"
                                        onClick={(e) => {
                                            e.stopPropagation(); // Prevent the click from triggering handleHistoryClick
                                            handleDelete(item.createdAt);
                                        }}
                                    >
                                        Delete
                                    </button>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p>No history yet.</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default withAuthenticator(App, {
    signUpAttributes: ['email'],
});
