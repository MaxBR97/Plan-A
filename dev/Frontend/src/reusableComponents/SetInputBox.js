import React, { useState } from 'react';
import SetEntry from '../reusableComponents/SetEntry';
import "./SetInputBox.css";
const SetInputBox = ({ 
    key,
    typeList,
    tupleTags,
    setName,
    handleAddTuple,
    handleTupleChange,
    handleTupleToggle,
    handleRemoveTuple,
    isRowSelected,
    setValues
}) => {

  return (
    <div key={key} className="module-box">
          {/* Display Variable Name */}
          <h3 className="module-title">{setName}</h3>

          {/* Display Type from setTypes */}
          <p className="variable-type">
            <strong>Type:</strong> {typeList.join(", ")}
          </p>

          {/* Add Button */}
          <button
            className="add-button"
            onClick={() => handleAddTuple(setName, typeList)}
          ></button>

          {/* Input Fields - Each type gets its own separate textbox */}
          {setValues?.map((row, rowIndex) => (
            <div key={rowIndex} className="input-row">
              
                  <SetEntry
                    key={rowIndex}
                    typeList={typeList}
                    row={row}
                    checked={isRowSelected(setName, rowIndex)}
                    onEdit={(e, typeIndex) => // Capture typeIndex here
                      handleTupleChange(setName, rowIndex, typeIndex, e.target.value)
                    }
                    onToggle={(e) =>
                      handleTupleToggle(
                        setName,
                        rowIndex,
                      )
                    }
                    onDelete={(e) =>
                      handleRemoveTuple(
                        setName,
                        rowIndex,
                      )
                    }
                    className="variable-input"
                    
                  />
              {/* Add a divider after each row */}
              <hr className="input-divider" />
            </div>
          ))}
        </div>
  );
};

export default SetInputBox;