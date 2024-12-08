set People := {"Yoni","Denis","Nadav","Max"};
set Emdot := {"North", "South"};
set Times := {16,20,24};

set Mishmarot := Emdot * Times; # -> {<North,16>, <North,20>, ....}

var Shibutsim[People * Mishmarot] binary; # -> {<Max,North,16>, <Max,North,20>, ....}


subto drisha1:
    forall <i,t> in People*Times: (sum <j,a,b> in People*Mishmarot | b == t and i == j : Shibutsim[i,a,b]) <= 1;

subto drisha2:
    forall <a,b> in Mishmarot : (sum <i,c,d> in People*Mishmarot| a==c and b==d: Shibutsim[i,a,b]) == 1;

minimize obj:
    1;

