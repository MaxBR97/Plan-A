import React from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
//import "./SolutionResultsPage.css";

const SolutionResultsPage = () => {
    const { solutionData } = useZPL(); // ✅ Get stored solution data

    if (!solutionData) {
        return <p className="error-message">No solution available. Please solve first.</p>;
    }

    return (
        <div className="solution-results-page">
            <h1 className="page-title">Solution Results</h1>

            {/* ✅ Solution Overview */}
            <div className="solution-overview">
                <p><strong>Status:</strong> {solutionData.solved ? "Solved ✅" : "Not Solved ❌"}</p>
                <p><strong>Solving Time:</strong> {solutionData.solvingTime} seconds</p>
                <p><strong>Objective Value:</strong> {solutionData.objectiveValue}</p>
            </div>

            {/* ✅ Display Each Variable's Solutions */}
            {Object.entries(solutionData.solution).map(([variableName, variableData]) => (
                <div key={variableName} className="variable-box">
                    <h2 className="variable-title">{variableName}</h2>

                    {/* ✅ Display Set Structure */}
                    <p><strong>Set Structure:</strong> {variableData.setStructure.join(", ")}</p>

                    {/* ✅ Display Solution Table */}
                    <table className="solution-table">
                        <thead>
                            <tr>
                                {variableData.typeStructure.map((type, index) => (
                                    <th key={index}>{type}</th>
                                ))}
                                <th>Objective Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            {variableData.solutions.map((solution, index) => (
                                <tr key={index}>
                                    {solution.values.map((value, vIndex) => (
                                        <td key={vIndex}>{value}</td>
                                    ))}
                                    <td>{solution.objectiveValue}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ))}

            {/* ✅ Back Button */}
            <Link to="/solution-preview" className="back-button">Back</Link>
        </div>
    );
};

export default SolutionResultsPage;
