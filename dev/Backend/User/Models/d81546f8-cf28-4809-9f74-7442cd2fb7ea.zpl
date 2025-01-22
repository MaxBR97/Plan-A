    param x := 10;
    set mySet := {1,2,3};

    var myVar[mySet] binary;

    subto sampleConstraint:
        myVar[1] == 0;

    maximize myObjective:
        1;
