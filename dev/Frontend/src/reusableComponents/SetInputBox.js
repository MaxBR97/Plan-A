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

  // In SetInputBox.jsx
// In SetInputBox.jsx
return (
  <div key={key} className="set-input">
    <h3 className="set-name">{setName}</h3>
    <p className="set-type">
      <strong>Type:</strong> {typeList.join(", ")}
    </p>
    <p className="total">Entries:{" "}
      {setValues?.filter((_, rowIndex) => isRowSelected(setName, rowIndex))
        .length || 0}
    </p>
    <button
      className="add-set-entry-button"
      onClick={() => handleAddTuple(setName, typeList)}
    >
      Add Entry
    </button>

    {/* Wrap all entries in a scrollable container div */}
    <div className="entries-container">
      {setValues?.map((row, rowIndex) => (
        <div key={rowIndex}>
          <SetEntry
            key={rowIndex}
            typeList={typeList}
            row={row}
            checked={isRowSelected(setName, rowIndex)}
            onEdit={(e, typeIndex) =>
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
          />
        </div>
      ))}
    </div>
  </div>
);
};

export default SetInputBox;