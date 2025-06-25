import React, { useState } from 'react';
import SetEntry from '../reusableComponents/SetEntry';
import "./SetInputBox.css";

const SetInputBox = ({ 
    key,
    typeList,
    tupleTags,
    setName,
    setAlias,
    handleAddTuple,
    handleTupleChange,
    handleTupleToggle,
    handleRemoveTuple,
    isRowSelected,
    setValues
}) => {

  return (
    <div key={key} className="set-input">
      <div className="set-header">
        <div className="set-header-left">
          <h3 className="set-name">{setAlias ? setAlias : setName}</h3>
          <p className="total">Entries:{" "}
            {setValues?.filter((_, rowIndex) => isRowSelected(setName, rowIndex))
              .length || 0}
          </p>
        </div>
        <button
          className="add-set-entry-button"
          onClick={() => handleAddTuple(setName, typeList)}
        >
          Add Entry
        </button>
      </div>

      {/* Wrap all entries in a scrollable container div */}
      <div className="entries-container">
        <div className="tag-row">
          {typeList.map((type, index) => (
            <span key={index} className="tag-label">
              {tupleTags?.[index] || type}
            </span>
          ))}
        </div>
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
              onToggle={() => handleTupleToggle(setName, rowIndex)}
              onDelete={() => handleRemoveTuple(setName, rowIndex)}
            />
          </div>
        ))}
      </div>
    </div>
  );
};

export default SetInputBox;