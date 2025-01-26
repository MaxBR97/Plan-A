    param x := 10;
    set mySet := {1,2,3};

    var myVar[mySet];

    subto sampleConstraint:
        myVar[x] == mySet[1];

    maximize myObjective:
        1;
