param conditioner := 10;
param soldiers := 9;
param absoluteMinimalRivuah := 8;

set setWithRange := {1..100 by conditioner};
set C := {1..soldiers};
set Emdot := {"Shin Gimel", "Fillbox"};
set Zmanim := {0,4,8,12,16,20};
set S := Emdot * Zmanim;
set CxS := C * S;
set CxSxS := {<i,a,b,c,d> in C * S * S | b < d };
set forTest1 := {<num, str, str2, str3> in C * Emdot * {<"a","b">} : <str, soldiers, 2*num, soldiers-num, card(CxS)>};
set forTest2 := {"a", "b"} * S * {1..soldiers} * C * {<"h",2.2> , <"a",-3.14>}; 
set forTest3 := {<2,"a",3>,<6,"2",3>};
set forTest4 := proj(forTest3,<2,1>);
set forTest5 := proj(forTest4 * {<2,"a">,<1,"f">},<2,4>); # -> <INT,TEXT>
set forTest6  := {0..soldiers-1};
param forTest7 := conditioner mod 2+soldiers;
param forTest8 := floor(conditioner+0.5);
param forTest9 := random(-2,conditioner);
param forTest10 := 2 + ord(forTest3,1,3);
set forTest11 := C union Zmanim;
param forTest12 := 0.15;
param forTest13 := min(1,conditioner,3);
param forTest14 := min(forTest11);

defstrg huehott(a) := if a < 0 then "hue" else "hott" end;
defbool myBool(x,y) := x == y and x > 2;
defnumb timeDifference(fromDay, fromHour, toDay, toHour) := soldiers;
set Soldiers := {1..soldiers};
set Stations := {<"Siyur1",8,4>,<"FillBox",4,1>,<"Shin Gimel",4,1>, <"Hamal",24,1>, <"Siyur2",8,4>, <"Siyur3",8,4>};
param planTimeRange := timeDifference(conditioner,soldiers,0,absoluteMinimalRivuah);
set Times := {0 .. planTimeRange};
set Shifts := {<station, stationInterval, requiredPeople, time> in Stations * Times | time mod stationInterval == 0 and time + stationInterval <= planTimeRange};
set SoldiersToShifts := Soldiers * proj(Shifts,<1,4>);
# set ShiftSpacings := {<soldier,station,stationInterval,shiftStartTime,shiftStartTime2, nextShiftTime> in Soldiers * proj(Shifts,<1,2,4>) * PossibleShiftAndRestTimes | shiftStartTime == shiftStartTime2 and nextShiftTime - shiftStartTime - stationInterval >= minimumRestTime : <soldier,station,stationInterval,shiftStartTime, nextShiftTime - shiftStartTime - stationInterval>};
# set ArtificialShift := {<soldier,station,interval,time,rest> in Soldiers * {<"inv",0>} * ({<time, firstShiftTime> in  {-1}*(Times union {max(Times)+1,max(Times)+2}) : <time, firstShiftTime-time>} union {<max(Times)+1,0>}) };


var edge[CxS] binary;
var couples[CxSxS] binary;
var varForTest1[CxS *{"A","a"} * S * {1 .. 5}];

param paramForTest1 := card(Zmanim);
subto condForTest1:
    sum <time> in Zmanim:
        sum <i,a,b> in CxS | b == time or b <= card(Emdot): 
            edge[i,a,b] == paramForTest1;

param paramForTest2 := card(Zmanim);
subto condForTest2:
    edge[1,"Shin Gimel", 0] == max(1,min(paramForTest2,0,0));

subto trivial1:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : vif couples[j,a1,a2,b1,b2] == 1 then edge[j,a1,a2] == 1  end;

subto trivial4:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : vif couples[j,a1,a2,b1,b2] == 1 then edge[j,a1,a2] == edge[j,b1,b2]  end;

subto trivial5:
    forall <i,a,b> in CxS | b < max(Zmanim): vif edge[i,a,b] == 1 and (sum<k,m,n> in CxS| k==i and n > b: edge[k,m,n]) >= 1 then sum <j,a1,a2,b1,b2> in CxSxS | i == j and (a==a1 and b==a2) : couples[j,a1,a2,b1,b2] == 1 end;

subto trivial7:
    forall <i,a,b> in CxS | b > min(Zmanim): vif edge[i,a,b] == 1 and (sum<k,m,n> in CxS| k==i and n < b: edge[k,m,n]) >= 1 then sum <j,a1,a2,b1,b2> in CxSxS | i == j and (a==b1 and b==b2) : couples[j,a1,a2,b1,b2] == 1 end;

subto trivial3:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : couples[j,a1,a2,b1,b2] == couples[j,a1,a2,b1,b2] *((sum <m,n> in S | n > a2 and n < b2: edge[j,m,n]) + 1);

subto Hayal_Lo_Shomer_Beshtey_Emdot_Bo_Zmanit:
    forall <i,t,z> in CxS : ( sum<m,a,b> in CxS | m == i and z == b : edge[i,a,b]) <= 1;

subto Kol_Haemdot_Meshubatsot_Hayal_Ehad:
    forall <s,f> in S: (sum<a,b,c> in CxS | s==b and f==c: edge[a,b,c]) == 1;

var minShmirot integer  >= 0 <= 5 priority 2 startval 1;
var maxShmirot integer >= 1 <= 7 priority 4 startval 2;
var minimalRivuah integer >= absoluteMinimalRivuah <= absoluteMinimalRivuah+4 priority 12 startval 12;

subto minShmirotCons:
    forall <i> in C: (sum <m,a,b> in CxS | i==m : edge[m,a,b]) >= minShmirot;

subto maxShmirotCons:
    forall <i> in C: (sum <m,a,b> in CxS | i==m : edge[m,a,b]) <= maxShmirot;

subto minimalRivuahCons:
    forall <i,a,b,c,d> in CxSxS: vif (d-b) < minimalRivuah then couples[i,a,b,c,d] == 0 end;

minimize rivuah: 
    ((maxShmirot-minShmirot)+conditioner)**3 -
    (minimalRivuah)**2 +
    (sum <i,a,b> in CxS: sum<m,n> in S | m != a or b!=n :(edge[i,a,b] * edge[i,m,n] * (b-n)))*8;

set People := {"fdas","re"};
var TotalMishmarot[People] binary;

maximize distributeShiftsEqually:
    sum<person> in People:          (TotalMishmarot[person]**2);