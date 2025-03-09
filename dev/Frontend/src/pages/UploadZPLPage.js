import React, { useState } from "react";
import axios from "axios";
import { useZPL } from "../context/ZPLContext";
import { Link, useNavigate } from "react-router-dom";
import "./UploadZPLPage.css";

const UploadZPLPage = () => {
    const {
        image,
        model,
        updateImageField,
        updateModel
    } = useZPL();

    const [fileContent, setFileContent] = useState(`
param restHours := 5;
param shiftTime := 4;
param totalShiftsPerStation := 5;
param shiftsUntil := shiftTime * totalShiftsPerStation;
param bias := 500;

set People := {"Yoni","Denis","Nadav","Max","er"};
set Emdot := {"North", "South"};
set Times := {0.. shiftsUntil by shiftTime};

set invalidShifts := {<"Yoni","North",0,20>};

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

subto enforceInvalidShifts:
    forall <person , station , fromTime , toTime> in invalidShifts:
        (sum <p,s,time> in People * Mishmarot | p == person and station == s and time >= fromTime and time <= toTime : Shibutsim[p,s,time]) == 0;

set indexSetOfPeople := {<i,p> in {1.. card(People)} * People | ord(People,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize distributeShiftsEqually:
    sum <person> in People : ((TotalMishmarot[person]+1)**2)
    + (sum <i,person,station,time> in  indexSetOfPeople*Mishmarot | time <= card(People)/card(Emdot)*shiftTime: (Shibutsim[person,station,time]*bias*i*time));
`);

    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleUpload = async () => {
        if (!image.imageName.trim()) {
            setMessage("Error: Image name cannot be empty.");
            return;
        }

        setLoading(true);
        setMessage("");

        const requestData = {
            code: fileContent,
            imageName: image.imageName.trim(),
            imageDescription: image.imageDescription,
        };

        console.log("Sending POST request:", requestData);

        try {
            const response = await axios.post("/images", requestData, {
                headers: { "Content-Type": "application/json" },
            });

            console.log("Response:", response);

            const { imageId, model } = response.data;

            // Update image state
            updateImageField("imageId", imageId);
            updateImageField("imageName", image.imageName.trim());
            updateImageField("imageDescription", image.imageDescription);

            // Update model state
            updateModel(model);

            setMessage("File uploaded successfully!");
            navigate("/configure-variables");
        } catch (error) {
            if (error.response) {
                setMessage(`Error: ${error.response.status} - ${error.response.data?.msg || "Unknown error occurred"}`);
            } else if (error.request) {
                setMessage("Error: No response from server. Check if backend is running.");
            } else {
                setMessage(`Error: ${error.message}`);
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="upload-zpl-page">
            <h1 className="page-title">Upload ZPL File</h1>
            <div className="upload-container">
                <label>Image Name:</label>
                <input
                    type="text"
                    value={image.imageName}
                    onChange={(e) => updateImageField("imageName", e.target.value)}
                    className="image-name-input"
                    placeholder="Enter image name..."
                />

                <label>ZIMPL code:</label>
                <textarea
                    value={fileContent}
                    onChange={(e) => setFileContent(e.target.value)}
                    className="fixed-textarea code"
                />

                <label>Image Description:</label>
                <textarea
                    value={image.imageDescription}
                    onChange={(e) => updateImageField("imageDescription", e.target.value)}
                    className="fixed-textarea"
                    placeholder="Enter image description..."
                />

                <button className="upload-button" onClick={handleUpload} disabled={loading}>
                    {loading ? "Uploading..." : "Upload"}
                </button>
            </div>
            {message && <p className="upload-message">{message}</p>}
            <Link to="/" className="back-button">
                Back
            </Link>
        </div>
    );
};

export default UploadZPLPage;
