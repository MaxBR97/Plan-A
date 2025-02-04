import React, { useState } from "react";
import axios from "axios";
import { useZPL } from "../context/ZPLContext";
import { useNavigate } from "react-router-dom";
import "./UploadZPLPage.css";

const UploadZPLPage = () => {
    const { imageId, setImageId, variables, setVariables, types, setTypes, constraints, setConstraints, preferences, setPreferences } = useZPL();

    const [fileName, setFileName] = useState("");
    const [fileContent, setFileContent] = useState(`
        param restHours := 5;
param shiftTime := 4;
set People := {"Yoni","Denis","Nadav","Max"};
set Emdot := {"North", "South"};
set Times := {0..20 by shiftTime};

set Mishmarot := Emdot * Times; # -> {<North,16>, <North,20>, ....}

var Shibutsim[People * Mishmarot] binary; # -> {<Max,North,16>, <Max,North,20>, ....}
var TotalMishmarot [People] integer >= 0;

subto drisha1:
    forall <i,t> in People*Times: (sum <j,a,b> in People*Mishmarot | b == t and i == j : Shibutsim[i,a,b]) <= 1;

subto drisha2:
    forall <a,b> in Mishmarot : (sum <i,c,d> in People*Mishmarot| a==c and b==d: Shibutsim[i,a,b]) == 1;

subto drisha3:
    forall <person, emda, zman> in People * Mishmarot : 
            vif Shibutsim[person,emda,zman] == 1
            then  (sum <person2, emda2, zman2> in People * Mishmarot | person == person2 and zman2 >= zman and zman2 <= zman+restHours : Shibutsim[person2, emda2, zman2]) == 1 end;

subto drisha4:
    forall <person> in People: 
        TotalMishmarot[person] == sum <person2,emda,zman> in People * Mishmarot | person ==person2 : Shibutsim[person2,emda,zman];

minimize distributeShiftsEqually:
    sum <person> in People : (TotalMishmarot[person]**2);



        `);
    const [message, setMessage] = useState("");
    const navigate = useNavigate();

    const handleUpload = async () => {

        const requestData = {
            code: fileContent,
        };

        try {
            const response = await axios.post("/images", requestData, {
                headers: {
                    "Content-Type": "application/json",
                },
            });

            const responseData = response.data;

            // Store response in the ZPL Context correctly
            setImageId(responseData.imageId);
            setVariables(responseData.model.variables);
            setConstraints(responseData.model.constraints);
            setPreferences(responseData.model.preferences);
            setTypes(responseData.model.types);

            console.log("Full Response Data:", responseData);

            // Create a JSON file and trigger a download
            const jsonData = JSON.stringify(responseData, null, 2);
            const blob = new Blob([jsonData], { type: "application/json" });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            console.log(jsonData);
            document.body.appendChild(a);
            //a.click();
            document.body.removeChild(a);

            setMessage("File uploaded successfully!");

            console.log("Stored Data:", {
                imageId,
                variables,
                types,
                constraints,
                preferences
            });


            navigate("/configure-variables"); // Redirect to Configure Variables Page
        } catch (error) {
            if (error.response) {
                const errorMsg = error.response.data?.msg || "Unknown error occurred";
                setMessage(`Error: ${error.response.status} - ${errorMsg}`);
            } else if (error.request) {
                setMessage("Error: No response from server. Check if backend is running.");
            } else {
                setMessage(`Error: ${error.message}`);
            }
        }
    };

    console.log("🚀 Upload Success - Parsed Variables:", variables);

    return (
        <div className="upload-zpl-page">
            <h1 className="page-title">Upload ZPL File</h1>
            <div className="upload-container">
               
                <label>File Content:</label>
                <textarea
                    value={fileContent}
                    onChange={(e) => setFileContent(e.target.value)}
                />
                <button className="upload-button" onClick={handleUpload}>Upload</button>
            </div>
            {message && <p className="upload-message">{message}</p>}
        </div>
    );
};

export default UploadZPLPage;
