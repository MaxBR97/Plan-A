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
        TotalMishmarot[person] == sum <person2,emda,zm
        an> in People * Mishmarot | person ==person2 : Shibutsim[person2,emda,zman];

minimize distributeShiftsEqually:
    sum <person> in People : (TotalMishmarot[person]**2);

