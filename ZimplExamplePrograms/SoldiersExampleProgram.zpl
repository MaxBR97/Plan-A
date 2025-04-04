param weight := 10;
param NumberOfSoldiers := 9;
param bias := 10000;

set Soldiers := {1..NumberOfSoldiers};
set Stations := {"Shin Gimel", "Fillbox", "Patrol"};  # Station names
set Times := {0 .. 24 by 1};
set PossibleShiftAndRestTimes := {<time1, time2> in  Times*Times | time1 < time2: <time1,time2-time1>} union {<time1, time2> in  {-1}*Times | time1 < time2: <time1,time2-time1>};

set Shifts := Stations * Times;            # Shift = (Station, Time)
set SoldiersToShifts := Soldiers * Shifts;
set ShiftSpacings := {<i,a,b,rest> in Soldiers * (Stations union {"invisible"}) * (PossibleShiftAndRestTimes union {<max(Times),1>, <max(Times)+1,0>, <-1,max(Times)+2>}) };

# set ArtificialShiftSpacings := ShiftSpacings union {<i,a,b,rest> in Soldiers * {"invisible"} * ({<time1, time2> in  {-1}*Times | time1 < time2: <time1,time2-time1>} union {<9,0>}) };


var Edge[SoldiersToShifts] binary;
var NeighbouringShifts[ShiftSpacings] binary;

subto EnforceEveryOneTakeInvisibleShifts:
    forall <soldier> in Soldiers:
        (sum <i,a,b,r> in ShiftSpacings | i == soldier and a == "invisible" and b == -1 : NeighbouringShifts[soldier,a,b,r]) == 1;

subto EnforceEveryOneTakeInvisibleShifts2:
    forall <soldier> in Soldiers:
        (sum <i,a,b,r> in ShiftSpacings | i == soldier and a == "invisible" and b == max(Times)+1 : NeighbouringShifts[soldier,a,b,r]) == 1;

subto EnforceNoOneTakeInvisibleShiftInValidTime:
    forall <i,a,b,r> in ShiftSpacings | a =="invisible" and b >=0 and b <= max(Times) : NeighbouringShifts[i,a,b,r] == 0;

subto EnforceNoOneTakeValidShiftInInvalidTime:
    forall <i,a,b,r> in ShiftSpacings | a != "invisible" and (b == -1 or b == max(Times)+1) : NeighbouringShifts[i,a,b,r] == 0;

subto EnforceShiftSpacingLogic1:
    forall <soldier,station,time,rest> in ShiftSpacings  : vif NeighbouringShifts[soldier,station,time,rest] == 1
                                                        then (sum <i,a,b,r> in ShiftSpacings | i == soldier and b == (time + rest) : NeighbouringShifts[i,a,b,r]) >= 1 end;

subto EnforceShiftSpacingLogic2:
    forall <soldier,station,time,rest> in ShiftSpacings | time != -1   : vif NeighbouringShifts[soldier,station,time,rest] == 1
                                                        then (sum <i,a,b,r> in ShiftSpacings | i == soldier and (b + r == time) : NeighbouringShifts[i,a,b,r]) >= 1 end;


subto EnforceShiftSpacingLogic3:
    forall <soldier,station,time,rest> in ShiftSpacings | time >= min(Times) and time <= max(Times) and station != "invisible" : vif NeighbouringShifts[soldier,station,time,rest] == 1
                                                        then Edge[soldier,station,time] == 1 end;

subto EnforceShiftSpacingLogic4:
    forall <soldier,station,time,rest> in ShiftSpacings | time >= min(Times) and time <= max(Times) and station != "invisible": vif Edge[soldier,station,time] == 1
                                                        then (sum <i,a,b,r> in ShiftSpacings | i == soldier and a == station and b == time : NeighbouringShifts[i,a,b,r]) == 1 end;

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <i,t,z> in SoldiersToShifts : 
        (sum<m,a,b> in SoldiersToShifts | m == i and z == b : Edge[i,a,b]) <= 1;

subto All_Stations_One_Soldier:
    forall <s,f> in Shifts : 
        (sum<a,b,c> in SoldiersToShifts | s==b and f==c : Edge[a,b,c]) == 1;

set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize myObjective: 
    sum <soldier> in Soldiers : sum <i,station,time,rest> in ShiftSpacings | i == soldier : (rest**2)
    + (sum <i,person,station,time> in  indexSetOfPeople*Shifts | time <= card(Soldiers)/card(Stations)*4: (Edge[person,station,time]*bias*i*(time+1))); # assign first few soldiers by their order in the set