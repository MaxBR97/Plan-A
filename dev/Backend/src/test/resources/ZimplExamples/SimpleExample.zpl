set mySet := {1,2,3,4};
param x := 10;
param coefficient := 10;

var myVar[mySet] >= 0;

subto sampleConstraint:
    myVar[1] + myVar[2] + myVar[3] == x;
    
subto optionalConstraint:
    myVar[3] <= 5;

maximize myObjective:
    coefficient * myVar[3] +
    myVar[1];