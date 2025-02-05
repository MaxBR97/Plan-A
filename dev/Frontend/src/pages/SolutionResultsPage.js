import React from "react";
import { useZPL } from "../context/ZPLContext";

const SolutionResultsPage = () => {
    const { solutionResponse } = useZPL(); // Retrieve stored response

    return (
        <div>
            <h1>Solution Results</h1>
            {solutionResponse ? (
                <pre>{JSON.stringify(solutionResponse, null, 2)}</pre>
            ) : (
                <p>No solution available yet.</p>
            )}
        </div>
    );
};

export default SolutionResultsPage;
