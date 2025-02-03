param weight := 10;
param NumberOfSoldiers := 10;
param absoluteMinimalSpacing := 8;

set Soldiers := {1..NumberOfSoldiers};
set Stations := {"Shin Gimel", "Fillbox", "Siyur"};  # Station names
set Times := {0,4,8,12,16,20};

set Shifts := Stations * Times;            # Shift = (Station, Time)
set SoldiersToShifts := Soldiers * Shifts;
set ShiftSpacings := {<i,a,b,c,d> in Soldiers * Shifts * Shifts | b < d };

var Edge[SoldiersToShifts] binary;
var NeighbouringShifts[ShiftSpacings] binary;

subto trivial1:
    forall <j,a1,a2,b1,b2> in ShiftSpacings | a1 != b1 or a2 != b2 : 
        vif NeighbouringShifts[j,a1,a2,b1,b2] == 1 then Edge[j,a1,a2] == 1 end;

subto trivial4:
    forall <j,a1,a2,b1,b2> in ShiftSpacings | a1 != b1 or a2 != b2 : 
        vif NeighbouringShifts[j,a1,a2,b1,b2] == 1 then Edge[j,a1,a2] == Edge[j,b1,b2] end;

subto trivial5:
    forall <i,a,b> in SoldiersToShifts | b < max(Times): 
        vif Edge[i,a,b] == 1 and (sum<k,m,n> in SoldiersToShifts | k==i and n > b : Edge[k,m,n]) >= 1 
        then sum <j,a1,a2,b1,b2> in ShiftSpacings | i == j and (a==a1 and b==a2) : NeighbouringShifts[j,a1,a2,b1,b2] == 1 end;

subto trivial7:
    forall <i,a,b> in SoldiersToShifts | b > min(Times): 
        vif Edge[i,a,b] == 1 and (sum<k,m,n> in SoldiersToShifts | k==i and n < b : Edge[k,m,n]) >= 1 
        then sum <j,a1,a2,b1,b2> in ShiftSpacings | i == j and (a==b1 and b==b2) : NeighbouringShifts[j,a1,a2,b1,b2] == 1 end;

subto trivial3:
    forall <j,a1,a2,b1,b2> in ShiftSpacings | a1 != b1 or a2 != b2 : 
        NeighbouringShifts[j,a1,a2,b1,b2] == NeighbouringShifts[j,a1,a2,b1,b2] * ((sum <m,n> in Shifts | n > a2 and n < b2 : Edge[j,m,n]) + 1);

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <i,t,z> in SoldiersToShifts : 
        (sum<m,a,b> in SoldiersToShifts | m == i and z == b : Edge[i,a,b]) <= 1;

subto All_Stations_One_Soldier:
    forall <s,f> in Shifts : 
        (sum<a,b,c> in SoldiersToShifts | s==b and f==c : Edge[a,b,c]) == 1;

var minGuards integer >= 0 <= 5 priority 2 startval 1;
var maxGuards integer >= 1 <= 7 priority 4 startval 2;
var minimalSpacing integer >= absoluteMinimalSpacing <= absoluteMinimalSpacing+4 priority 12 startval 12;


subto minGuardsCons:
    forall <i> in Soldiers : 
        (sum <m,a,b> in SoldiersToShifts | i==m : Edge[m,a,b]) >= minGuards;

subto maxGuardsCons:
    forall <i> in Soldiers : 
        (sum <m,a,b> in SoldiersToShifts | i==m : Edge[m,a,b]) <= maxGuards;

subto minimalSpacingCons:
    forall <i,a,b,c,d> in ShiftSpacings : 
        vif (d - b) < minimalSpacing then NeighbouringShifts[i,a,b,c,d] == 0 end;

minimize myObjective: 
    ((maxGuards - minGuards) + weight)**3 
    - (minimalSpacing)**2 
    + sum<i,a,b> in SoldiersToShifts : sum<m,n> in Shifts | m != a or b != n : (Edge[i,a,b] * Edge[i,m,n] * (b - n));