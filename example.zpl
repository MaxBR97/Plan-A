param restHours := 5;
param shiftTime := 4;
set People := {"Yoni","Denis","Nadav","Max","er"};
set Emdot := {"North", "South"};
set Times := {0..20 by shiftTime};

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

minimize distributeShiftsEqually:
    sum <person> in People : (TotalMishmarot[person]**2);

