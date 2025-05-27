set mySet := {7,6,4};
param x := 10;

var myVar[mySet] >= 0;

subto sampleConstraint:
    myVar[1] + myVar[2] + myVar[3] == x;

subto optionalConstraint:
    myVar[3] <= 5;

maximize myObjective:
    myVar[3];
